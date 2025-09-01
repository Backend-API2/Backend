package backend_api.Backend.DTO.payment;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data 
public class UpdatePaymentRequest {
    
    
    // (lo más común que un cliente querría actualizar)
    @Size(max = 1000, message = "Metadata no puede exceder 1000 caracteres")
    private String metadata;
    
    // (en caso de que quiera cambiar el método)
    private Long payment_method_id;
    
    // status - Solo el sistema/admin puede cambiar estados
    // amounts - No se pueden cambiar después de creados
    // gateway_txn_id - Solo el sistema lo maneja
    // timestamps - Automáticos del sistema
    
    // NOTA: Para cambios críticos como cancelaciones, 
    // mejor tener endpoints específicos como /cancel, /refund, etc.
}
