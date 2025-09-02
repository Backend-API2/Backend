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
    
    @Column(name = "payment_id", nullable = false)
    private Long paymentId;
    
    @Column(name = "invoice_number", unique = true, nullable = false)
    private String invoiceNumber;
    
    @Column(name = "issue_date", nullable = false)
    private LocalDateTime issueDate;
    
    @Column(name = "due_date")
    private LocalDateTime dueDate;
    
    @Column(name = "total_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;
    
    @Column(name = "subtotal_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal subtotalAmount;
    
    @Column(name = "tax_amount", precision = 10, scale = 2)
    private BigDecimal taxAmount;
    
    @Column(name = "discount_amount", precision = 10, scale = 2)
    private BigDecimal discountAmount;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InvoiceStatus status;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InvoiceType type;
    
    @Column(name = "user_id", nullable = false)
    private Long userId;
    
    @Column(name = "provider_id", nullable = false)
    private Long providerId;
    
    @Column(name = "currency", nullable = false, length = 3)
    private String currency;
    
    @Column(name = "legal_fields", columnDefinition = "TEXT")
    private String legalFields;
    
    @Column(name = "pdf_url")
    private String pdfUrl;
    
    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @Column(name = "sent_at")
    private LocalDateTime sentAt;
    
    @Column(name = "paid_at")
    private LocalDateTime paidAt;
    
    @Column(columnDefinition = "JSON")
    private String metadata;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (status == null) {
            status = InvoiceStatus.DRAFT;
        }
        if (type == null) {
            type = InvoiceType.STANDARD;
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
