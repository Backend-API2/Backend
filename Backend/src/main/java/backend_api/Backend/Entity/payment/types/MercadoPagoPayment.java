package backend_api.Backend.Entity.payment.types;

import backend_api.Backend.Entity.payment.PaymentMethod;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Entity
@Data
@EqualsAndHashCode(callSuper = true)
public class MercadoPagoPayment extends PaymentMethod {
    
    private String mercadoPagoUserId;
    private String accessToken;
    private String preferenceId;
    private String installments;
    private String issuer;
    
    public MercadoPagoPayment() {
        this.setType(PaymentMethodType.MERCADO_PAGO);
    }
}
