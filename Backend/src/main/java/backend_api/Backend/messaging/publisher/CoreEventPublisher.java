package backend_api.Backend.messaging.publisher;

import backend_api.Backend.messaging.config.QueueConfig;
import backend_api.Backend.messaging.dto.CoreResponseMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class CoreEventPublisher {

    private final AmqpTemplate rabbitTemplate;

    public void publishToCore(CoreResponseMessage message) {
        log.info("Enviando mensaje al CORE - MessageId: {}, EventName: {}",
            message.getMessageId(),
            message.getDestination().getEventName());

        rabbitTemplate.convertAndSend(
            QueueConfig.PAYMENT_EXCHANGE,
            "core.event.response",
            message
        );
    }
}
