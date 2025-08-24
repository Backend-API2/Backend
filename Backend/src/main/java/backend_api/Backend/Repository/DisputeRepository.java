package backend_api.Backend.Repository;

import backend_api.Backend.Entity.dispute.Dispute;
import backend_api.Backend.Entity.dispute.DisputeStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface DisputeRepository extends JpaRepository<Dispute, Long> {
    
    List<Dispute> findByPaymentId(Long paymentId);
    
    List<Dispute> findByStatus(DisputeStatus status);
    
    @Query("SELECT d FROM Dispute d WHERE d.created_at BETWEEN :startDate AND :endDate")
    List<Dispute> findByCreatedAtBetween(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT d FROM Dispute d WHERE d.amount_provisioned >= :minAmount")
    List<Dispute> findByAmountProvisionedGreaterThanEqual(@Param("minAmount") BigDecimal minAmount);
    
    @Query("SELECT d FROM Dispute d WHERE d.payment_id = :paymentId AND d.status = :status")
    List<Dispute> findByPaymentIdAndStatus(@Param("paymentId") Long paymentId, @Param("status") DisputeStatus status);
}
