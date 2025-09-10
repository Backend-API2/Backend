package backend_api.Backend.Service.Interface;

import backend_api.Backend.Entity.payment.Payment;

import java.math.BigDecimal;

public interface FundsService {
    void debitForPayment(Payment payment);
    void creditForRefund(Payment payment, BigDecimal amount);
}