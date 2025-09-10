package backend_api.Backend.Entity.payment;

import backend_api.Backend.Entity.payment.types.PaymentMethodType;
import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "payment_methods")
@Inheritance(strategy = InheritanceType.JOINED) // tablas por subtipo
public abstract class PaymentMethod {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING) // guarda el nombre del enum
    @Column(nullable = false, length = 32)
    private PaymentMethodType type;

    @Version
    @Column(name = "version")
    private Long version;
}