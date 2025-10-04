package backend_api.Backend.messaging.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaymentRequestPayload {
    private Long solicitudId;
    private Long userId;
    private Long providerId;
    private BigDecimal amount;
    private String currency;
    private String description;
    private Long cotizacionId;
}
