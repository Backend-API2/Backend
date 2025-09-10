package backend_api.Backend.Service.Interface;

import backend_api.Backend.Entity.refund.Refund;
import backend_api.Backend.Entity.refund.RefundStatus;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface RefundService {
    Refund createRefund(Refund refund);                   // crea refund en estado PENDING
    Optional<Refund> getRefundById(Long id);
    List<Refund> getAllRefunds();
    List<Refund> getRefundsByPaymentId(Long paymentId);
    List<Refund> getRefundsByStatus(RefundStatus status);
    Refund updateRefundStatus(Long id, RefundStatus status);

    BigDecimal getRefundedAmountForPayment(Long paymentId);   // suma PARTIAL_REFUND+TOTAL_REFUND
    BigDecimal getRemainingRefundable(Long paymentId);        // total pagado - reembolsado
}