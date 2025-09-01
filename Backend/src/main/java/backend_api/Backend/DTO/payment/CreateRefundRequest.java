package backend_api.Backend.DTO.payment;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CreateRefundRequest {
    
    @NotNull(message = "Amount es requerido")
    @DecimalMin(value = "0.01", message = "Amount debe ser mayor a 0")
    private BigDecimal amount;
    
    private String reason;
    
    private boolean isPartial = false;
    
    private String metadata;
}
