package backend_api.Backend.Service.Implementation;

import backend_api.Backend.Entity.payment.Payment;
import backend_api.Backend.Entity.payment.PaymentMethod;
import backend_api.Backend.Entity.payment.PaymentStatus;
import backend_api.Backend.Repository.PaymentRepository;
import backend_api.Backend.Service.Interface.PaymentService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class PaymentServiceImpl implements PaymentService{

    @Autowired
    private PaymentRepository paymentRepository;

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
    public Payment updatePayment(Long id, Payment payment) {
        Optional<Payment> existingPayment = paymentRepository.findById(id);
        if (existingPayment.isPresent()) {
            Payment paymentToUpdate = existingPayment.get();

            paymentToUpdate.setAmount_subtotal(payment.getAmount_subtotal());
            paymentToUpdate.setTaxes(payment.getTaxes());
            paymentToUpdate.setFees(payment.getFees());
            paymentToUpdate.setAmount_total(payment.getAmount_total());
            paymentToUpdate.setCurrency(payment.getCurrency());
            paymentToUpdate.setStatus(payment.getStatus());
            paymentToUpdate.setMethod(payment.getMethod());
            paymentToUpdate.setMetadata(payment.getMetadata());
            paymentToUpdate.setUpdated_at(LocalDateTime.now());
            
            return paymentRepository.save(paymentToUpdate);
        }
        throw new RuntimeException("Pago no fue encontrado con id: " + id);
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
    public void deletePayment(Long id) {
        if (paymentRepository.existsById(id)){
            paymentRepository.deleteById(id);
        } else {
            throw new RuntimeException("Pago no fue encontrado con id: " + id);
        }
    }

    @Override
    public boolean existsById(Long id){
        return paymentRepository.existsById(id);
    }

    @Override
    public long countPaymentsByStatus(PaymentStatus status) {
        return paymentRepository.findByStatus(status).size();
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
    
    @Override
    public BigDecimal getTotalAmountByUserIdAndStatus(Long userId, PaymentStatus status) {
        return paymentRepository.getTotalAmountByUserIdAndStatus(userId, status);
    }
}
