// backend_api/Backend/Entity/refund/Refund.java
package backend_api.Backend.Entity.refund;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import jakarta.persistence.*;
import lombok.Data;

@Table(name = "refunds")
@Data
@Entity
public class Refund {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "payment_id", nullable = false)
    private Long payment_id;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    private String reason;

    @Enumerated(EnumType.STRING)
    @Column(length = 32, nullable = false)
    private RefundStatus status;

    private String gateway_refund_id;

    @Column(nullable = false)
    private LocalDateTime created_at;

    @Column
    private LocalDateTime updated_at;

    // Quién lo pidió (usuario) y quién lo revisó (merchant)
    @Column(name = "requested_by", nullable = false)
    private Long requestedBy;

    @Column(name = "reviewed_by")
    private Long reviewedBy;

    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;

    @Column(name = "decision_message")
    private String decisionMessage;

    @PrePersist
    void prePersist() {
        created_at = LocalDateTime.now();
        updated_at = created_at;
        if (status == null) status = RefundStatus.PENDING;
    }

    @PreUpdate
    void preUpdate() {
        updated_at = LocalDateTime.now();
    }
}