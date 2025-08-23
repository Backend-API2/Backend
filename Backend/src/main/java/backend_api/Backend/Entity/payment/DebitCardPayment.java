package backend_api.Backend.Entity.payment;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "debit_card_payments")
public class DebitCardPayment {
    private String card_network;
    private String last4Digits;
    private String holder_name;
    private Integer expiration_month;
    private Integer expiration_year;
    private String bank_name;
    private String cbu;
}
