package backend_api.Backend.messaging.service;

import backend_api.Backend.Entity.payment.Payment;
import backend_api.Backend.Entity.payment.PaymentStatus;
import backend_api.Backend.Service.Interface.PaymentService;
import backend_api.Backend.Service.Implementation.DataStorageServiceImpl;
import backend_api.Backend.messaging.dto.*;
import backend_api.Backend.messaging.publisher.CoreEventPublisher;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
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
    private final DataStorageServiceImpl dataStorageService;
    private final ObjectMapper objectMapper;

    public void processPaymentRequestFromCore(CoreEventMessage coreMessage) {
        log.info("Procesando solicitud de pago del CORE - MessageId: {}", coreMessage.getMessageId());

        Map<String, Object> payload = coreMessage.getPayload();
        Long solicitudId = extractLong(payload, "solicitudId");
        Long userId = extractLong(payload, "userId");
        Long providerId = extractLong(payload, "providerId");
        Double amount = extractDouble(payload, "amount");
        String currency = extractString(payload, "currency");
        String description = extractString(payload, "description");

        log.info("IDs extraídos - SolicitudId: {}, UserId: {}, ProviderId: {}",
            solicitudId, userId, providerId);

        // Siempre buscar datos en BD local (que ya recibimos del módulo de usuarios)
        log.info("Buscando datos en BD local para crear pago - SolicitudId: {}, UserId: {}, ProviderId: {}", 
            solicitudId, userId, providerId);
        
        // Verificar que tenemos todos los datos necesarios
        if (!dataStorageService.userDataExists(userId)) {
            log.error("Usuario no encontrado en BD local - UserId: {}", userId);
            return;
        }
        
        if (!dataStorageService.providerDataExists(providerId)) {
            log.error("Proveedor no encontrado en BD local - ProviderId: {}", providerId);
            return;
        }
        
        // Crear pago con datos de BD local
        createPaymentFromStoredData(solicitudId, userId, providerId, amount, currency, description, coreMessage.getMessageId());
    }


    private void createPaymentFromStoredData(Long solicitudId, Long userId, Long providerId, 
                                           Double amount, String currency, String description, 
                                           String originalMessageId) {
        log.info("Creando pago desde datos almacenados - SolicitudId: {}, UserId: {}, ProviderId: {}", 
            solicitudId, userId, providerId);

        var userData = dataStorageService.getUserData(userId);
        var providerData = dataStorageService.getProviderData(providerId);

        // Crear el pago
        Payment payment = new Payment();
        payment.setUser_id(userId);
        payment.setProvider_id(providerId);
        payment.setSolicitud_id(solicitudId); // Agregar solicitud_id
        payment.setAmount_total(amount != null ? BigDecimal.valueOf(amount) : BigDecimal.ZERO);
        payment.setAmount_subtotal(amount != null ? BigDecimal.valueOf(amount) : BigDecimal.ZERO);
        payment.setTaxes(BigDecimal.ZERO);
        payment.setFees(BigDecimal.ZERO);
        payment.setCurrency(currency != null ? currency : "USD");
        payment.setStatus(PaymentStatus.PENDING_PAYMENT);
        payment.setCreated_at(LocalDateTime.now());
        payment.setUpdated_at(LocalDateTime.now());
        payment.setDescripcion(description); // Agregar descripcion si está disponible

        // Construir metadata con el mismo formato que PaymentRequestProcessorService
        Map<String, Object> metadata = new HashMap<>();
        // Generar idCorrelacion a partir de solicitudId si no está disponible
        String idCorrelacion = "PED-" + (solicitudId != null ? solicitudId : "UNKNOWN");
        metadata.put("idCorrelacion", idCorrelacion);
        // MetodoPreferido no está disponible en este flujo, usar null o un valor por defecto
        metadata.put("metodoPreferido", null);

        if (userData.isPresent()) {
            metadata.put("userName", userData.get().getName());
            metadata.put("userEmail", userData.get().getEmail());
        }
        if (providerData.isPresent()) {
            metadata.put("providerName", providerData.get().getName());
            metadata.put("providerEmail", providerData.get().getEmail());
        }

        try {
            payment.setMetadata(objectMapper.writeValueAsString(metadata));
        } catch (Exception e) {
            log.error("Error serializando metadata: {}", e.getMessage());
            payment.setMetadata("{}");
        }

        Payment savedPayment = paymentService.createPayment(payment);
        log.info("Pago creado exitosamente desde datos almacenados - PaymentId: {}, SolicitudId: {}",
            savedPayment.getId(), solicitudId);

        // Enviar confirmación al CORE
        sendPaymentCreatedConfirmation(savedPayment, originalMessageId);
    }

    public void processUserProviderDataFromCore(CoreEventMessage coreMessage) {
        log.info("Recibido datos de usuario/prestador del CORE - MessageId: {}", coreMessage.getMessageId());

        Map<String, Object> payload = coreMessage.getPayload();
        log.info("Datos recibidos del CORE: {}", payload);

        // Extraer datos del payload
        Long solicitudId = extractLong(payload, "solicitudId");
        Long userId = extractLong(payload, "userId");
        Long providerId = extractLong(payload, "providerId");
        Double amount = extractDouble(payload, "amount");
        String currency = extractString(payload, "currency");
        String description = extractString(payload, "description");
        String status = extractString(payload, "status");

        // Extraer datos de usuario y prestador si vienen en el payload
        @SuppressWarnings("unchecked")
        Map<String, Object> userData = extractMap(payload, "userData");
        @SuppressWarnings("unchecked")
        Map<String, Object> providerData = extractMap(payload, "providerData");

        // Guardar datos en BD
        if (userData != null && userId != null) {
            dataStorageService.saveUserData(userId, userData, coreMessage.getMessageId());
        }

        if (providerData != null && providerId != null) {
            dataStorageService.saveProviderData(providerId, providerData, coreMessage.getMessageId());
        }

        if (solicitudId != null) {
            dataStorageService.saveSolicitudData(
                solicitudId, userId, providerId, amount, currency, description,
                coreMessage.getMessageId(), status != null ? status : "PENDING"
            );
        }

        log.info("Datos guardados en BD - SolicitudId: {}, UserId: {}, ProviderId: {}",
            solicitudId, userId, providerId);

        // Crear pago si tenemos todos los datos
        if (solicitudId != null && userId != null && providerId != null) {
            createPaymentFromStoredData(solicitudId, userId, providerId, amount, currency, description, coreMessage.getMessageId());
        }
    }

    public void processUserDataFromCore(CoreEventMessage coreMessage) {
        log.info("Procesando datos de usuario del CORE - MessageId: {}", coreMessage.getMessageId());

        Map<String, Object> payload = coreMessage.getPayload();
        Long userId = extractLong(payload, "userId");
        
        if (userId != null) {
            dataStorageService.saveUserData(userId, payload, coreMessage.getMessageId());
            log.info("Datos de usuario guardados - UserId: {}", userId);
        }
    }

    public void processProviderDataFromCore(CoreEventMessage coreMessage) {
        log.info("Procesando datos de prestador del CORE - MessageId: {}", coreMessage.getMessageId());

        Map<String, Object> payload = coreMessage.getPayload();
        Long providerId = extractLong(payload, "providerId");
        
        if (providerId != null) {
            dataStorageService.saveProviderData(providerId, payload, coreMessage.getMessageId());
            log.info("Datos de prestador guardados - ProviderId: {}", providerId);
        }
    }

    public void processSolicitudDataFromCore(CoreEventMessage coreMessage) {
        log.info("Procesando datos de solicitud del CORE - MessageId: {}", coreMessage.getMessageId());

        Map<String, Object> payload = coreMessage.getPayload();
        Long solicitudId = extractLong(payload, "solicitudId");
        Long userId = extractLong(payload, "userId");
        Long providerId = extractLong(payload, "providerId");
        Double amount = extractDouble(payload, "amount");
        String currency = extractString(payload, "currency");
        String description = extractString(payload, "description");
        String status = extractString(payload, "status");

        if (solicitudId != null) {
            dataStorageService.saveSolicitudData(
                solicitudId, userId, providerId, amount, currency, description,
                coreMessage.getMessageId(), status != null ? status : "PENDING"
            );
            log.info("Datos de solicitud guardados - SolicitudId: {}", solicitudId);
        }
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

        // Construir metadata con el mismo formato que PaymentRequestProcessorService
        Map<String, Object> metadata = new HashMap<>();
        // Generar idCorrelacion a partir de solicitudId si no está disponible
        String idCorrelacion = "PED-" + (solicitudId != null ? solicitudId : "UNKNOWN");
        metadata.put("idCorrelacion", idCorrelacion);
        // MetodoPreferido no está disponible en este flujo, usar null o un valor por defecto
        metadata.put("metodoPreferido", null);

        // Solo guardar nombres y emails de usuario y prestador
        if (userData != null) {
            if (userData.containsKey("name")) {
                metadata.put("userName", userData.get("name"));
            }
            if (userData.containsKey("email")) {
                metadata.put("userEmail", userData.get("email"));
            }
        }
        if (providerData != null) {
            if (providerData.containsKey("name")) {
                metadata.put("providerName", providerData.get("name"));
            }
            if (providerData.containsKey("email")) {
                metadata.put("providerEmail", providerData.get("email"));
            }
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
            .timestamp(Instant.now().toString())
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

    private Double extractDouble(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) return null;
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        try {
            return Double.parseDouble(value.toString());
        } catch (NumberFormatException e) {
            log.warn("No se pudo convertir {} a Double: {}", key, value);
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
