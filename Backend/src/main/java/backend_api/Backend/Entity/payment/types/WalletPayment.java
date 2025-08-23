package backend_api.Backend.Entity.payment.types;

import backend_api.Backend.Entity.payment.PaymentMethod;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "wallet_payments")
public class WalletPayment extends PaymentMethod {
    private String wallet_provider; // mercadoPago, paypal
    private String alias;
}
