package backend_api.Backend.Entity.payment.types;

import backend_api.Backend.Entity.payment.PaymentMethod;
import jakarta.persistence.Entity;
import jakarta.persistence.DiscriminatorValue;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@Entity
@EqualsAndHashCode(callSuper = true)
@DiscriminatorValue("DEBIT_CARD")
public class DebitCardPayment extends PaymentMethod {
    private String card_network;
    private String last4Digits;
    private String holder_name;
    private Integer expiration_month;
    private Integer expiration_year;
    private String bank_name;
    private String cbu;
}
