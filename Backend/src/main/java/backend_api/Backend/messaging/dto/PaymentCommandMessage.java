// src/main/java/backend_api/Backend/messaging/dto/PaymentCommandMessage.java
package backend_api.Backend.messaging.dto;

import lombok.*;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class PaymentCommandMessage extends BaseMessage {
    private Long paymentId;
    private String command;   // APPROVE | REJECT | CANCEL | CAPTURE
    private String reason;
    private Map<String, Object> args; // ej. { "agent":"core", "note":"fraud check failed" }
}