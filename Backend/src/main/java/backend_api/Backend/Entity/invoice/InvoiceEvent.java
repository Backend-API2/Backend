package backend_api.Backend.Entity.invoice;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Table(name = "invoice_events")
@Data
@Entity
public class InvoiceEvent {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "invoice_id", nullable = false)
    private Long invoiceId;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false)
    private InvoiceEventType eventType;
    
    @Column(name = "description")
    private String description;
    
    @Column(name = "created_by")
    private Long createdBy;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "event_data", columnDefinition = "JSON")
    private String eventData;
    
    @Column(name = "ip_address")
    private String ipAddress;
    
    @Column(name = "user_agent")
    private String userAgent;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
