package backend_api.Backend.Repository;

import backend_api.Backend.Entity.refund.Refund;
import backend_api.Backend.Entity.refund.RefundStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface RefundRepository extends JpaRepository<Refund, Long> {

    @Query("SELECT r FROM Refund r WHERE r.paymend_id = :paymentId")
    List<Refund> findByPaymendId(@Param("paymentId") Long paymentId);

    List<Refund> findByStatus(RefundStatus status);

    @Query("SELECT r FROM Refund r WHERE r.gateway_refund_id = :gatewayRefundId")
    Optional<Refund> findByGatewayRefundId(@Param("gatewayRefundId") String gatewayRefundId);

    @Query("SELECT r FROM Refund r WHERE r.created_at BETWEEN :startDate AND :endDate")
    List<Refund> findByCreatedAtBetween(@Param("startDate") LocalDateTime startDate,
                                        @Param("endDate") LocalDateTime endDate);

    @Query("SELECT r FROM Refund r WHERE r.amount >= :minAmount")
    List<Refund> findByAmountGreaterThanEqual(@Param("minAmount") BigDecimal minAmount);

    @Query("SELECT r FROM Refund r WHERE r.paymend_id = :paymentId AND r.status = :status")
    List<Refund> findByPaymentIdAndStatus(@Param("paymentId") Long paymentId,
                                          @Param("status") RefundStatus status);

    // NUEVO: suma total por payment para ciertos estados (PENDING/COMPLETED)
    @Query("SELECT COALESCE(SUM(r.amount), 0) FROM Refund r " +
            "WHERE r.paymend_id = :paymentId AND r.status IN :statuses")
    BigDecimal sumAmountByPaymentIdAndStatuses(@Param("paymentId") Long paymentId,
                                               @Param("statuses") List<RefundStatus> statuses);
}