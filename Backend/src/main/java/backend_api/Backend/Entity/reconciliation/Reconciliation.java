package backend_api.Backend.Entity.reconciliation;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "reconciliations")
public class Reconciliation {
    private Long id;
    private LocalDateTime date; 
    private String gateway_batch_id; // ID de lote del gateway
    private BigDecimal net_amount; //total neto
    private BigDecimal fees; //comisiones
    private ReconciliationStatus status;
}
