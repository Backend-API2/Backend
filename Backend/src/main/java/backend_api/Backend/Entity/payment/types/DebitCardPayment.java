package backend_api.Backend.Entity.payment.types;

import jakarta.persistence.Entity;
import jakarta.persistence.DiscriminatorValue;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@Entity
@EqualsAndHashCode(callSuper = true)
@DiscriminatorValue("DEBIT_CARD")
public class DebitCardPayment extends CardPayment {
    // Campos específicos de tarjeta de débito
    private String bank_name;
    private String cbu;
    
    // Los campos comunes (card_network, last4Digits, holder_name, expiration_month, expiration_year)
    // están ahora heredados de CardPayment
}
