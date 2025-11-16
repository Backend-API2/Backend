package backend_api.Backend.Entity.payment;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Table(name = "payments", indexes = {
    @Index(name = "idx_payments_user_id", columnList = "user_id"),
    @Index(name = "idx_payments_provider_id", columnList = "provider_id"),
    @Index(name = "idx_payments_status", columnList = "status"),
    @Index(name = "idx_payments_created_at", columnList = "created_at"),
    @Index(name = "idx_payments_user_created", columnList = "user_id,created_at"),
    @Index(name = "idx_payments_provider_created", columnList = "provider_id,created_at")
})
@Data
@Entity
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
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
    @Column(name = "descripcion", columnDefinition = "TEXT")
    private String descripcion; // Descripción del pago
    @Column(name = "descripcion_solicitud", columnDefinition = "TEXT")
    private String descripcionSolicitud; // Descripción de la solicitud
    
    private Boolean rejected_by_balance = false; // Si fue rechazado por saldo insuficiente
    private Integer retry_attempts = 0; // Número de intentos de reintento
}