package backend_api.Backend.events.dto;

import backend_api.Backend.Entity.payment.PaymentStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
public class PaymentStatusEventPayload {
    private String eventType;       // "PAYMENT_FINALIZED"
    private String eventId;         // UUID
    private String correlationId;   // opcional
    private Long paymentId;
    private Long userId;
    private Long providerId;
    private BigDecimal amountTotal;
    private String currency;
    private PaymentStatus finalStatus;
    private LocalDateTime occurredAt;
    private String timelineUrl;
    private Map<String, Object> metadata;
}