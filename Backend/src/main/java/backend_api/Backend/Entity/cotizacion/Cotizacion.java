package backend_api.Backend.Entity.cotizacion;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Entity
@Table(name = "cotizaciones")
public class Cotizacion {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "cotizacion_number", unique = true, nullable = false)
    private String cotizacionNumber;
    
    @Column(name = "merchant_id", nullable = false)
    private Long merchantId;  // Usuario que crea la cotización (prestador)
    
    @Column(name = "client_id", nullable = false)
    private Long clientId;    // Usuario que recibe la cotización (consumidor)
    
    @Column(name = "client_phone")
    private String clientPhone;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CotizacionStatus status;
    
    @Column(name = "amount_subtotal", nullable = false, precision = 10, scale = 2)
    private BigDecimal amountSubtotal;
    
    @Column(name = "taxes", nullable = false, precision = 10, scale = 2)
    private BigDecimal taxes;
    
    @Column(name = "discount", precision = 10, scale = 2)
    private BigDecimal discount;
    
    @Column(name = "amount_total", nullable = false, precision = 10, scale = 2)
    private BigDecimal amountTotal;
    
    @Column(nullable = false, length = 3)
    private String currency;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @Column(columnDefinition = "TEXT")
    private String notes;
    
    @Column(name = "valid_until", nullable = false)
    private LocalDateTime validUntil;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @Column(name = "approved_at")
    private LocalDateTime approvedAt;
    
    @Column(name = "rejected_at")
    private LocalDateTime rejectedAt;
    
    @Column(name = "expired_at")
    private LocalDateTime expiredAt;
    
    @Column(name = "converted_at")
    private LocalDateTime convertedAt;
    
    @Column(name = "converted_payment_id")
    private Long convertedPaymentId;
    
    @Column(name = "rejection_reason")
    private String rejectionReason;
    
    @Column(columnDefinition = "JSON")
    private String metadata;
    
    @Column(name = "acceptance_token")
    private String acceptanceToken;
    
    @Column(name = "pdf_url")
    private String pdfUrl;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (status == null) {
            status = CotizacionStatus.DRAFT;
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
