package backend_api.Backend.Service.Interface;

import backend_api.Backend.Entity.payment.Payment;
import backend_api.Backend.Entity.payment.PaymentMethod;
import backend_api.Backend.Entity.payment.PaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface PaymentService {
    Payment createPayment(Payment payment);

    Optional<Payment> getPaymentById(Long id);

    List<Payment> getAllPayments();

    Optional<Payment> getPaymentByIntentId(String paymentIntentId);

    List<Payment> getPaymentsByUserId(Long userId);

    List<Payment> getPaymentsByProviderId(Long providerId);

    List<Payment> getPaymentsByMethod(PaymentMethod method);

    List<Payment> getPaymentsByStatus(PaymentStatus status);    

    Optional<Payment> getPaymentsByGatewayTxnId(String gatewayTxnId);

    List<Payment> getPaymentsBySolicitudId(Long solicitudId);

    List<Payment> getPaymentsByCotizacionId(Long cotizacionId);

    List<Payment> getPaymentsByAmountGreaterThan(BigDecimal minAmount);

    List<Payment> getPaymentsByDateRange(LocalDateTime startDate, LocalDateTime endDate);

    List<Payment> getPaymentsByUserAndStatus(Long userId, PaymentStatus status);

    List<Payment> getPaymentsByCurrency(String currency);

    Payment updatePayment(Long id, Payment payment);

    Payment updatePaymentStatus(Long id, PaymentStatus status);

    void deletePayment(Long id);

    boolean existsById(Long id);

    long countPaymentsByStatus(PaymentStatus status);

    BigDecimal getTotalAmountByUserId (Long userId);



}