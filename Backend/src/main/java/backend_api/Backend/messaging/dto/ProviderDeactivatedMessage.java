package backend_api.Backend.messaging.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ProviderDeactivatedMessage extends BaseMessage {
    private Long id;
    private String email;
    private Integer activo;  // 0
    private String motivo;   // opcional
}