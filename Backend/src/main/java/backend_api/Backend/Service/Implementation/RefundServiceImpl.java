// backend_api/Backend/Service/Implementation/RefundServiceImpl.java
package backend_api.Backend.Service.Implementation;

import backend_api.Backend.DTO.refund.CreateRefundRequest;
import backend_api.Backend.Entity.payment.Payment;
import backend_api.Backend.Entity.payment.PaymentEventType;
import backend_api.Backend.Entity.payment.PaymentStatus;
import backend_api.Backend.Entity.refund.Refund;
import backend_api.Backend.Entity.refund.RefundStatus;
import backend_api.Backend.Repository.PaymentRepository;
import backend_api.Backend.Repository.RefundRepository;
import backend_api.Backend.Service.Interface.PaymentEventService;
import backend_api.Backend.Service.Interface.RefundService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class RefundServiceImpl implements RefundService {

    private final RefundRepository refundRepository;
    private final PaymentRepository paymentRepository;
    private final PaymentEventService paymentEventService;

    @Override
    public Refund createRefund(CreateRefundRequest request, Long requesterUserId) {
        Payment payment = paymentRepository.findById(request.getPaymentId())
                .orElseThrow(() -> new EntityNotFoundException("Pago no encontrado: " + request.getPaymentId()));

        // Solo el dueño del pago puede pedirlo
        if (!payment.getUser_id().equals(requesterUserId)) {
            throw new IllegalStateException("El pago no pertenece al usuario autenticado.");
        }

        // Estados NO reembolsables (idéntico a lo que ya tenías)
        if (payment.getStatus() == PaymentStatus.CANCELLED ||
                payment.getStatus() == PaymentStatus.REJECTED ||
                payment.getStatus() == PaymentStatus.PENDING_APPROVAL ||
                payment.getStatus() == PaymentStatus.PENDING_PAYMENT ||
                payment.getStatus() == PaymentStatus.EXPIRED) {
            throw new IllegalStateException("El pago no es reembolsable en su estado actual: " + payment.getStatus());
        }

        // Monto disponible = total - ya reembolsado
        BigDecimal yaReembolsado = refundRepository
                .sumAmountByPaymentIdAndStatuses(payment.getId(),
                        List.of(RefundStatus.PARTIAL_REFUND, RefundStatus.TOTAL_REFUND));
        if (yaReembolsado == null) yaReembolsado = BigDecimal.ZERO;

        BigDecimal disponible = payment.getAmount_total().subtract(yaReembolsado);
        if (request.getAmount().compareTo(disponible) > 0) {
            throw new IllegalArgumentException("El monto excede lo disponible para reembolso: " + disponible);
        }

        // Solo dejamos el request en PENDING (esperando que el merchant acepte/rechace)
        Refund refund = new Refund();
        refund.setPayment_id(payment.getId());
        refund.setAmount(request.getAmount());
        refund.setReason(request.getReason());
        refund.setStatus(RefundStatus.PENDING);
        refund.setRequestedBy(requesterUserId);
        refund = refundRepository.save(refund);

        paymentEventService.createEvent(
                payment.getId(),
                PaymentEventType.REFUND_INITIATED,
                String.format("{\"refund_id\": %d, \"amount\": %s}", refund.getId(), refund.getAmount()),
                "user_" + requesterUserId
        );

        return refund;
    }

    @Override
    public Refund approveRefund(Long refundId, Long merchantUserId, String message) {
        Refund refund = refundRepository.findById(refundId)
                .orElseThrow(() -> new EntityNotFoundException("Refund no encontrado: " + refundId));

        Payment payment = paymentRepository.findById(refund.getPayment_id())
                .orElseThrow(() -> new EntityNotFoundException("Pago no encontrado para el refund: " + refund.getPayment_id()));

        // El merchant que aprueba debe ser el dueño del payment (provider)
        if (!payment.getProvider_id().equals(merchantUserId)) {
            throw new IllegalStateException("No tienes permisos para aprobar este refund.");
        }

        // Marcamos aprobación y simulamos ejecución inmediata
        refund.setStatus(RefundStatus.APPROVED);
        refund.setReviewedBy(merchantUserId);
        refund.setReviewedAt(LocalDateTime.now());
        refund.setDecisionMessage(message);
        refundRepository.save(refund);

        // "Procesamiento" (simulado)
        BigDecimal yaReembolsado = refundRepository
                .sumAmountByPaymentIdAndStatuses(payment.getId(),
                        List.of(RefundStatus.PARTIAL_REFUND, RefundStatus.TOTAL_REFUND));
        if (yaReembolsado == null) yaReembolsado = BigDecimal.ZERO;

        BigDecimal disponible = payment.getAmount_total().subtract(yaReembolsado);
        BigDecimal restante = disponible.subtract(refund.getAmount());
        boolean esTotal = restante.compareTo(BigDecimal.ZERO) == 0;

        refund.setGateway_refund_id("RF-" + System.currentTimeMillis());
        refund.setStatus(esTotal ? RefundStatus.TOTAL_REFUND : RefundStatus.PARTIAL_REFUND);
        refundRepository.save(refund);

        if (esTotal) {
            payment.setStatus(PaymentStatus.REFUNDED);
            payment.setRefund_id(refund.getId());
            payment.setUpdated_at(LocalDateTime.now());
            paymentRepository.save(payment);
        }

        paymentEventService.createEvent(
                payment.getId(),
                PaymentEventType.REFUND_COMPLETED,
                String.format("{\"refund_id\": %d, \"status\": \"%s\"}", refund.getId(), refund.getStatus()),
                "merchant_" + merchantUserId
        );

        return refund;
    }

    @Override
    public Refund declineRefund(Long refundId, Long merchantUserId, String message) {
        Refund refund = refundRepository.findById(refundId)
                .orElseThrow(() -> new EntityNotFoundException("Refund no encontrado: " + refundId));

        Payment payment = paymentRepository.findById(refund.getPayment_id())
                .orElseThrow(() -> new EntityNotFoundException("Pago no encontrado para el refund: " + refund.getPayment_id()));

        if (!payment.getProvider_id().equals(merchantUserId)) {
            throw new IllegalStateException("No tienes permisos para rechazar este refund.");
        }

        refund.setStatus(RefundStatus.DECLINED);
        refund.setReviewedBy(merchantUserId);
        refund.setReviewedAt(LocalDateTime.now());
        refund.setDecisionMessage(message);
        refundRepository.save(refund);

        paymentEventService.createEvent(
                payment.getId(),
                PaymentEventType.REFUND_FAILED, // o crea REFUND_DECLINED si querés
                String.format("{\"refund_id\": %d, \"status\": \"DECLINED\", \"message\": \"%s\"}", refund.getId(), message),
                "merchant_" + merchantUserId
        );

        return refund;
    }

    // --- lo restante igual que ya lo tenías ---
    @Override @Transactional(readOnly = true)
    public Optional<Refund> getRefundById(Long id) { return refundRepository.findById(id); }

    @Override @Transactional(readOnly = true)
    public List<Refund> getAllRefunds() { return refundRepository.findAll(); }

    @Override
    public Refund updateRefundStatus(Long id, RefundStatus status) {
        Refund refund = refundRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Refund no encontrado: " + id));
        refund.setStatus(status);
        return refundRepository.save(refund);
    }

    @Override @Transactional(readOnly = true)
    public List<Refund> getRefundsByPaymentId(Long paymentId) { return refundRepository.findByPayment_id(paymentId); }

    @Override @Transactional(readOnly = true)
    public List<Refund> getRefundsByStatus(RefundStatus status) { return refundRepository.findByStatus(status); }
}