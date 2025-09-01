package backend_api.Backend.Repository;


import backend_api.Backend.Entity.payment.Payment;
import backend_api.Backend.Entity.payment.PaymentMethod;
import backend_api.Backend.Entity.payment.PaymentStatus;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
    
    
    // Buscar por nombre de usuario 
    @Query("SELECT p FROM Payment p " +
           "JOIN User u ON p.user_id = u.id " +
           "WHERE LOWER(u.name) LIKE LOWER(CONCAT('%', :userName, '%'))")
    List<Payment> findByUserNameContaining(@Param("userName") String userName);
    
    // Buscar por nombre de usuario con paginación
    @Query("SELECT p FROM Payment p " +
           "JOIN User u ON p.user_id = u.id " +
           "WHERE LOWER(u.name) LIKE LOWER(CONCAT('%', :userName, '%'))")
    Page<Payment> findByUserNameContaining(@Param("userName") String userName, Pageable pageable);
    
    // Filtrar por rango de monto
    @Query("SELECT p FROM Payment p WHERE p.amount_total BETWEEN :minAmount AND :maxAmount")
    List<Payment> findByAmountTotalBetween(@Param("minAmount") BigDecimal minAmount, @Param("maxAmount") BigDecimal maxAmount);
    
    // Filtrar por rango de monto con paginación
    @Query("SELECT p FROM Payment p WHERE p.amount_total BETWEEN :minAmount AND :maxAmount")
    Page<Payment> findByAmountTotalBetween(@Param("minAmount") BigDecimal minAmount, @Param("maxAmount") BigDecimal maxAmount, Pageable pageable);
    
    // Filtros combinados con paginación
    @Query("SELECT p FROM Payment p " +
           "WHERE (:status IS NULL OR p.status = :status) " +
           "AND (:currency IS NULL OR p.currency = :currency) " +
           "AND (:minAmount IS NULL OR p.amount_total >= :minAmount) " +
           "AND (:maxAmount IS NULL OR p.amount_total <= :maxAmount) " +
           "AND (:startDate IS NULL OR p.created_at >= :startDate) " +
           "AND (:endDate IS NULL OR p.created_at <= :endDate)")
    Page<Payment> findWithFilters(@Param("status") PaymentStatus status,
                                 @Param("currency") String currency,
                                 @Param("minAmount") BigDecimal minAmount,
                                 @Param("maxAmount") BigDecimal maxAmount,
                                 @Param("startDate") LocalDateTime startDate,
                                 @Param("endDate") LocalDateTime endDate,
                                 Pageable pageable);
    
    // Contar pagos por estado
    @Query("SELECT COUNT(p) FROM Payment p WHERE p.status = :status")
    Long countByStatus(@Param("status") PaymentStatus status);
    
    // Obtener total de pagos por usuario
    @Query("SELECT SUM(p.amount_total) FROM Payment p WHERE p.user_id = :userId AND p.status = :status")
    BigDecimal getTotalAmountByUserIdAndStatus(@Param("userId") Long userId, @Param("status") PaymentStatus status);
    
}
