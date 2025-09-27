package backend_api.Backend.messaging.service;

import backend_api.Backend.messaging.dto.BaseMessage;
import backend_api.Backend.messaging.dto.PaymentCoordinationMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

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

        if (message instanceof PaymentCoordinationMessage) {
            validatePaymentCoordinationMessage((PaymentCoordinationMessage) message);
        }
    }

    private void validatePaymentCoordinationMessage(PaymentCoordinationMessage message) {
        if (message.getMatchingId() == null) {
            throw new IllegalArgumentException("El matchingId es requerido");
        }

        if (message.getUserId() == null) {
            throw new IllegalArgumentException("El userId es requerido");
        }

        if (message.getProviderId() == null) {
            throw new IllegalArgumentException("El providerId es requerido");
        }

        if (message.getAmount() == null || message.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("El amount debe ser mayor a cero");
        }

        if (message.getCurrency() == null || message.getCurrency().trim().isEmpty()) {
            throw new IllegalArgumentException("La currency es requerida");
        }

        if (message.getCoordinatedAt() == null) {
            throw new IllegalArgumentException("El coordinatedAt es requerido");
        }

        log.debug("Mensaje validado exitosamente - MatchingId: {}", message.getMatchingId());
    }
}