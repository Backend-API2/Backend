package backend_api.Backend.messaging.service;

import backend_api.Backend.messaging.dto.*;
import backend_api.Backend.messaging.publisher.CoreEventPublisher;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class CoreEventProcessorService {

    private final CoreEventPublisher coreEventPublisher;
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
            .source("PAYMENTS_TEAM")
            .destination(CoreResponseMessage.Destination.builder()
                .channel("CORE.DISTRIBUTION.REQUEST")
                .eventName("REQUEST_USER_PROVIDER_DATA")
                .build())
            .payload(payloadMap)
            .build();

        coreEventPublisher.publishToCore(response);
        log.info("IDs enviados al CORE para distribución - SolicitudId: {}", solicitudId);
    }

    public void processUserProviderDataFromCore(CoreEventMessage coreMessage) {
        log.info("Recibido datos de usuario/prestador del CORE - MessageId: {}", coreMessage.getMessageId());

        // Aquí procesarías los datos completos del usuario y prestador
        // que te devuelve el CORE después de consultar a los otros módulos
        Map<String, Object> payload = coreMessage.getPayload();

        log.info("Datos recibidos del CORE: {}", payload);

        // TODO: Implementar lógica para crear el pago con los datos completos
        // Ejemplo:
        // - Extraer userData y providerData del payload
        // - Crear Payment con toda la información
        // - Guardar en BD
        // - Enviar confirmación al CORE si es necesario
    }
}
