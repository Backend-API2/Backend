package backend_api.Backend.DTO.refund;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CreateRefundRequest {

    @NotNull(message = "paymentId es requerido")
    private Long paymentId;

    @NotNull(message = "amount es requerido")
    @DecimalMin(value = "0.01", message = "Valor debe ser mayor a 0")
    private BigDecimal amount;

    // Opcional: motivo legible (ej: "customer_request", "duplicate_charge", etc.)
    private String reason;

    // Opcional: metadata en JSON/string si lo necesitas (igual que en payments)
    private String metadata;
}