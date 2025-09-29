package backend_api.Backend.messaging.consumer;

import backend_api.Backend.messaging.config.QueueConfig;
import backend_api.Backend.messaging.dto.PaymentStatusUpdateMessage;
import backend_api.Backend.Entity.payment.PaymentEvent;
import backend_api.Backend.Entity.payment.PaymentEventType;
import backend_api.Backend.Repository.PaymentEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@Slf4j
@RequiredArgsConstructor
public class PaymentStatusUpdateConsumer {

    private final PaymentEventRepository paymentEventRepository;

    @RabbitListener(queues = QueueConfig.PAYMENT_STATUS_UPDATE_QUEUE)
    public void handlePaymentStatusUpdate(PaymentStatusUpdateMessage message) {
        try {
            log.info(" Recibido evento de actualización de estado - PaymentId: {}, MessageId: {}, Status: {} -> {}",
                message.getPaymentId(), message.getMessageId(), message.getOldStatus(), message.getNewStatus());

            PaymentEvent event = new PaymentEvent();
            event.setPaymentId(message.getPaymentId());
            event.setType(mapStatusToEventType(message.getNewStatus()));
            event.setPayload(String.format("{\"messageId\": \"%s\", \"oldStatus\": \"%s\", \"newStatus\": \"%s\", \"matchingId\": %s}",
                message.getMessageId(),
                message.getOldStatus(),
                message.getNewStatus(),
                message.getMatchingId() != null ? message.getMatchingId() : "null"));
            event.setActor("rabbitmq-system");
            event.setEventSource("rabbitmq-consumer");
            event.setCorrelationId(message.getMessageId());
            event.setMetadata(String.format("{\"timestamp\": \"%s\"}", message.getTimestamp()));
            event.setCreatedAt(LocalDateTime.now());

            PaymentEvent savedEvent = paymentEventRepository.save(event);

            log.info("Evento guardado en BD - EventId: {}, PaymentId: {}, Type: {}",
                savedEvent.getId(), savedEvent.getPaymentId(), savedEvent.getType());

        } catch (Exception e) {
            log.error("Error procesando actualización de estado - PaymentId: {}, MessageId: {}, Error: {}",
                message.getPaymentId(), message.getMessageId(), e.getMessage(), e);
            throw new RuntimeException("Error guardando evento en BD", e);
        }
    }

    private PaymentEventType mapStatusToEventType(backend_api.Backend.Entity.payment.PaymentStatus status) {
        switch (status) {
            case PENDING_PAYMENT:
            case PENDING_APPROVAL:
                return PaymentEventType.PAYMENT_PENDING;
            case APPROVED:
                return PaymentEventType.PAYMENT_APPROVED;
            case REJECTED:
                return PaymentEventType.PAYMENT_REJECTED;
            case CANCELLED:
                return PaymentEventType.PAYMENT_CANCELLED;
            case EXPIRED:
                return PaymentEventType.PAYMENT_EXPIRED;
            default:
                return PaymentEventType.PAYMENT_PENDING;
        }
    }
}