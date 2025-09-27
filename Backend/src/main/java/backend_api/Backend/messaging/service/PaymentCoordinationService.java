package backend_api.Backend.messaging.service;

import backend_api.Backend.Entity.payment.Payment;
import backend_api.Backend.Entity.payment.PaymentStatus;
import backend_api.Backend.Service.Interface.PaymentService;
import backend_api.Backend.messaging.dto.PaymentCoordinationMessage;
import backend_api.Backend.messaging.dto.PaymentStatusUpdateMessage;
import backend_api.Backend.messaging.publisher.PaymentStatusPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class PaymentCoordinationService {

    private final PaymentService paymentService;
    private final PaymentStatusPublisher paymentStatusPublisher;

    @Transactional
    public Payment processPaymentCoordination(PaymentCoordinationMessage message) {
        log.info("Procesando coordinación de pago - MatchingId: {}", message.getMatchingId());

        Payment payment = createPaymentFromMessage(message);
        Payment savedPayment = paymentService.createPayment(payment);

        sendCoordinationConfirmation(savedPayment, message);

        log.info("Pago creado y confirmación enviada - PaymentId: {}, MatchingId: {}",
            savedPayment.getId(), message.getMatchingId());

        return savedPayment;
    }

    private Payment createPaymentFromMessage(PaymentCoordinationMessage message) {
        Payment payment = new Payment();
        payment.setUser_id(message.getUserId());
        payment.setProvider_id(message.getProviderId());
        payment.setAmount_total(message.getAmount());
        payment.setAmount_subtotal(message.getAmount());
        payment.setTaxes(BigDecimal.ZERO);
        payment.setFees(BigDecimal.ZERO);
        payment.setCurrency(message.getCurrency());
        payment.setStatus(PaymentStatus.PENDING_PAYMENT);
        payment.setCreated_at(LocalDateTime.now());
        payment.setUpdated_at(LocalDateTime.now());

        Map<String, Object> metadata = new HashMap<>();
        metadata.put("matchingId", message.getMatchingId());
        metadata.put("coordinatedAt", message.getCoordinatedAt());
        metadata.put("paymentMethodType", message.getPaymentMethodType());

        if (message.getSolicitudId() != null) {
            metadata.put("solicitudId", message.getSolicitudId());
        }
        if (message.getCotizacionId() != null) {
            metadata.put("cotizacionId", message.getCotizacionId());
        }
        if (message.getDescription() != null) {
            metadata.put("description", message.getDescription());
        }
        if (message.getMetadata() != null) {
            metadata.putAll(message.getMetadata());
        }

        payment.setMetadata(metadata);

        return payment;
    }

    private void sendCoordinationConfirmation(Payment payment, PaymentCoordinationMessage originalMessage) {
        PaymentStatusUpdateMessage confirmationMessage = new PaymentStatusUpdateMessage();
        confirmationMessage.setPaymentId(payment.getId());
        confirmationMessage.setMatchingId(originalMessage.getMatchingId());
        confirmationMessage.setOldStatus(null);
        confirmationMessage.setNewStatus(PaymentStatus.PENDING_PAYMENT);
        confirmationMessage.setReason("Payment coordination confirmed");
        confirmationMessage.setUpdatedAt(LocalDateTime.now());
        confirmationMessage.setAmountTotal(payment.getAmount_total());
        confirmationMessage.setCurrency(payment.getCurrency());

        Map<String, Object> additionalData = new HashMap<>();
        additionalData.put("coordinationConfirmed", true);
        additionalData.put("originalMatchingId", originalMessage.getMatchingId());
        confirmationMessage.setAdditionalData(additionalData);

        paymentStatusPublisher.publishPaymentCoordinationConfirmation(confirmationMessage);
    }
}