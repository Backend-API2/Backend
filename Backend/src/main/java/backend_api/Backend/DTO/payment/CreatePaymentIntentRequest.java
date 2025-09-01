package backend_api.Backend.DTO.payment;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CreatePaymentIntentRequest {
    
    @NotNull(message = "User ID es requerido")
    private Long userId;
    
    @NotNull(message = "Provider ID es requerido") 
    private Long providerId;
    
    private Long solicitudId;
    private Long cotizacionId;
    
    @NotNull(message = "Amount subtotal es requerido")
    @DecimalMin(value = "0.01", message = "Amount subtotal debe ser mayor a 0")
    private BigDecimal amountSubtotal;
    
    @NotNull(message = "Taxes es requerido")
    @DecimalMin(value = "0.00", message = "Taxes no puede ser negativo")
    private BigDecimal taxes;
    
    @NotNull(message = "Fees es requerido")
    @DecimalMin(value = "0.00", message = "Fees no puede ser negativo")
    private BigDecimal fees;
    
    @NotBlank(message = "Currency es requerida")
    private String currency;
    
    private String metadata;
    
    private Integer expiresInMinutes = 30;
}
