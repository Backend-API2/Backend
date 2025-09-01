package backend_api.Backend.Service.Interface;

import backend_api.Backend.Entity.payment.PaymentAttempt;
import backend_api.Backend.Entity.payment.PaymentStatus;

import java.util.List;
import java.util.Optional;

public interface PaymentAttemptService {
    
    PaymentAttempt createAttempt(Long paymentId, PaymentStatus status, String responseCode, String gatewayResponseCode);
    
    PaymentAttempt createAttempt(Long paymentId, PaymentStatus status, String responseCode, String gatewayResponseCode, String gatewayMessage, String failureReason);
    
    List<PaymentAttempt> getAttemptsByPaymentId(Long paymentId);
    
    Optional<PaymentAttempt> getLastAttempt(Long paymentId);
    
    Integer getAttemptCount(Long paymentId);
    
    Optional<PaymentAttempt> getSuccessfulAttempt(Long paymentId);
    
    boolean hasExceededMaxAttempts(Long paymentId, int maxAttempts);
    
    PaymentAttempt updateAttempt(Long attemptId, PaymentAttempt attempt);
}
