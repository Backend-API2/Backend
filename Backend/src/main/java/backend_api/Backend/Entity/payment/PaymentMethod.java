package backend_api.Backend.Entity.payment;

import backend_api.Backend.Entity.payment.types.PaymentMethodType;
import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "payment_methods")
@Inheritance(strategy = InheritanceType.JOINED)   // <-- clave
public abstract class PaymentMethod {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)                  // <-- guarda el nombre, no el ordinal
    @Column(nullable = false, length = 32)
    private PaymentMethodType type;

    @Version                                      // <-- si querés optimistic locking, va en la raíz
    @Column(name = "version")
    private Long version;
}