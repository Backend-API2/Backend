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
public class CoreUserProviderDataConsumer {

    private final CoreEventProcessorService coreEventProcessorService;

    @RabbitListener(queues = QueueConfig.CORE_USER_PROVIDER_DATA_QUEUE)
    public void handleUserProviderDataFromCore(CoreEventMessage message) {
        try {
            log.info("Recibidos datos de usuario/prestador del CORE - MessageId: {}",
                message.getMessageId());

            coreEventProcessorService.processUserProviderDataFromCore(message);

            log.info("Datos procesados exitosamente - MessageId: {}", message.getMessageId());

        } catch (Exception e) {
            log.error("Error procesando datos del CORE - MessageId: {}, Error: {}",
                message.getMessageId(), e.getMessage(), e);
            throw e;
        }
    }
}
