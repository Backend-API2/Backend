package backend_api.Backend.Service.Interface;

import backend_api.Backend.DTO.refund.CreateRefundRequest;
import backend_api.Backend.Entity.refund.Refund;
import backend_api.Backend.Entity.refund.RefundStatus;

import java.util.List;
import java.util.Optional;

public interface RefundService {
    Refund createRefund(CreateRefundRequest request);
    Optional<Refund> getRefundById(Long id);
    List<Refund> getAllRefunds();
    Refund updateRefundStatus(Long id, RefundStatus status);
    List<Refund> getRefundsByPaymentId(Long paymentId);
    List<Refund> getRefundsByStatus(RefundStatus status);
}