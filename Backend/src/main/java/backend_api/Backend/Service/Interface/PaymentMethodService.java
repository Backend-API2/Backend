package backend_api.Backend.Service.Interface;

import backend_api.Backend.Entity.payment.PaymentMethod;
import backend_api.Backend.DTO.payment.SelectPaymentMethodRequest;

public interface PaymentMethodService {
    
    PaymentMethod createPaymentMethod(SelectPaymentMethodRequest request);
    
    PaymentMethod getPaymentMethodById(Long id);
    
    void deletePaymentMethod(Long id);
}
