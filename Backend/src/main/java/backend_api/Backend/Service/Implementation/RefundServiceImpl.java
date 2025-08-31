package backend_api.Backend.Service.Implementation;

import backend_api.Backend.Entity.payment.Payment;
import backend_api.Backend.Entity.payment.PaymentStatus;
import backend_api.Backend.Entity.refund.Refund;
import backend_api.Backend.Entity.refund.RefundStatus;
import backend_api.Backend.Repository.PaymentRepository;
import backend_api.Backend.Repository.RefundRepository;
import backend_api.Backend.Service.Interface.RefundService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;

@Service
public class RefundServiceImpl implements RefundService {

    @Autowired
    private RefundRepository refundRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Override
    public Refund createRefund(Refund refund) {
        if (refund.getPaymend_id() == null) {
            throw new IllegalArgumentException("payment_id es requerido");
        }
        if (refund.getAmount() == null || refund.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("El monto del refund debe ser mayor a 0");
        }

        Payment payment = paymentRepository.findById(refund.getPaymend_id())
                .orElseThrow(() -> new RuntimeException("Pago no encontrado con id: " + refund.getPaymend_id()));

        // Solo pagos aprobados (o ya parcialmente reembolsados) pueden reembolsarse
        if (!(payment.getStatus() == PaymentStatus.APPROVED || payment.getStatus() == PaymentStatus.REFUNDED)) {
            throw new IllegalStateException("El pago no está en estado válido para refund (APPROVED/REFUNDED)");
        }

        BigDecimal remaining = getRemainingRefundable(payment.getId());
        if (refund.getAmount().compareTo(remaining) > 0) {
            throw new IllegalArgumentException("El monto solicitado excede el saldo reembolsable. Restante: " + remaining);
        }

        refund.setStatus(RefundStatus.PENDING);
        refund.setCreated_at(LocalDateTime.now());

        return refundRepository.save(refund);
    }

    @Override
    public Optional<Refund> getRefundById(Long id) {
        return refundRepository.findById(id);
    }

    @Override
    public List<Refund> getAllRefunds() {
        return refundRepository.findAll();
    }

    @Override
    public List<Refund> getRefundsByPaymentId(Long paymentId) {
        return refundRepository.findByPaymendId(paymentId);
    }

    @Override
    public List<Refund> getRefundsByStatus(RefundStatus status) {
        return refundRepository.findByStatus(status);
    }

    @Override
    public Refund updateRefundStatus(Long id, RefundStatus status) {
        Refund existing = refundRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Refund no encontrado con id: " + id));

        existing.setStatus(status);
        Refund saved = refundRepository.save(existing);

        // Si se completó, revisar si el pago quedó totalmente reembolsado
        if (status == RefundStatus.COMPLETED) {
            Payment payment = paymentRepository.findById(existing.getPaymend_id())
                    .orElseThrow(() -> new RuntimeException("Pago no encontrado con id: " + existing.getPaymend_id()));

            BigDecimal remaining = getRemainingRefundable(payment.getId());
            if (remaining.compareTo(BigDecimal.ZERO) == 0) {
                payment.setStatus(PaymentStatus.REFUNDED);
                payment.setUpdated_at(LocalDateTime.now());
                paymentRepository.save(payment);
            }
        }

        return saved;
    }

    @Override
    public BigDecimal getRefundedAmountForPayment(Long paymentId) {
        return refundRepository.sumAmountByPaymentIdAndStatuses(
                paymentId,
                List.copyOf(EnumSet.of(RefundStatus.PENDING, RefundStatus.COMPLETED))
        );
    }

    @Override
    public BigDecimal getRemainingRefundable(Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Pago no encontrado con id: " + paymentId));

        BigDecimal already = getRefundedAmountForPayment(paymentId);
        BigDecimal total = payment.getAmount_total() != null ? payment.getAmount_total() : BigDecimal.ZERO;

        BigDecimal remaining = total.subtract(already);
        return remaining.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : remaining;
    }
}