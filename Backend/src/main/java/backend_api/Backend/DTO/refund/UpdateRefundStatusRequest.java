package backend_api.Backend.DTO.refund;

import backend_api.Backend.Entity.refund.RefundStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateRefundStatusRequest {

    @NotNull(message = "status es requerido")
    private RefundStatus status;

    // Mensaje opcional de error/observación cuando FAILED, etc.
    private String message;
}