package backend_api.Backend.Entity.payment;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "payment_events")
public class PaymentEvent {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "payment_id", nullable = false)
    private Long paymentId;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentEventType type;
    
    @Column(columnDefinition = "TEXT")
    private String payload;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(nullable = false)
    private String actor;
    
    @Column(name = "event_source")
    private String eventSource;

    @Column(name = "description")
    private String description;
    
    @Column(name = "correlation_id")
    private String correlationId;
    private String metadata;
}
