package backend_api.Backend.Entity.reconciliation;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "reconciliations")
public class Reconciliation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private LocalDateTime date; 
    private String gateway_batch_id; // ID de lote del gateway
    private BigDecimal net_amount; //total neto
    private BigDecimal fees; //comisiones
    @Enumerated(EnumType.STRING)
    private ReconciliationStatus status;
}
