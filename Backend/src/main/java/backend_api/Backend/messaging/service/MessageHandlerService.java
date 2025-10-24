package backend_api.Backend.messaging.service;

import backend_api.Backend.messaging.dto.BaseMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class MessageHandlerService {

    public void validateMessage(BaseMessage message) {
        if (message == null) {
            throw new IllegalArgumentException("El mensaje no puede ser nulo");
        }

        if (message.getMessageId() == null || message.getMessageId().trim().isEmpty()) {
            throw new IllegalArgumentException("El messageId es requerido");
        }

        if (message.getTimestamp() == null) {
            throw new IllegalArgumentException("El timestamp es requerido");
        }

        // Validación básica completada
    }

}