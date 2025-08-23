package backend_api.Backend.Entity.invoice;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Table(name = "invoice_status")
@Entity
public enum InvoiceStatus {
    PAID,
    UNPAID,
    PENDING,
    CANCELED
    
}
