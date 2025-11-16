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
        try {
            log.info("Enviando mensaje al CORE vía HTTP - MessageId: {}, EventName: {}",
                message.getMessageId(),
                message.getDestination() != null ? message.getDestination().getEventName() : "unknown");

            coreHubService.publishMessage(message);
            log.info("✅ Mensaje enviado exitosamente al CORE - MessageId: {}", message.getMessageId());
        } catch (Exception e) {
            log.error("⚠️ Error enviando mensaje al CORE - MessageId: {}, Error: {}",
                message.getMessageId(), e.getMessage(), e);
            // No lanzar excepción para no interrumpir el flujo de negocio
        }
    }
}
