package backend_api.Backend.Service.Implementation;

import backend_api.Backend.Entity.payment.Payment;
import backend_api.Backend.Entity.payment.PaymentMethod;
import backend_api.Backend.Entity.payment.PaymentStatus;
import backend_api.Backend.Entity.payment.PaymentEventType;
import backend_api.Backend.Repository.PaymentRepository;
import backend_api.Backend.Service.Interface.PaymentService;
import backend_api.Backend.Service.Interface.PaymentEventService;
import backend_api.Backend.Service.Interface.PaymentAttemptService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class PaymentServiceImpl implements PaymentService{

    @Autowired
    private PaymentRepository paymentRepository;
    
    @Autowired
    private PaymentEventService paymentEventService;
    
    @Autowired
    private PaymentAttemptService paymentAttemptService;

    @Override
    public Payment createPayment(Payment payment) {
        payment.setCreated_at(LocalDateTime.now());
        payment.setUpdated_at(LocalDateTime.now());
        return paymentRepository.save(payment);
    }

    @Override
    public Optional<Payment> getPaymentById(Long id) {
        return paymentRepository.findById(id);
    }

    @Override
    public List<Payment> getAllPayments() {
        return paymentRepository.findAll();
    }

    @Override
public Optional<Payment> getPaymentByIntentId(String paymentIntentId) {
    return paymentRepository.findByPaymentIntentId(paymentIntentId);
}

    @Override
    public List<Payment> getPaymentsByUserId(Long userId) {
        return paymentRepository.findByUserId(userId);
    }

    @Override
    public List<Payment> getPaymentsByProviderId(Long providerId) {
        return paymentRepository.findByProviderId(providerId);
    }

    @Override
    public List<Payment> getPaymentsByStatus(PaymentStatus status) {
        return paymentRepository.findByStatus(status);
    }

    @Override
    public List<Payment> getPaymentsByMethod(PaymentMethod method) {
        return paymentRepository.findByMethod(method);
    }

    @Override
    public Optional<Payment> getPaymentsByGatewayTxnId(String gatewayTxnId) {
        return paymentRepository.findByGatewayTxnId(gatewayTxnId);
    }

    @Override
    public List<Payment> getPaymentsBySolicitudId(Long solicitudId){
        return paymentRepository.findBySolicitudId(solicitudId);
    }

    // Se integra con el módulo Cotizacion
    @Override
    public List<Payment> getPaymentsByCotizacionId(Long cotizacionId){
        return paymentRepository.findByCotizacionId(cotizacionId);
    }

    @Override
    public List<Payment> getPaymentsByAmountGreaterThan(BigDecimal minAmount){
        return paymentRepository.findByAmountTotalGreaterThanEqual(minAmount);
    }

    @Override
    public List<Payment> getPaymentsByDateRange(LocalDateTime startDate, LocalDateTime endDate){
        return paymentRepository.findByCreatedAtBetween(startDate, endDate);
    }

    @Override
    public List<Payment> getPaymentsByUserAndStatus(Long userId, PaymentStatus status){
        return paymentRepository.findByUserIdAndStatus(userId, status);
    }

    @Override
    public List<Payment> getPaymentsByCurrency(String currency){
        return paymentRepository.findByCurrency(currency);
    }

  

    @Override
    public Payment updatePaymentStatus(Long id, PaymentStatus status) {
        Optional<Payment> exisitngPayment = paymentRepository.findById(id);
        if (exisitngPayment.isPresent()){
            Payment paymentToUpdate = exisitngPayment.get();
            paymentToUpdate.setStatus(status);
            paymentToUpdate.setUpdated_at(LocalDateTime.now());

            if ( status == PaymentStatus.APPROVED) {
                paymentToUpdate.setCaptured_at(LocalDateTime.now());
            }
            return paymentRepository.save(paymentToUpdate);
        }
        throw new RuntimeException("Pago no fue encontrado con id: " + id);
    }

   
    @Override
    public boolean existsById(Long id){
        return paymentRepository.existsById(id);
    }


    @Override
    public BigDecimal getTotalAmountByUserId(Long userId){
        List<Payment> payments = paymentRepository.findByUserId(userId);
        return payments.stream()
                .filter(payment -> payment.getStatus() == PaymentStatus.APPROVED)
                .map(Payment::getAmount_total)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
        
    @Override
    public List<Payment> findByUserNameContaining(String userName) {
        return paymentRepository.findByUserNameContaining(userName);
    }
    
    @Override
    public Page<Payment> findByUserNameContaining(String userName, Pageable pageable) {
        return paymentRepository.findByUserNameContaining(userName, pageable);
    }
    
    @Override
    public List<Payment> findByAmountTotalBetween(BigDecimal minAmount, BigDecimal maxAmount) {
        return paymentRepository.findByAmountTotalBetween(minAmount, maxAmount);
    }
    
    @Override
    public Page<Payment> findByAmountTotalBetween(BigDecimal minAmount, BigDecimal maxAmount, Pageable pageable) {
        return paymentRepository.findByAmountTotalBetween(minAmount, maxAmount, pageable);
    }
    
    @Override
    public Page<Payment> findWithFilters(PaymentStatus status, String currency, 
                                       BigDecimal minAmount, BigDecimal maxAmount,
                                       LocalDateTime startDate, LocalDateTime endDate, 
                                       Pageable pageable) {
        return paymentRepository.findWithFilters(status, currency, minAmount, maxAmount, startDate, endDate, pageable);
    }
    

    
    // Se integra con el módulo Cotizacion  
    @Override
    public Payment createPaymentIntent(Long userId, Long providerId, Long solicitudId, Long cotizacionId,
                                     BigDecimal amountSubtotal, BigDecimal taxes, BigDecimal fees,
                                     String currency, String metadata, Integer expiresInMinutes) {
        Payment payment = new Payment();
        payment.setPayment_intent_id("pi_" + UUID.randomUUID().toString().replace("-", ""));
        payment.setUser_id(userId);
        payment.setProvider_id(providerId);
        payment.setSolicitud_id(solicitudId);
        payment.setCotizacion_id(cotizacionId); // Se integra con el módulo Cotizacion
        payment.setAmount_subtotal(amountSubtotal);
        payment.setTaxes(taxes);
        payment.setFees(fees);
        payment.setAmount_total(amountSubtotal.add(taxes).add(fees));
        payment.setCurrency(currency);
        payment.setStatus(PaymentStatus.PENDING);
        payment.setCreated_at(LocalDateTime.now());
        payment.setUpdated_at(LocalDateTime.now());
        payment.setExpired_at(LocalDateTime.now().plusMinutes(expiresInMinutes));
        payment.setMetadata(metadata);
        
        Payment savedPayment = paymentRepository.save(payment);
        
        paymentEventService.createEvent(
            savedPayment.getId(),
            PaymentEventType.PAYMENT_INTENT_CREATED,
            String.format("{\"payment_intent_id\": \"%s\", \"amount_total\": %s, \"expires_at\": \"%s\"}", 
                savedPayment.getPayment_intent_id(), savedPayment.getAmount_total(), savedPayment.getExpired_at()),
            "system"
        );
        
        return savedPayment;
    }
    @Override
    public Payment confirmPayment(Long paymentId, String paymentMethodType, String paymentMethodId, boolean captureImmediately) {
        Optional<Payment> paymentOpt = paymentRepository.findById(paymentId);
        if (!paymentOpt.isPresent()) {
            throw new RuntimeException("Payment not found with id: " + paymentId);
        }
        
        Payment payment = paymentOpt.get();
        
        if (isPaymentExpired(payment)) {
            throw new RuntimeException("Payment has expired");
        }
        
        if (payment.getStatus() != PaymentStatus.PENDING) {
            throw new RuntimeException("Payment is not in pending status");
        }
        
        paymentEventService.createEvent(
            paymentId,
            PaymentEventType.AUTHORIZATION_STARTED,
            String.format("{\"payment_method_type\": \"%s\", \"capture_immediately\": %s}", paymentMethodType, captureImmediately),
            "system"
        );
        
        boolean success = simulateGatewayCall();
        
        if (success) {
            payment.setStatus(PaymentStatus.APPROVED);
            payment.setCaptured_at(LocalDateTime.now());
            payment.setGateway_txn_id("txn_" + UUID.randomUUID().toString().replace("-", ""));
            
            paymentEventService.createEvent(
                paymentId,
                PaymentEventType.PAYMENT_APPROVED,
                String.format("{\"gateway_txn_id\": \"%s\", \"captured_at\": \"%s\"}", payment.getGateway_txn_id(), payment.getCaptured_at()),
                "gateway"
            );
            
            paymentAttemptService.createAttempt(paymentId, PaymentStatus.APPROVED, "success", "approved", "Payment successful", null);
        } else {
            payment.setStatus(PaymentStatus.REJECTED);
            
            paymentEventService.createEvent(
                paymentId,
                PaymentEventType.PAYMENT_REJECTED,
                "{\"reason\": \"gateway_declined\", \"retry_allowed\": true}",
                "gateway"
            );
            
            paymentAttemptService.createAttempt(paymentId, PaymentStatus.REJECTED, "declined", "card_declined", "Card was declined", "insufficient_funds");
        }
        
        payment.setUpdated_at(LocalDateTime.now());
        return paymentRepository.save(payment);
    }
    
    @Override
    public Payment cancelPayment(Long paymentId, String reason) {
        Optional<Payment> paymentOpt = paymentRepository.findById(paymentId);
        if (!paymentOpt.isPresent()) {
            throw new RuntimeException("Payment not found with id: " + paymentId);
        }
        
        Payment payment = paymentOpt.get();
        payment.setStatus(PaymentStatus.CANCELLED);
        payment.setUpdated_at(LocalDateTime.now());
        
        paymentEventService.createEvent(
            paymentId,
            PaymentEventType.PAYMENT_CANCELLED,
            String.format("{\"reason\": \"%s\"}", reason),
            "system"
        );
        
        return paymentRepository.save(payment);
    }
    
    @Override
    public Payment expirePayment(Long paymentId) {
        Optional<Payment> paymentOpt = paymentRepository.findById(paymentId);
        if (!paymentOpt.isPresent()) {
            throw new RuntimeException("Payment not found with id: " + paymentId);
        }
        
        Payment payment = paymentOpt.get();
        payment.setStatus(PaymentStatus.EXPIRED);
        payment.setUpdated_at(LocalDateTime.now());
        
        paymentEventService.createEvent(
            paymentId,
            PaymentEventType.PAYMENT_EXPIRED,
            "{\"expired_at\": \"" + LocalDateTime.now() + "\"}",
            "system"
        );
        
        return paymentRepository.save(payment);
    }
    
    @Override
    public boolean isPaymentExpired(Payment payment) {
        return payment.getExpired_at() != null && LocalDateTime.now().isAfter(payment.getExpired_at());
    }
    
    @Override
    public Payment processPaymentWithRetry(Long paymentId, int maxAttempts) {
        if (paymentAttemptService.hasExceededMaxAttempts(paymentId, maxAttempts)) {
            throw new RuntimeException("Maximum retry attempts exceeded for payment: " + paymentId);
        }
        
        Optional<Payment> paymentOpt = paymentRepository.findById(paymentId);
        if (!paymentOpt.isPresent()) {
            throw new RuntimeException("Payment not found with id: " + paymentId);
        }
        
        Payment payment = paymentOpt.get();
        
        paymentEventService.createEvent(
            paymentId,
            PaymentEventType.RETRY_ATTEMPTED,
            String.format("{\"attempt_number\": %d, \"max_attempts\": %d}", 
                paymentAttemptService.getAttemptCount(paymentId) + 1, maxAttempts),
            "system"
        );
        
        return confirmPayment(paymentId, "credit_card", null, true);
    }
    
    private boolean simulateGatewayCall() {
        return Math.random() > 0.2;
    }
}
