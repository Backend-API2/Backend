package backend_api.Backend.Entity.invoice;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Table(name = "invoices")
@Data
@Entity
public class Invoice {
    private Long id;
    private Long payment_id;
    private String invoice_number;
    private LocalDateTime issue_date; //fecha de emision
    private BigDecimal total_amount;
    private InvoiceStatus status;
    private InvoiceType type;
    private Long user_id;
    private Long provider_id;
    private String legal_fields;
    private String pdf_url; //link al pdf
    
}
