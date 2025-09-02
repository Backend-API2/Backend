package backend_api.Backend.DTO.invoice;

import backend_api.Backend.Entity.invoice.InvoiceStatus;
import backend_api.Backend.Entity.invoice.InvoiceType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InvoiceResponse {
    
    private Long id;
    private Long paymentId;
    private String invoiceNumber;
    private LocalDateTime issueDate;
    private LocalDateTime dueDate;
    private BigDecimal totalAmount;
    private BigDecimal subtotalAmount;
    private BigDecimal taxAmount;
    private BigDecimal discountAmount;
    private InvoiceStatus status;
    private InvoiceType type;
    private Long userId;
    private Long providerId;
    private String currency;
    private String legalFields;
    private String pdfUrl;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime sentAt;
    private LocalDateTime paidAt;
    private String metadata;
    
    private List<InvoiceLineResponse> lines;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class InvoiceLineResponse {
        private Long id;
        private Long invoiceId;
        private Long productId;
        private String description;
        private String productName;
        private String productCode;
        private Integer quantity;
        private BigDecimal unitPrice;
        private BigDecimal subtotal;
        private BigDecimal taxRate;
        private BigDecimal taxAmount;
        private BigDecimal discountRate;
        private BigDecimal discountAmount;
        private BigDecimal totalAmount;
        private Integer lineNumber;
        private String unitOfMeasure;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
    }
}
