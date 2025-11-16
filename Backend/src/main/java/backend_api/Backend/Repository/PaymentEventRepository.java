package backend_api.Backend.Repository;

import backend_api.Backend.Entity.payment.PaymentEvent;
import backend_api.Backend.Entity.payment.PaymentEventType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PaymentEventRepository extends JpaRepository<PaymentEvent, Long> {
    
    @Query("SELECT pe FROM PaymentEvent pe WHERE pe.paymentId = :paymentId ORDER BY pe.createdAt ASC")
    List<PaymentEvent> findByPaymentIdOrderByCreatedAt(@Param("paymentId") Long paymentId);
    
    List<PaymentEvent> findByTypeOrderByCreatedAtDesc(PaymentEventType type);
    
    @Query("SELECT pe FROM PaymentEvent pe WHERE pe.createdAt >= :since ORDER BY pe.createdAt DESC")
    List<PaymentEvent> findRecentEvents(@Param("since") LocalDateTime since);
    
    List<PaymentEvent> findByActorOrderByCreatedAtDesc(String actor);
    
    @Query("SELECT pe FROM PaymentEvent pe WHERE pe.paymentId = :paymentId AND pe.type = :type")
    List<PaymentEvent> findByPaymentIdAndType(@Param("paymentId") Long paymentId, @Param("type") PaymentEventType type);
    
    @Query("SELECT pe FROM PaymentEvent pe WHERE pe.correlationId = :correlationId")
    java.util.Optional<PaymentEvent> findByCorrelationId(@Param("correlationId") String correlationId);
    
    @Query("SELECT pe FROM PaymentEvent pe WHERE pe.correlationId = :correlationId AND pe.paymentId = :paymentId")
    java.util.Optional<PaymentEvent> findByCorrelationIdAndPaymentId(@Param("correlationId") String correlationId, @Param("paymentId") Long paymentId);
}
