// src/main/java/backend_api/Backend/messaging/publisher/PaymentMethodSelectedPublisher.java
package backend_api.Backend.messaging.publisher;

import backend_api.Backend.messaging.config.QueueConfig;
import backend_api.Backend.messaging.dto.PaymentMethodSelectedMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class PaymentMethodSelectedPublisher {

    private final AmqpTemplate rabbitTemplate;

    public void publish(PaymentMethodSelectedMessage msg) {
        log.info("Enviando evento de método seleccionado - paymentId={}, type={}",
                msg.getPaymentId(), msg.getMethodType());

        rabbitTemplate.convertAndSend(
                QueueConfig.PAYMENT_EXCHANGE,
                "payment.method.selected",
                msg
        );
    }
}