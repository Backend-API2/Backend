package backend_api.Backend.Entity.dispute;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;


@Data
@Entity
@Table(name = "disputes")
public class Dispute {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long payment_id;
    @Enumerated(EnumType.STRING)
    private DisputeStatus status; 
    private String evidences;
    private BigDecimal amount_provisioned; 
    private LocalDateTime created_at;
}
