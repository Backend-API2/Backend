package backend_api.Backend.Entity.payment;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "payment_attempts")
@Data
public class PaymentAttempt {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private Long payment_id;
    
    @Column(nullable = false)
    private Integer attempt_number;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus status;
    
    private String response_code;
    private String gateway_response_code;
    private String gateway_message;
    private String failure_reason;
    
    @Column(nullable = false)
    private LocalDateTime created_at;
    
    private LocalDateTime completed_at;
    private String gateway_txn_id;
    private String metadata;
}
