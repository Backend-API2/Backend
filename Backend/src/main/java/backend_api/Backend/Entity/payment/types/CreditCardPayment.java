package backend_api.Backend.Entity.payment.types;

import jakarta.persistence.Entity;
import jakarta.persistence.DiscriminatorValue;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@Entity
@EqualsAndHashCode(callSuper = true)
@DiscriminatorValue("CREDIT_CARD")
public class CreditCardPayment extends CardPayment {
    // Todos los campos comunes están ahora en CardPayment
    // Solo campos específicos de tarjeta de crédito irían aquí si los hubiera
}
