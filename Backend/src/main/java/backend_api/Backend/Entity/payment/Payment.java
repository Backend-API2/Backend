package backend_api.Backend.Entity.payment;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Table(name = "payments")
@Data
@Entity
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String payment_intent_id;
    private Long user_id;
    private Long provider_id;
    private Long solicitud_id;
    private Long cotizacion_id; // Se integra con el módulo Cotizacion
    private BigDecimal amount_subtotal; // subtotal sin impuestos
    private BigDecimal taxes; // impuestos
    private BigDecimal fees; // comisiones
    private BigDecimal amount_total;
    private String currency; // tipo de moneda
    @Enumerated(EnumType.STRING)
    private PaymentStatus status; // la idea es que tenga diferentes estados de pago 
    private String gateway_txn_id; // id de la transaccion de gateway
    @ManyToOne
    @JoinColumn(name = "payment_method_id")
    private PaymentMethod method; 
    private LocalDateTime created_at;
    private LocalDateTime updated_at;
    private LocalDateTime captured_at;
    private LocalDateTime expired_at;
    private String metadata; //caso de info adicional
}
