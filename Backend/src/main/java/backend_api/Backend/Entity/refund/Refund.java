package backend_api.Backend.Entity.refund;


import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;


@Table(name = "refunds")
@Data
@Entity
public class Refund {
    private Long id;
    private Long paymend_id;
    private BigDecimal amount;
    private String reason;
    private RefundStatus status;
    private String gateway_refund_id;
    private LocalDateTime created_at;
}
