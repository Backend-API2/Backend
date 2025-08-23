package backend_api.Backend.Entity.payment.types;

import backend_api.Backend.Entity.payment.PaymentMethod;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "bank_transfer_payments")
public class BankTransferPayment extends PaymentMethod {
    private String cbu;
    private String bank_name;
    private String alias;
}
