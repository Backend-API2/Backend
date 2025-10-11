// src/main/java/backend_api/Backend/messaging/publisher/PaymentCoordinationPublisher.java
package backend_api.Backend.messaging.publisher;

import backend_api.Backend.messaging.dto.PaymentCoordinationMessage;
import backend_api.Backend.messaging.service.CoreHubService;
import backend_api.Backend.messaging.dto.CoreResponseMessage;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.HashMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class PaymentCoordinationPublisher {

    private final CoreHubService coreHubService;

    public void publishCoordination(PaymentCoordinationMessage message) {
        try {
            log.info("Enviando coordinación de pago - matchingId={}, userId={}, providerId={}",
                message.getMatchingId(), message.getUserId(), message.getProviderId());

            CoreResponseMessage coreMessage = CoreResponseMessage.builder()
                .messageId(UUID.randomUUID().toString())
                .timestamp(Instant.now().toString())
                .source("payments")
                .destination(CoreResponseMessage.Destination.builder()
                    .channel("payments.payment.coordination")
                    .eventName("coordination")
                    .build())
                .payload(createCoordinationPayload(message))
                .build();
            coreHubService.publishMessage(coreMessage);
            log.info("Coordinacion enviada exitosamente - MatchingId: {}", message.getMatchingId());
        } catch (Exception e) {
            log.error("Error al enviar coordinación de pago - MatchingId: {}, Error: {}",
                message.getMatchingId(), e.getMessage(), e);
            throw new RuntimeException("Error al enviar coordinación de pago", e);
        }
    }

    private Map<String, Object> createCoordinationPayload(PaymentCoordinationMessage message) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("matchingId", message.getMatchingId());
        payload.put("userId", message.getUserId());
        payload.put("providerId", message.getProviderId());
        payload.put("amount", message.getAmount());
        payload.put("currency", message.getCurrency());
        payload.put("description", message.getDescription());
        payload.put("createdAt", message.getCreatedAt());
        return payload;
    }
}