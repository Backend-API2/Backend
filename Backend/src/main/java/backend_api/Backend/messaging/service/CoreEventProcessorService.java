package backend_api.Backend.messaging.service;

import backend_api.Backend.Entity.payment.Payment;
import backend_api.Backend.Entity.payment.PaymentStatus;
import backend_api.Backend.Service.Interface.PaymentService;
import backend_api.Backend.messaging.dto.*;
import backend_api.Backend.messaging.publisher.CoreEventPublisher;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class CoreEventProcessorService {

    private final CoreEventPublisher coreEventPublisher;
    private final PaymentService paymentService;
    private final ObjectMapper objectMapper;

    public void processPaymentRequestFromCore(CoreEventMessage coreMessage) {
        log.info("Procesando solicitud de pago del CORE - MessageId: {}", coreMessage.getMessageId());

        PaymentRequestPayload paymentRequest = objectMapper.convertValue(
            coreMessage.getPayload(),
            PaymentRequestPayload.class
        );

        Long userId = paymentRequest.getUserId();
        Long providerId = paymentRequest.getProviderId();
        Long solicitudId = paymentRequest.getSolicitudId();

        log.info("IDs extraídos - SolicitudId: {}, UserId: {}, ProviderId: {}",
            solicitudId, userId, providerId);

        sendIdsToCore(solicitudId, userId, providerId, coreMessage.getMessageId());
    }

    private void sendIdsToCore(Long solicitudId, Long userId, Long providerId, String originalMessageId) {
        UserProviderIdsPayload payload = UserProviderIdsPayload.builder()
            .solicitudId(solicitudId)
            .userId(userId)
            .providerId(providerId)
            .build();

        Map<String, Object> payloadMap = objectMapper.convertValue(payload, Map.class);

        CoreResponseMessage response = CoreResponseMessage.builder()
            .messageId(UUID.randomUUID().toString())
            .timestamp(LocalDateTime.now())
            .source("payments")
            .destination(CoreResponseMessage.Destination.builder()
                .channel("payments.ids.extracted")
                .eventName("extracted")
                .build())
            .payload(payloadMap)
            .build();

        coreEventPublisher.publishToCore(response);
        log.info("IDs enviados al CORE para distribución - SolicitudId: {}", solicitudId);
    }

    public void processUserProviderDataFromCore(CoreEventMessage coreMessage) {
        log.info("Recibido datos de usuario/prestador del CORE - MessageId: {}", coreMessage.getMessageId());

        Map<String, Object> payload = coreMessage.getPayload();
        log.info("Datos recibidos del CORE: {}", payload);

        // Extraer datos del payload
        Long solicitudId = extractLong(payload, "solicitudId");
        Long userId = extractLong(payload, "userId");
        Long providerId = extractLong(payload, "providerId");
        BigDecimal amount = extractBigDecimal(payload, "amount");
        String currency = extractString(payload, "currency");
        String description = extractString(payload, "description");
        Long cotizacionId = extractLong(payload, "cotizacionId");

        // Extraer datos de usuario y prestador si vienen en el payload
        Map<String, Object> userData = extractMap(payload, "userData");
        Map<String, Object> providerData = extractMap(payload, "providerData");

        log.info("Creando pago - SolicitudId: {}, UserId: {}, ProviderId: {}, Amount: {}",
            solicitudId, userId, providerId, amount);

        // Crear el pago
        Payment payment = createPaymentFromCoreData(
            solicitudId, userId, providerId, amount, currency, description,
            cotizacionId, userData, providerData, coreMessage.getMessageId()
        );

        Payment savedPayment = paymentService.createPayment(payment);
        log.info("Pago creado exitosamente - PaymentId: {}, SolicitudId: {}",
            savedPayment.getId(), solicitudId);

        // Enviar confirmación al CORE
        sendPaymentCreatedConfirmation(savedPayment, coreMessage.getMessageId());
    }

    private Payment createPaymentFromCoreData(Long solicitudId, Long userId, Long providerId,
                                             BigDecimal amount, String currency, String description,
                                             Long cotizacionId, Map<String, Object> userData,
                                             Map<String, Object> providerData, String originalMessageId) {
        Payment payment = new Payment();
        payment.setUser_id(userId);
        payment.setProvider_id(providerId);
        payment.setAmount_total(amount != null ? amount : BigDecimal.ZERO);
        payment.setAmount_subtotal(amount != null ? amount : BigDecimal.ZERO);
        payment.setTaxes(BigDecimal.ZERO);
        payment.setFees(BigDecimal.ZERO);
        payment.setCurrency(currency != null ? currency : "USD");
        payment.setStatus(PaymentStatus.PENDING_PAYMENT);
        payment.setCreated_at(LocalDateTime.now());
        payment.setUpdated_at(LocalDateTime.now());

        // Construir metadata con toda la información
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("solicitudId", solicitudId);
        metadata.put("coreMessageId", originalMessageId);

        if (cotizacionId != null) {
            metadata.put("cotizacionId", cotizacionId);
        }
        if (description != null) {
            metadata.put("description", description);
        }
        if (userData != null) {
            metadata.put("userData", userData);
        }
        if (providerData != null) {
            metadata.put("providerData", providerData);
        }

        try {
            payment.setMetadata(objectMapper.writeValueAsString(metadata));
        } catch (Exception e) {
            log.error("Error serializando metadata: {}", e.getMessage());
            payment.setMetadata("{}");
        }

        return payment;
    }

    private void sendPaymentCreatedConfirmation(Payment payment, String originalMessageId) {
        Map<String, Object> confirmPayload = new HashMap<>();
        confirmPayload.put("paymentId", payment.getId());
        confirmPayload.put("solicitudId", extractSolicitudIdFromMetadata(payment.getMetadata()));
        confirmPayload.put("status", payment.getStatus().toString());
        confirmPayload.put("amount", payment.getAmount_total());
        confirmPayload.put("currency", payment.getCurrency());

        CoreResponseMessage confirmation = CoreResponseMessage.builder()
            .messageId(UUID.randomUUID().toString())
            .timestamp(LocalDateTime.now())
            .source("payments")
            .destination(CoreResponseMessage.Destination.builder()
                .channel("payments.payment.created")
                .eventName("created")
                .build())
            .payload(confirmPayload)
            .build();

        coreEventPublisher.publishToCore(confirmation);
        log.info("Confirmación de pago enviada al CORE - PaymentId: {}", payment.getId());
    }

    // Métodos auxiliares para extraer datos del payload
    private Long extractLong(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) return null;
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        try {
            return Long.parseLong(value.toString());
        } catch (NumberFormatException e) {
            log.warn("No se pudo convertir {} a Long: {}", key, value);
            return null;
        }
    }

    private BigDecimal extractBigDecimal(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) return null;
        if (value instanceof BigDecimal) {
            return (BigDecimal) value;
        }
        try {
            return new BigDecimal(value.toString());
        } catch (NumberFormatException e) {
            log.warn("No se pudo convertir {} a BigDecimal: {}", key, value);
            return null;
        }
    }

    private String extractString(Map<String, Object> map, String key) {
        Object value = map.get(key);
        return value != null ? value.toString() : null;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> extractMap(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value instanceof Map) {
            return (Map<String, Object>) value;
        }
        return null;
    }

    private Long extractSolicitudIdFromMetadata(String metadataJson) {
        try {
            Map<String, Object> metadata = objectMapper.readValue(metadataJson, Map.class);
            return extractLong(metadata, "solicitudId");
        } catch (Exception e) {
            log.error("Error extrayendo solicitudId del metadata: {}", e.getMessage());
            return null;
        }
    }
}
