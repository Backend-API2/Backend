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
    
    @Column(name = "payment_id", nullable = false)
    private Long paymentId;
    
    @Column(name = "attempt_number", nullable = false)
    private Integer attemptNumber;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus status;
    
    @Column(name = "response_code")
    private String responseCode;
    
    @Column(name = "gateway_response_code")
    private String gatewayResponseCode;
    
    @Column(name = "gateway_message")
    private String gatewayMessage;
    
    @Column(name = "failure_reason")
    private String failureReason;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "completed_at")
    private LocalDateTime completedAt;
    
    @Column(name = "gateway_txn_id")
    private String gatewayTxnId;
    private String metadata;
}
