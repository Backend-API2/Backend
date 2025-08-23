package backend_api.Backend.Entity.payment;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;

@Table(name = "payment_methods")
@Data
@Entity
public abstract class PaymentMethod {
    private Long id;
    private PaymentMethodType type;
}