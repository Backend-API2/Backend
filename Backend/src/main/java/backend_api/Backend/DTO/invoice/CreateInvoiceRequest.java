package backend_api.Backend.DTO.invoice;

import backend_api.Backend.Entity.invoice.InvoiceType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class CreateInvoiceRequest {
    
    @NotNull(message = "El ID del pago es requerido")
    private Long paymentId;
    
    @NotNull(message = "El ID del usuario es requerido")
    private Long userId;
    
    @NotNull(message = "El ID del proveedor es requerido")
    private Long providerId;
    
    @NotNull(message = "El tipo de factura es requerido")
    private InvoiceType type;
    
    private LocalDateTime dueDate;
    
    @NotBlank(message = "La moneda es requerida")
    @Size(min = 3, max = 3, message = "La moneda debe tener exactamente 3 caracteres")
    private String currency;
    
    private String legalFields;
    
    private String notes;
    
    @NotNull(message = "Las líneas de la factura son requeridas")
    @NotEmpty(message = "Debe incluir al menos una línea en la factura")
    @Valid
    private List<CreateInvoiceLineRequest> lines;
    
    private String metadata;
    
    @Data
    public static class CreateInvoiceLineRequest {
        
        private Long productId;
        
        @NotBlank(message = "La descripción es requerida")
        @Size(max = 255, message = "La descripción no puede exceder 255 caracteres")
        private String description;
        
        private String productName;
        
        private String productCode;
        
        @NotNull(message = "La cantidad es requerida")
        @Min(value = 1, message = "La cantidad debe ser mayor a 0")
        private Integer quantity;
        
        @NotNull(message = "El precio unitario es requerido")
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
        
        @NotNull(message = "El número de línea es requerido")
        @Min(value = 1, message = "El número de línea debe ser mayor a 0")
        private Integer lineNumber;
        
        private String unitOfMeasure;
    }
}
