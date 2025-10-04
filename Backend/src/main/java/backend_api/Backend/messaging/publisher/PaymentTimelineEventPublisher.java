// src/main/java/backend_api/Backend/messaging/publisher/PaymentTimelineEventPublisher.java
package backend_api.Backend.messaging.publisher;

import backend_api.Backend.messaging.config.QueueConfig;
import backend_api.Backend.Entity.payment.PaymentEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class PaymentTimelineEventPublisher {

    private final AmqpTemplate rabbitTemplate;

    public void publish(PaymentEvent event) {
        // event ya tiene type, actor, payload, metadata, etc.
        rabbitTemplate.convertAndSend(
                QueueConfig.PAYMENT_EXCHANGE,
                "payment.timeline.event",
                event
        );
        log.debug("Timeline event enviado - paymentId={}, type={}", event.getPaymentId(), event.getType());
    }
}