package backend_api.Backend.Entity.invoice;

public enum InvoiceStatus {
    DRAFT,
    PENDING,
    SENT,
    PAID,
    UNPAID,
    PARTIAL,
    CANCELED,
    OVERDUE,
    DISPUTED
}
