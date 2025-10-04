package backend_api.Backend.messaging.consumer;

import backend_api.Backend.messaging.config.QueueConfig;
import backend_api.Backend.messaging.dto.CoreEventMessage;
import backend_api.Backend.messaging.service.CoreEventProcessorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class CorePaymentRequestConsumer {

    private final CoreEventProcessorService coreEventProcessorService;

    @RabbitListener(queues = QueueConfig.CORE_PAYMENT_REQUEST_QUEUE)
    public void handlePaymentRequestFromCore(CoreEventMessage message) {
        try {
            log.info("Recibida solicitud de pago del CORE - MessageId: {}, Source: {}",
                message.getMessageId(), message.getSource());

            coreEventProcessorService.processPaymentRequestFromCore(message);

            log.info("Solicitud procesada exitosamente - MessageId: {}", message.getMessageId());

        } catch (Exception e) {
            log.error("Error procesando solicitud del CORE - MessageId: {}, Error: {}",
                message.getMessageId(), e.getMessage(), e);
            throw e;
        }
    }
}
