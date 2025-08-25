package backend_api.Backend.Entity.payment.types;

import backend_api.Backend.Entity.payment.PaymentMethod;
import jakarta.persistence.Entity;
import lombok.Data;

@Data
@Entity
public class WalletPayment extends PaymentMethod {
    private String wallet_provider; // mercadoPago, paypal
    private String alias;
}
