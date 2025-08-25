package backend_api.Backend.Entity.payment.types;

import backend_api.Backend.Entity.payment.PaymentMethod;
import jakarta.persistence.Entity;
import lombok.Data;

@Data
@Entity
public class BankTransferPayment extends PaymentMethod {
    private String cbu;
    private String bank_name;
    private String alias;
}
