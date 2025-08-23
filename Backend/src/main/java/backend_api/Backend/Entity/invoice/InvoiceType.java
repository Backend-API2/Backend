package backend_api.Backend.Entity.invoice;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Table(name = "invoice_type")
@Entity
public enum InvoiceType {
    STANDARD,
    PROFORMA,
    CREDIT
}
