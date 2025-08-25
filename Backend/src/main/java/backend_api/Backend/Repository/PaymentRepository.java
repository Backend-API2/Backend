package backend_api.Backend.Repository;


import backend_api.Backend.Entity.payment.Payment;
import backend_api.Backend.Entity.payment.PaymentMethod;
import backend_api.Backend.Entity.payment.PaymentStatus;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    @Query("SELECT p FROM Payment p WHERE p.payment_intent_id = :paymentIntentId")
    Optional<Payment> findByPaymentIntentId(@Param("paymentIntentId") String paymentIntentId);

    @Query("SELECT p FROM Payment p WHERE p.user_id = :userId")
    List<Payment> findByUserId(@Param("userId") Long userId);

    @Query("SELECT p FROM Payment p WHERE p.provider_id = :providerId")
    List<Payment> findByProviderId(@Param("providerId") Long providerId);

    List<Payment> findByStatus(PaymentStatus status);

    List<Payment> findByMethod(PaymentMethod method);

    @Query("SELECT p FROM Payment p WHERE p.gateway_txn_id = :gatewayTxnId")
    Optional<Payment> findByGatewayTxnId(@Param("gatewayTxnId") String gatewayTxnId);

    @Query("SELECT p FROM Payment p WHERE p.solicitud_id = :solicitudId")
    List<Payment> findBySolicitudId(@Param("solicitudId") Long solicitudId);

    @Query("SELECT p FROM Payment p WHERE p.cotizacion_id = :cotizacionId")
    List<Payment> findByCotizacionId(@Param("cotizacionId") Long cotizacionId);

    @Query("SELECT p FROM Payment p WHERE p.amount_total >= :minAmount")
    List<Payment> findByAmountTotalGreaterThanEqual(@Param("minAmount") BigDecimal minAmount);

    @Query("SELECT p FROM Payment p WHERE p.created_at BETWEEN :startDate AND :endDate")
    List<Payment> findByCreatedAtBetween(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    @Query("SELECT p FROM Payment p WHERE p.user_id = :userId AND p.status = :status")
    List<Payment> findByUserIdAndStatus(@Param("userId") Long userId, @Param("status") PaymentStatus status);

    @Query("SELECT p FROM Payment p WHERE p.currency = :currency")
    List<Payment> findByCurrency(@Param("currency") String currency);
    
}
