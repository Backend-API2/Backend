package backend_api.Backend.Entity.invoice;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Table(name = "invoices")
@Data
@Entity
public class Invoice {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long payment_id;
    private String invoice_number;
    private LocalDateTime issue_date; //fecha de emision
    private BigDecimal total_amount;
    @Enumerated(EnumType.STRING)
    private InvoiceStatus status;
    @Enumerated(EnumType.STRING)
    private InvoiceType type;
    private Long user_id;
    private Long provider_id;
    private String legal_fields;
    private String pdf_url; //link al pdf
    
}
