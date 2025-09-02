package backend_api.Backend.DTO.invoice;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateInvoiceStatusRequest {
    
    @NotNull(message = "El nuevo estado es requerido")
    private String status;
    
    private String notes;
}
