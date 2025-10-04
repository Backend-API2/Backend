package backend_api.Backend.messaging.publisher;

import backend_api.Backend.messaging.dto.CoreResponseMessage;
import backend_api.Backend.messaging.service.CoreHubService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class CoreEventPublisher {

    private final CoreHubService coreHubService;

    public void publishToCore(CoreResponseMessage message) {
        log.info("Enviando mensaje al CORE vía HTTP - MessageId: {}, EventName: {}",
            message.getMessageId(),
            message.getDestination().getEventName());

        coreHubService.publishMessage(message);
    }
}
