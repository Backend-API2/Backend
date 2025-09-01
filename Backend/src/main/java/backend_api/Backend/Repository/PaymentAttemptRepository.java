package backend_api.Backend.Repository;

import backend_api.Backend.Entity.payment.PaymentAttempt;
import backend_api.Backend.Entity.payment.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentAttemptRepository extends JpaRepository<PaymentAttempt, Long> {
    
    List<PaymentAttempt> findByPaymentIdOrderByAttemptNumberDesc(Long paymentId);
    
    @Query("SELECT pa FROM PaymentAttempt pa WHERE pa.payment_id = :paymentId ORDER BY pa.attempt_number DESC LIMIT 1")
    Optional<PaymentAttempt> findLastAttemptByPaymentId(@Param("paymentId") Long paymentId);
    
    @Query("SELECT COUNT(pa) FROM PaymentAttempt pa WHERE pa.payment_id = :paymentId")
    Integer countByPaymentId(@Param("paymentId") Long paymentId);
    
    List<PaymentAttempt> findByPaymentIdAndStatus(Long paymentId, PaymentStatus status);
    
    @Query("SELECT pa FROM PaymentAttempt pa WHERE pa.payment_id = :paymentId AND pa.status = 'APPROVED'")
    Optional<PaymentAttempt> findSuccessfulAttemptByPaymentId(@Param("paymentId") Long paymentId);
}
