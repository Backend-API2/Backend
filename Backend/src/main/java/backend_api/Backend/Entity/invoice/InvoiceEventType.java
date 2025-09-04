package backend_api.Backend.Entity.invoice;

public enum InvoiceEventType {
    // Eventos de creación
    INVOICE_CREATED,
    INVOICE_DRAFT_SAVED,
    
    // Eventos de estado
    INVOICE_SENT,
    INVOICE_VIEWED,
    INVOICE_DOWNLOADED,
    
    // Eventos de pago
    PAYMENT_INITIATED,
    PAYMENT_COMPLETED,
    PAYMENT_FAILED,
    PARTIAL_PAYMENT_RECEIVED,
    
    // Eventos de tiempo
    INVOICE_DUE_SOON,
    INVOICE_OVERDUE,
    
    // Eventos de modificación
    INVOICE_UPDATED,
    INVOICE_CANCELED,
    INVOICE_DELETED,
    
    // Eventos de PDF
    PDF_GENERATED,
    PDF_REGENERATED,
    
    
    // Eventos de recordatorio
    REMINDER_SENT,
    FINAL_NOTICE_SENT,
    
    // Eventos de nota de crédito
    CREDIT_NOTE_ISSUED,
    REFUND_PROCESSED,
    
    // Eventos del sistema
    SYSTEM_ERROR,
    MANUAL_ADJUSTMENT
}
