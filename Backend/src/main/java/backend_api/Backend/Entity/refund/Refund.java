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
    private Long paymend_id;
    private BigDecimal amount;
    private String reason;
    @Enumerated(EnumType.STRING)
    private RefundStatus status;
    private String gateway_refund_id;
    private LocalDateTime created_at;
}
