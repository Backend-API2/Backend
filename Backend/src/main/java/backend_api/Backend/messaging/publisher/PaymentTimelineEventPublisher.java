// src/main/java/backend_api/Backend/messaging/publisher/PaymentTimelineEventPublisher.java
package backend_api.Backend.messaging.publisher;

import backend_api.Backend.Entity.payment.PaymentEvent;
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
public class PaymentTimelineEventPublisher {

    private final CoreHubService coreHubService;

    public void publish(PaymentEvent event) {
        try {
            CoreResponseMessage coreMessage = CoreResponseMessage.builder()
                .messageId(UUID.randomUUID().toString())
                .timestamp(Instant.now().toString())
                .source("payments")
                .destination(CoreResponseMessage.Destination.builder()
                    .channel("payments.payment.timeline_event")
                    .eventName("timeline_event")
                    .build())
                .payload(createTimelineEventPayload(event))
                .build();

            coreHubService.publishMessage(coreMessage);
            log.debug("Timeline event enviado - paymentId={}, type={}", event.getPaymentId(), event.getType());
        } catch (Exception e) {
            log.error("Error al publicar timeline event: {}", e.getMessage(), e);
        }
    }

    private Map<String, Object> createTimelineEventPayload(PaymentEvent event){
        Map<String, Object> payload = new HashMap<>();
        payload.put("paymentId", event.getPaymentId());
        payload.put("eventType", event.getType());
        payload.put("actor", event.getActor());
        payload.put("description", event.getDescription());
        payload.put("createdAt", event.getCreatedAt());
        return payload;
    }
}