package backend_api.Backend.Entity.payment;

public enum PaymentStatus {
    PENDING_APPROVAL,
    PENDING_PAYMENT,
    APPROVED,
    COMPLETED,
    REJECTED,
    CANCELLED,
    EXPIRED
}
