package backend_api.Backend.Entity.invoice;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Table(name = "invoice_lines")
@Data
@Entity
public class InvoiceLine {
    
    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "invoice_id", nullable = false)
    private Long invoiceId;
    
    @Column(name = "product_id")
    private Long productId;
    
    @Column(name = "description", nullable = false)
    private String description;
    
    @Column(name = "product_name")
    private String productName;
    
    @Column(name = "product_code")
    private String productCode;
    
    @Column(name = "quantity", nullable = false)
    private Integer quantity;
    
    @Column(name = "unit_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal unitPrice;
    
    @Column(name = "subtotal", nullable = false, precision = 10, scale = 2)
    private BigDecimal subtotal;
    
    @Column(name = "tax_rate", precision = 5, scale = 2)
    private BigDecimal taxRate;
    
    @Column(name = "tax_amount", precision = 10, scale = 2)
    private BigDecimal taxAmount;
    
    @Column(name = "discount_rate", precision = 5, scale = 2)
    private BigDecimal discountRate;
    
    @Column(name = "discount_amount", precision = 10, scale = 2)
    private BigDecimal discountAmount;
    
    @Column(name = "total_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;
    
    @Column(name = "line_number", nullable = false)
    private Integer lineNumber;
    
    @Column(name = "unit_of_measure")
    private String unitOfMeasure;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        calculateTotals();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
        calculateTotals();
    }
    
    private void calculateTotals() {
        if (quantity != null && unitPrice != null) {
            subtotal = unitPrice.multiply(BigDecimal.valueOf(quantity));
            
            BigDecimal lineTotal = subtotal;
            
            // Aplicar descuento
            if (discountRate != null && discountRate.compareTo(BigDecimal.ZERO) > 0) {
                discountAmount = subtotal.multiply(discountRate.divide(BigDecimal.valueOf(100)));
                lineTotal = lineTotal.subtract(discountAmount);
            } else if (discountAmount != null) {
                lineTotal = lineTotal.subtract(discountAmount);
            }
            
            // Aplicar impuesto
            if (taxRate != null && taxRate.compareTo(BigDecimal.ZERO) > 0) {
                taxAmount = lineTotal.multiply(taxRate.divide(BigDecimal.valueOf(100)));
                lineTotal = lineTotal.add(taxAmount);
            } else if (taxAmount != null) {
                lineTotal = lineTotal.add(taxAmount);
            }
            
            totalAmount = lineTotal;
        }
    }
}
