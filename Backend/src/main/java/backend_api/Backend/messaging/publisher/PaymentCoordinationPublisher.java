// src/main/java/backend_api/Backend/messaging/publisher/PaymentCoordinationPublisher.java
package backend_api.Backend.messaging.publisher;

import backend_api.Backend.messaging.config.QueueConfig;
import backend_api.Backend.messaging.dto.PaymentCoordinationMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class PaymentCoordinationPublisher {

    private final AmqpTemplate rabbitTemplate;

    public void publishCoordination(PaymentCoordinationMessage message) {
        log.info("Enviando coordinación de pago - matchingId={}, userId={}, providerId={}",
                message.getMatchingId(), message.getUserId(), message.getProviderId());

        rabbitTemplate.convertAndSend(
                QueueConfig.PAYMENT_EXCHANGE,
                "payment.coordination",
                message
        );
    }
}