package backend_api.Backend.Entity.payment.types;

import backend_api.Backend.Entity.payment.PaymentMethod;
import jakarta.persistence.Entity;
import jakarta.persistence.DiscriminatorValue;
import lombok.Data;
import lombok.EqualsAndHashCode;



@Data
@Entity
@EqualsAndHashCode(callSuper = true)
@DiscriminatorValue("CREDIT_CARD")
public class CreditCardPayment extends PaymentMethod {
    private String card_network; //visa, mastercard, etc.
    private String last4Digits;
    private String holder_name; 
    private Integer expiration_month;
    private Integer expiration_year;
}
