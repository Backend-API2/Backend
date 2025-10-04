// src/main/java/backend_api/Backend/messaging/dto/PaymentMethodSelectedMessage.java
package backend_api.Backend.messaging.dto;

import lombok.*;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class PaymentMethodSelectedMessage extends BaseMessage {
    private Long paymentId;
    private Long userId;
    private String methodType;   // CREDIT_CARD, DEBIT_CARD, BANK_TRANSFER, etc.
    private Map<String, Object> methodSnapshot; // últimos 4, network, etc.
}