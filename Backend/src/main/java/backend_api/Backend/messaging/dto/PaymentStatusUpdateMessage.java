package backend_api.Backend.messaging.dto;

import backend_api.Backend.Entity.payment.PaymentStatus;
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
public class PaymentStatusUpdateMessage extends BaseMessage {
    private Long paymentId;
    private Long matchingId;
    private PaymentStatus oldStatus;
    private PaymentStatus newStatus;
    private String reason;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;

    private Map<String, Object> additionalData;
    private BigDecimal amountTotal;
    private String currency;
    private String gatewayTxnId;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime capturedAt;
}