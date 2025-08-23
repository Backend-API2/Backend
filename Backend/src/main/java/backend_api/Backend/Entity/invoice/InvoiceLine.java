package backend_api.Backend.Entity.invoice;


import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;
import java.math.BigDecimal;

@Table(name = "invoice_lines")
@Data
@Entity
public class InvoiceLine {
    private Long id;
    private Long invoice_id;
    private String description;
    private Integer quantity;
    private BigDecimal unit_price;
    private BigDecimal subtotal;
    
}
