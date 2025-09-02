package backend_api.Backend.DTO.invoice;

import backend_api.Backend.Entity.invoice.InvoiceType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class UpdateInvoiceRequest {
    
    private InvoiceType type;
    
    private LocalDateTime dueDate;
    
    @Size(min = 3, max = 3, message = "La moneda debe tener exactamente 3 caracteres")
    private String currency;
    
    private String legalFields;
    
    private String notes;
    
    @Valid
    private List<UpdateInvoiceLineRequest> lines;
    
    private String metadata;
    
    @Data
    public static class UpdateInvoiceLineRequest {
        
        private Long id; 
        
        private Long productId;
        
        @Size(max = 255, message = "La descripción no puede exceder 255 caracteres")
        private String description;
        
        private String productName;
        
        private String productCode;
        
        @Min(value = 1, message = "La cantidad debe ser mayor a 0")
        private Integer quantity;
        
        @DecimalMin(value = "0.01", message = "El precio unitario debe ser mayor a 0")
        private BigDecimal unitPrice;
        
        @DecimalMin(value = "0", message = "La tasa de impuesto no puede ser negativa")
        @DecimalMax(value = "100", message = "La tasa de impuesto no puede ser mayor a 100%")
        private BigDecimal taxRate;
        
        @DecimalMin(value = "0", message = "El monto del impuesto no puede ser negativo")
        private BigDecimal taxAmount;
        
        @DecimalMin(value = "0", message = "La tasa de descuento no puede ser negativa")
        @DecimalMax(value = "100", message = "La tasa de descuento no puede ser mayor a 100%")
        private BigDecimal discountRate;
        
        @DecimalMin(value = "0", message = "El monto del descuento no puede ser negativo")
        private BigDecimal discountAmount;
        
        @Min(value = 1, message = "El número de línea debe ser mayor a 0")
        private Integer lineNumber;
        
        private String unitOfMeasure;
        
        private Boolean deleted = false; // Para marcar líneas a eliminar
    }
}
