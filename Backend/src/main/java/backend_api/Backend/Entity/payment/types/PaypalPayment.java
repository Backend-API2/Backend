package backend_api.Backend.Entity.payment.types;

import backend_api.Backend.Entity.payment.PaymentMethod;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Entity
@Data
@EqualsAndHashCode(callSuper = true)
public class PaypalPayment extends PaymentMethod {
    
    private String paypalEmail;
    private String paypalOrderId;
    private String paypalPayerId;
    private String paypalPaymentId;
    private String paypalSubscriptionId;
    private String intent; // "sale", "authorize", "order"
    
    public PaypalPayment() {
        this.setType(PaymentMethodType.PAYPAL);
    }
}
