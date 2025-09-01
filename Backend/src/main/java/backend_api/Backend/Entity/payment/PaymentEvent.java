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
    
    @Column(nullable = false)
    private Long payment_id;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentEventType type;
    
    @Column(columnDefinition = "TEXT")
    private String payload;
    
    @Column(nullable = false)
    private LocalDateTime created_at;
    
    @Column(nullable = false)
    private String actor;
    
    private String event_source;
    private String correlation_id;
    private String metadata;
}
