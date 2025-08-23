package backend_api.Backend.Entity.dispute;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;


@Data
@Entity
@Table(name = "disputes")
public class Dispute {
    private Long id;
    private Long payment_id;
    private DisputeStatus status; 
    private String evidences;
    private BigDecimal amount_provisioned; 
    private LocalDateTime created_at;
}
