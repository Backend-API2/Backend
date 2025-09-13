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

    // Por payment_id
    @Query("SELECT r FROM Refund r WHERE r.payment_id = :paymentId")
    List<Refund> findByPayment_id(@Param("paymentId") Long paymentId);

    // Por estado
    List<Refund> findByStatus(RefundStatus status);

    // Por gateway_refund_id
    @Query("SELECT r FROM Refund r WHERE r.gateway_refund_id = :gatewayRefundId")
    Optional<Refund> findByGatewayRefundId(@Param("gatewayRefundId") String gatewayRefundId);

    // Por rango de fechas en created_at
    @Query("SELECT r FROM Refund r WHERE r.created_at BETWEEN :startDate AND :endDate")
    List<Refund> findByCreatedAtBetween(@Param("startDate") LocalDateTime startDate,
                                        @Param("endDate") LocalDateTime endDate);

    // Amount >= min
    @Query("SELECT r FROM Refund r WHERE r.amount >= :minAmount")
    List<Refund> findByAmountGreaterThanEqual(@Param("minAmount") BigDecimal minAmount);

    // Por payment_id y estado
    @Query("SELECT r FROM Refund r WHERE r.payment_id = :paymentId AND r.status = :status")
    List<Refund> findByPayment_idAndStatus(@Param("paymentId") Long paymentId,
                                           @Param("status") RefundStatus status);

    // Suma total por payment para ciertos estados (PARTIAL_REFUND/TOTAL_REFUND)
    @Query("""
           SELECT COALESCE(SUM(r.amount), 0)
           FROM Refund r
           WHERE r.payment_id = :paymentId
             AND r.status IN :statuses
           """)
    BigDecimal sumAmountByPaymentIdAndStatuses(@Param("paymentId") Long paymentId,
                                               @Param("statuses") List<RefundStatus> statuses);

    @Query("""
           SELECT COUNT(r) > 0
           FROM Refund r
           WHERE r.payment_id = :paymentId
             AND r.status != 'DECLINED'
           """)
    boolean existsActiveRefundForPayment(@Param("paymentId") Long paymentId);
}