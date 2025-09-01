package backend_api.Backend.Service.Implementation;

import backend_api.Backend.Entity.payment.PaymentAttempt;
import backend_api.Backend.Entity.payment.PaymentStatus;
import backend_api.Backend.Repository.PaymentAttemptRepository;
import backend_api.Backend.Service.Interface.PaymentAttemptService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class PaymentAttemptServiceImpl implements PaymentAttemptService {
    
    @Autowired
    private PaymentAttemptRepository paymentAttemptRepository;
    
    @Override
    public PaymentAttempt createAttempt(Long paymentId, PaymentStatus status, String responseCode, String gatewayResponseCode) {
        return createAttempt(paymentId, status, responseCode, gatewayResponseCode, null, null);
    }
    
    @Override
    public PaymentAttempt createAttempt(Long paymentId, PaymentStatus status, String responseCode, String gatewayResponseCode, String gatewayMessage, String failureReason) {
        Integer attemptNumber = getAttemptCount(paymentId) + 1;
        
        PaymentAttempt attempt = new PaymentAttempt();
        attempt.setPaymentId(paymentId);
        attempt.setAttemptNumber(attemptNumber);
        attempt.setStatus(status);
        attempt.setResponseCode(responseCode);
        attempt.setGatewayResponseCode(gatewayResponseCode);
        attempt.setGatewayMessage(gatewayMessage);
        attempt.setFailureReason(failureReason);
        attempt.setCreatedAt(LocalDateTime.now());
        
        if (status == PaymentStatus.APPROVED || status == PaymentStatus.REJECTED) {
            attempt.setCompletedAt(LocalDateTime.now());
        }
        
        return paymentAttemptRepository.save(attempt);
    }
    
    @Override
    public List<PaymentAttempt> getAttemptsByPaymentId(Long paymentId) {
        return paymentAttemptRepository.findByPaymentIdOrderByAttemptNumberDesc(paymentId);
    }
    
    @Override
    public Optional<PaymentAttempt> getLastAttempt(Long paymentId) {
        return paymentAttemptRepository.findLastAttemptByPaymentId(paymentId);
    }
    
    @Override
    public Integer getAttemptCount(Long paymentId) {
        Integer count = paymentAttemptRepository.countByPaymentId(paymentId);
        return count != null ? count : 0;
    }
    
    @Override
    public Optional<PaymentAttempt> getSuccessfulAttempt(Long paymentId) {
        return paymentAttemptRepository.findSuccessfulAttemptByPaymentId(paymentId);
    }
    
    @Override
    public boolean hasExceededMaxAttempts(Long paymentId, int maxAttempts) {
        return getAttemptCount(paymentId) >= maxAttempts;
    }
    
    @Override
    public PaymentAttempt updateAttempt(Long attemptId, PaymentAttempt attempt) {
        Optional<PaymentAttempt> existingAttempt = paymentAttemptRepository.findById(attemptId);
        if (existingAttempt.isPresent()) {
            PaymentAttempt attemptToUpdate = existingAttempt.get();
            attemptToUpdate.setStatus(attempt.getStatus());
            attemptToUpdate.setResponseCode(attempt.getResponseCode());
            attemptToUpdate.setGatewayResponseCode(attempt.getGatewayResponseCode());
            attemptToUpdate.setGatewayMessage(attempt.getGatewayMessage());
            attemptToUpdate.setFailureReason(attempt.getFailureReason());
            attemptToUpdate.setCompletedAt(LocalDateTime.now());
            attemptToUpdate.setGatewayTxnId(attempt.getGatewayTxnId());
            
            return paymentAttemptRepository.save(attemptToUpdate);
        }
        throw new RuntimeException("PaymentAttempt not found with id: " + attemptId);
    }
}
