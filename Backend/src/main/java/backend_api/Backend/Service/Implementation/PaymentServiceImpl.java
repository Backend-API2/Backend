package backend_api.Backend.Service.Implementation;

import backend_api.Backend.Entity.payment.Payment;
import backend_api.Backend.Entity.payment.PaymentMethod;
import backend_api.Backend.Entity.payment.PaymentStatus;
import backend_api.Backend.Entity.payment.PaymentEventType;
import backend_api.Backend.Repository.PaymentRepository;
import backend_api.Backend.Service.Interface.PaymentService;
import backend_api.Backend.Service.Interface.PaymentEventService;
import backend_api.Backend.Service.Interface.PaymentAttemptService;
import backend_api.Backend.messaging.publisher.PaymentStatusPublisher;
import backend_api.Backend.messaging.publisher.PaymentMethodSelectedPublisher;
import backend_api.Backend.messaging.dto.PaymentStatusUpdateMessage;
import backend_api.Backend.messaging.dto.PaymentMethodSelectedMessage;
import backend_api.Backend.Entity.payment.types.PaymentMethodType;
import backend_api.Backend.Entity.payment.types.CreditCardPayment;
import backend_api.Backend.Entity.payment.types.DebitCardPayment;
import backend_api.Backend.Entity.payment.types.MercadoPagoPayment;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
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

    @Autowired
    private PaymentStatusPublisher paymentStatusPublisher;

    @Autowired
    private PaymentMethodSelectedPublisher paymentMethodSelectedPublisher;

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
    public List<Payment> getAllPayments(int page, int size) {
        Pageable pageable = Pageable.ofSize(size).withPage(page);
        // Usar findAllOptimized que tiene JOIN FETCH para evitar N+1 queries
        Page<Payment> paymentPage = paymentRepository.findAllOptimized(pageable);
        return paymentPage.getContent();
    }

    @Override
    public List<Payment> getPaymentsByUserId(Long userId, int page, int size) {
        Pageable pageable = Pageable.ofSize(size).withPage(page);
        Page<Payment> paymentPage = paymentRepository.findByUserId(userId, pageable);
        return paymentPage.getContent();
    }

    @Override
    public List<Payment> getPaymentsByProviderId(Long providerId, int page, int size) {
        Pageable pageable = Pageable.ofSize(size).withPage(page);
        Page<Payment> paymentPage = paymentRepository.findByProviderId(providerId, pageable);
        return paymentPage.getContent();
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
    public List<Payment> getPaymentsByDateRange(LocalDate startDate, LocalDate endDate){
        return paymentRepository.findByCreatedAtBetween(startDate, endDate);
    }

    @Override
    public List<Payment> getPaymentsByUserAndStatus(Long userId, PaymentStatus status){
        return paymentRepository.findByUserIdAndStatus(userId, status);
    }

    @Override
    public List<Payment> getPaymentsByProviderAndStatus(Long providerId, PaymentStatus status){
        return paymentRepository.findByProviderIdAndStatus(providerId, status);
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
            PaymentStatus oldStatus = paymentToUpdate.getStatus();
            paymentToUpdate.setStatus(status);
            paymentToUpdate.setUpdated_at(LocalDateTime.now());

            if ( status == PaymentStatus.APPROVED) {
                paymentToUpdate.setCaptured_at(LocalDateTime.now());
            }

            Payment savedPayment = paymentRepository.save(paymentToUpdate);

            publishPaymentStatusUpdate(savedPayment, oldStatus, status);

            return savedPayment;
        }
        throw new RuntimeException("Pago no fue encontrado con id: " + id);
    }

   
    @Override
    public boolean existsById(Long id){
        return paymentRepository.existsById(id);
    }


    @Override
    public BigDecimal getTotalAmountByUserId(Long userId){
        return paymentRepository.getTotalAmountByUserId(userId);
    }
    
    @Override
    public BigDecimal getTotalAmountByProviderId(Long providerId){
        return paymentRepository.getTotalAmountByProviderId(providerId);
    }
    
    @Override
    public BigDecimal getTotalAmountAllApprovedPayments(){
        return paymentRepository.getTotalAmountAllApprovedPayments();
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
    public Payment confirmPayment(Long paymentId, String paymentMethodType, String paymentMethodId, boolean captureImmediately) {
        Optional<Payment> paymentOpt = paymentRepository.findById(paymentId);
        if (!paymentOpt.isPresent()) {
            throw new RuntimeException("Payment not found with id: " + paymentId);
        }
        
        Payment payment = paymentOpt.get();
        
        if (isPaymentExpired(payment)) {
            throw new RuntimeException("Payment has expired");
        }
        
        if (payment.getStatus() != PaymentStatus.PENDING_PAYMENT) {
            throw new RuntimeException("Payment is not in pending_payment status");
        }
        
        paymentEventService.createEvent(
            paymentId,
            PaymentEventType.AUTHORIZATION_STARTED,
            String.format("{\"payment_method_type\": \"%s\", \"capture_immediately\": %s}", paymentMethodType, captureImmediately),
            "system"
        );
        
        boolean success = simulateGatewayCall();

        PaymentStatus newStatus;
        
        if (success) {
            newStatus = PaymentStatus.APPROVED;
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
            newStatus = PaymentStatus.REJECTED;
            
            paymentEventService.createEvent(
                paymentId,
                PaymentEventType.PAYMENT_REJECTED,
                "{\"reason\": \"gateway_declined\", \"retry_allowed\": true}",
                "gateway"
            );
            
            paymentAttemptService.createAttempt(paymentId, PaymentStatus.REJECTED, "declined", "card_declined", "Card was declined", "insufficient_funds");
        }
        
        return updatePaymentStatus(paymentId, newStatus);
    }
    
    @Override
    public Payment cancelPayment(Long paymentId, String reason) {
        Optional<Payment> paymentOpt = paymentRepository.findById(paymentId);
        if (!paymentOpt.isPresent()) {
            throw new RuntimeException("Payment not found with id: " + paymentId);
        }
        
    
        paymentEventService.createEvent(
            paymentId,
            PaymentEventType.PAYMENT_CANCELLED,
            String.format("{\"reason\": \"%s\"}", reason),
            "system"
        );

        return updatePaymentStatus(paymentId, PaymentStatus.CANCELLED);
    }

    @Override
    public Payment expirePayment(Long paymentId) {
        Optional<Payment> paymentOpt = paymentRepository.findById(paymentId);
        if (!paymentOpt.isPresent()) {
            throw new RuntimeException("Payment not found with id: " + paymentId);
        }
        
       
        paymentEventService.createEvent(
            paymentId,
            PaymentEventType.PAYMENT_EXPIRED,
            "{\"expired_at\": \"" + LocalDateTime.now() + "\"}",
            "system"
        );
        
         return updatePaymentStatus(paymentId, PaymentStatus.EXPIRED);
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

    private void publishPaymentStatusUpdate(Payment payment, PaymentStatus oldStatus, PaymentStatus newStatus) {
        try {
            PaymentStatusUpdateMessage message = new PaymentStatusUpdateMessage();
            message.setPaymentId(payment.getId());
            message.setOldStatus(oldStatus);
            message.setNewStatus(newStatus);
            message.setUpdatedAt(payment.getUpdated_at());
            message.setAmountTotal(payment.getAmount_total());
            message.setCurrency(payment.getCurrency());
            message.setGatewayTxnId(payment.getGateway_txn_id());
            message.setCapturedAt(payment.getCaptured_at());

            if (payment.getMetadata() != null && payment.getMetadata().contains("matchingId")) {
                try {
                    com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                    @SuppressWarnings("unchecked")
                    java.util.Map<String, Object> metadataMap = mapper.readValue(payment.getMetadata(), java.util.Map.class);
                    if (metadataMap.containsKey("matchingId")) {
                        message.setMatchingId(((Number) metadataMap.get("matchingId")).longValue());
                    }
                } catch (Exception e) {
                }
            }

            String reason = determineStatusChangeReason(oldStatus, newStatus);
            message.setReason(reason);

            paymentStatusPublisher.publishPaymentStatusUpdate(message);
        } catch (Exception e) {
            throw new RuntimeException("Error al publicar actualización de estado", e);
        }
    }

    private String determineStatusChangeReason(PaymentStatus oldStatus, PaymentStatus newStatus) {
        if (newStatus == PaymentStatus.APPROVED) {
            return "Payment approved successfully";
        } else if (newStatus == PaymentStatus.REJECTED) {
            return "Payment rejected";
        } else if (newStatus == PaymentStatus.CANCELLED) {
            return "Payment cancelled";
        } else if (newStatus == PaymentStatus.EXPIRED) {
            return "Payment expired";
        } else {
            return "Payment status updated from " + oldStatus + " to " + newStatus;
        }
    }
    
    @Override
    public Payment updatePaymentMethod(Long paymentId, PaymentMethod paymentMethod) {
        Optional<Payment> paymentOpt = paymentRepository.findById(paymentId);
        if (!paymentOpt.isPresent()) {
            throw new RuntimeException("Payment not found with id: " + paymentId);
        }
        
        Payment payment = paymentOpt.get();
        
        if (payment.getStatus() != PaymentStatus.PENDING_PAYMENT) {
            throw new RuntimeException("Cannot update payment method. Payment status must be PENDING_PAYMENT");
        }
        
        payment.setMethod(paymentMethod);
        
        paymentEventService.createEvent(
            paymentId,
            PaymentEventType.PAYMENT_METHOD_UPDATED,
            String.format("{\"payment_method_type\": \"%s\", \"payment_method_id\": %d}", 
                paymentMethod.getType(), paymentMethod.getId()),
            "user"
        );
        
        Payment savedPayment = paymentRepository.save(payment);
        
        // Enviar evento de método seleccionado al CORE
        publishMethodSelectedEvent(savedPayment, paymentMethod);
        
        return savedPayment;
    }
    
    private void publishMethodSelectedEvent(Payment payment, PaymentMethod paymentMethod) {
        try {
            PaymentMethodSelectedMessage message = new PaymentMethodSelectedMessage();
            message.setPaymentId(payment.getId());
            message.setUserId(payment.getUser_id());
            message.setMethodType(paymentMethod.getType() != null ? paymentMethod.getType().toString() : null);
            message.setMethodId(paymentMethod.getId());
            message.setSelectedAt(LocalDateTime.now());
            
            // Crear snapshot del método con información relevante
            java.util.Map<String, Object> methodSnapshot = new java.util.HashMap<>();
            if (paymentMethod instanceof CreditCardPayment) {
                CreditCardPayment cc = (CreditCardPayment) paymentMethod;
                methodSnapshot.put("last4Digits", cc.getLast4Digits());
                methodSnapshot.put("cardNetwork", cc.getCard_network());
                methodSnapshot.put("holderName", cc.getHolder_name());
            } else if (paymentMethod instanceof DebitCardPayment) {
                DebitCardPayment dc = (DebitCardPayment) paymentMethod;
                methodSnapshot.put("last4Digits", dc.getLast4Digits());
                methodSnapshot.put("cardNetwork", dc.getCard_network());
                methodSnapshot.put("holderName", dc.getHolder_name());
                methodSnapshot.put("bankName", dc.getBank_name());
            } else if (paymentMethod instanceof MercadoPagoPayment) {
                MercadoPagoPayment mp = (MercadoPagoPayment) paymentMethod;
                methodSnapshot.put("mercadoPagoUserId", mp.getMercadoPagoUserId());
            }
            message.setMethodSnapshot(methodSnapshot);
            
            paymentMethodSelectedPublisher.publish(message);
        } catch (Exception e) {
            // No lanzar excepción para no romper el flujo de negocio, pero loguear el error
            throw new RuntimeException("Error al publicar evento de método seleccionado al CORE", e);
        }
    }

    
}

