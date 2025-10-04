// src/main/java/backend_api/Backend/messaging/consumer/PaymentCommandConsumer.java
package backend_api.Backend.messaging.consumer;

import backend_api.Backend.Entity.payment.Payment;
import backend_api.Backend.Entity.payment.PaymentStatus;
import backend_api.Backend.Service.Interface.PaymentService;
import backend_api.Backend.messaging.config.QueueConfig;
import backend_api.Backend.messaging.dto.PaymentCommandMessage;
import backend_api.Backend.messaging.dto.PaymentStatusUpdateMessage;
import backend_api.Backend.messaging.publisher.PaymentStatusPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@Slf4j
@RequiredArgsConstructor
public class PaymentCommandConsumer {

    private final PaymentService paymentService;
    private final PaymentStatusPublisher statusPublisher;

    @RabbitListener(queues = QueueConfig.PAYMENT_COMMAND_QUEUE)
    public void handle(PaymentCommandMessage cmd) {
        try {
            log.info("Comando recibido - paymentId={}, command={}", cmd.getPaymentId(), cmd.getCommand());

            Payment payment = paymentService.getPaymentById(cmd.getPaymentId())
                    .orElseThrow(() -> new IllegalArgumentException("Pago no encontrado"));

            PaymentStatus old = payment.getStatus();
            PaymentStatus next;

            switch (cmd.getCommand()) {
                case "APPROVE":
                    next = PaymentStatus.APPROVED;
                    break;
                case "REJECT":
                    next = PaymentStatus.REJECTED;
                    break;
                case "CANCEL":
                    next = PaymentStatus.CANCELLED;
                    break;
                case "CAPTURE":
                    // si tu modelo diferencia AUTHORIZED/CAPTURED, ajustalo; aquí lo tratamos como APPROVED.
                    next = PaymentStatus.APPROVED;
                    break;
                default:
                    throw new IllegalArgumentException("Comando no soportado: " + cmd.getCommand());
            }

            paymentService.updatePaymentStatus(payment.getId(), next);

            // Notificar por el canal estándar de updates
            PaymentStatusUpdateMessage msg = new PaymentStatusUpdateMessage();
            msg.setPaymentId(payment.getId());
            msg.setOldStatus(old);
            msg.setNewStatus(next);
            msg.setReason(cmd.getReason());
            msg.setUpdatedAt(LocalDateTime.now());
            statusPublisher.publishPaymentStatusUpdate(msg);

            log.info("Comando procesado OK - paymentId={}, {} -> {}", payment.getId(), old, next);
        } catch (Exception e) {
            log.error("Error procesando comando - paymentId={}, err={}", cmd.getPaymentId(), e.getMessage(), e);
            throw e;
        }
    }
}