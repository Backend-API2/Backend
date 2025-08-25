package backend_api.Backend.Entity.payment;

import backend_api.Backend.Entity.payment.types.PaymentMethodType;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import lombok.Data;

@Table(name = "payment_methods")
@Data
@Entity
public abstract class PaymentMethod {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private PaymentMethodType type;
}