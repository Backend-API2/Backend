package backend_api.Backend.Entity.payment.types;

import backend_api.Backend.Entity.payment.PaymentMethod;
import jakarta.persistence.Entity;
import lombok.Data;



@Data
@Entity
public class CreditCardPayment extends PaymentMethod {
    private String card_network; //visa, mastercard, etc.
    private String last4Digits;
    private String holder_name; 
    private Integer expiration_month;
    private Integer expiration_year;
}
