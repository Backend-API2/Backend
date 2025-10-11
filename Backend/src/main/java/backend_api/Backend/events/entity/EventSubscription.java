package backend_api.Backend.events.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "event_subscriptions")
@Data
public class EventSubscription {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false) private String name;
    @Column(nullable = false, length = 1000) private String targetUrl;
    /** CSV de tipos: "PAYMENT_FINALIZED" */
    @Column(nullable = false) private String eventTypes;

    /** secreto para firmar (HMAC) */
    @Column(nullable = false) private String secret;

    @Column(nullable = false) private boolean active = true;
    @Column(nullable = false) private int maxRetries = 3;
    @Column(nullable = false) private long backoffMs = 1500;
    @Column(nullable = false) private int requestTimeoutMs = 10000;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = createdAt;
    }
    @PreUpdate void onUpdate() { updatedAt = LocalDateTime.now(); }

    public boolean supports(EventType type) {
        if (eventTypes == null) return false;
        for (String t : eventTypes.split(",")) {
            if (t.trim().equalsIgnoreCase(type.name())) return true;
        }
        return false;
    }
}