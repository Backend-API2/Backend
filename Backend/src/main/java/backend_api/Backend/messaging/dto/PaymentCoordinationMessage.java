package backend_api.Backend.messaging.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class PaymentCoordinationMessage extends BaseMessage {
    private Long matchingId;
    private Long userId;
    private Long providerId;
    private BigDecimal amount;
    private String currency;
    private String paymentMethodType;
    private Map<String, Object> metadata;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime coordinatedAt;

    private String description;
    private Long solicitudId;
    private Long cotizacionId;
}