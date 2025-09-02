package backend_api.Backend.Entity.invoice;

public enum InvoiceType {
    STANDARD,   // Factura estándar
    PROFORMA,   // Factura proforma (estimación)
    CREDIT,     // Nota de crédito
    DEBIT,      // Nota de débito
    RECURRING,  // Factura recurrente
    FINAL,      // Factura final
    ADVANCE     // Factura de anticipo
}
