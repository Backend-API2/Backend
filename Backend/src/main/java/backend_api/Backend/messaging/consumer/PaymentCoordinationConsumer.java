package backend_api.Backend.messaging.consumer;

import backend_api.Backend.messaging.config.QueueConfig;
import backend_api.Backend.messaging.dto.PaymentCoordinationMessage;
import backend_api.Backend.messaging.service.MessageHandlerService;
import backend_api.Backend.messaging.service.PaymentCoordinationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class PaymentCoordinationConsumer {

    private final PaymentCoordinationService paymentCoordinationService;
    private final MessageHandlerService messageHandlerService;

    @RabbitListener(queues = QueueConfig.PAYMENT_COORDINATION_QUEUE)
    public void handlePaymentCoordination(PaymentCoordinationMessage message) {
        try {
            log.info("Recibido mensaje de coordinación de pago - MatchingId: {}, MessageId: {}",
                message.getMatchingId(), message.getMessageId());

            messageHandlerService.validateMessage(message);
            paymentCoordinationService.processPaymentCoordination(message);

            log.info("Pago coordinado exitosamente - MatchingId: {}", message.getMatchingId());

        } catch (IllegalArgumentException e) {
            log.error("Error de validación en mensaje - MatchingId: {}, Error: {}",
                message.getMatchingId(), e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Error procesando coordinación de pago - MatchingId: {}, Error: {}",
                message.getMatchingId(), e.getMessage(), e);
            throw e;
        }
    }
}