package backend_api.Backend.messaging.publisher;

import backend_api.Backend.messaging.config.QueueConfig;
import backend_api.Backend.messaging.dto.PaymentStatusUpdateMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class PaymentStatusPublisher {

    private final AmqpTemplate rabbitTemplate;

    public void publishPaymentStatusUpdate(PaymentStatusUpdateMessage message) {
        try {
            log.info("Enviando actualización de estado - PaymentId: {}, Status: {} -> {}",
                message.getPaymentId(), message.getOldStatus(), message.getNewStatus());

            rabbitTemplate.convertAndSend(
                QueueConfig.PAYMENT_EXCHANGE,
                "payment.status.update",
                message
            );

            log.info("Mensaje enviado exitosamente - PaymentId: {}, MessageId: {}",
                message.getPaymentId(), message.getMessageId());

        } catch (Exception e) {
            log.error("Error enviando actualización de estado - PaymentId: {}, Error: {}",
                message.getPaymentId(), e.getMessage(), e);
            throw new RuntimeException("Error al enviar mensaje de actualización", e);
        }
    }

    public void publishPaymentCoordinationConfirmation(PaymentStatusUpdateMessage message) {
        try {
            log.info("Enviando confirmación de coordinación - PaymentId: {}, MatchingId: {}",
                message.getPaymentId(), message.getMatchingId());

            rabbitTemplate.convertAndSend(
                QueueConfig.PAYMENT_EXCHANGE,
                "payment.coordination.confirmed",
                message
            );

            log.info("Confirmación enviada exitosamente - PaymentId: {}", message.getPaymentId());

        } catch (Exception e) {
            log.error("Error enviando confirmación - PaymentId: {}, Error: {}",
                message.getPaymentId(), e.getMessage(), e);
            throw new RuntimeException("Error al enviar confirmación", e);
        }
    }
}