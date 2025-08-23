package backend_api.Backend.Entity.payment;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Table(name = "payments")
@Data
@Entity
public class Payment {
    private Long id;
    private String payment_intent_id;
    private Long user_id;
    private Long provider_id;
    private Long solicitud_id;
    private Long cotizacion_id;
    private BigDecimal amount_subtotal; // subtotal sin impuestos
    private BigDecimal taxes; // impuestos
    private BigDecimal fees; // comisiones
    private BigDecimal amount_total;
    private String currency; // tipo de moneda
    private PaymentStatus status; // la idea es que tenga diferentes estados de pago 
    private String gateway_txn_id; // id de la transaccion de gateway
    private PaymentMethod method; 
    private LocalDateTime created_at;
    private LocalDateTime updated_at;
    private LocalDateTime captured_at;
    private LocalDateTime expired_at;
    private String metadata; //caso de info adicional
}
