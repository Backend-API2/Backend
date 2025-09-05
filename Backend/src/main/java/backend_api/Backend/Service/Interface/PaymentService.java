package backend_api.Backend.Service.Interface;

import backend_api.Backend.Entity.payment.Payment;
import backend_api.Backend.Entity.payment.PaymentMethod;
import backend_api.Backend.Entity.payment.PaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PaymentService {
    Payment createPayment(Payment payment);

    Optional<Payment> getPaymentById(Long id);

    List<Payment> getAllPayments();


    List<Payment> getPaymentsByUserId(Long userId);

    List<Payment> getPaymentsByProviderId(Long providerId);

    List<Payment> getPaymentsByMethod(PaymentMethod method);

    List<Payment> getPaymentsByStatus(PaymentStatus status);    

    Optional<Payment> getPaymentsByGatewayTxnId(String gatewayTxnId);

    List<Payment> getPaymentsBySolicitudId(Long solicitudId);

    // Se integra con el módulo Cotizacion
    List<Payment> getPaymentsByCotizacionId(Long cotizacionId);

    List<Payment> getPaymentsByAmountGreaterThan(BigDecimal minAmount);

    List<Payment> getPaymentsByDateRange(LocalDate startDate, LocalDate endDate);

    List<Payment> getPaymentsByUserAndStatus(Long userId, PaymentStatus status);

    List<Payment> getPaymentsByProviderAndStatus(Long providerId, PaymentStatus status);

    List<Payment> getPaymentsByCurrency(String currency);

    Payment updatePaymentStatus(Long id, PaymentStatus status);


    boolean existsById(Long id);


    BigDecimal getTotalAmountByUserId (Long userId);
    
    BigDecimal getTotalAmountByProviderId (Long providerId);
    
    
    List<Payment> findByUserNameContaining(String userName);

    Page<Payment> findByUserNameContaining(String userName, Pageable pageable);
    
    List<Payment> findByAmountTotalBetween(BigDecimal minAmount, BigDecimal maxAmount);

    Page<Payment> findByAmountTotalBetween(BigDecimal minAmount, BigDecimal maxAmount, Pageable pageable);
    
    Page<Payment> findWithFilters(PaymentStatus status, String currency, 
                                 BigDecimal minAmount, BigDecimal maxAmount,
                                 LocalDate startDate, LocalDate endDate, 
                                 Pageable pageable);
    
    
    Payment confirmPayment(Long paymentId, String paymentMethodType, String paymentMethodId, boolean captureImmediately);
    
    Payment cancelPayment(Long paymentId, String reason);
    
    Payment expirePayment(Long paymentId);
    
    boolean isPaymentExpired(Payment payment);
    
    Payment processPaymentWithRetry(Long paymentId, int maxAttempts);

}