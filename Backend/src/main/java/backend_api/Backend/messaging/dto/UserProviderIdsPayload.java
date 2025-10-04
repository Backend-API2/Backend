package backend_api.Backend.messaging.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserProviderIdsPayload {
    private Long solicitudId;
    private Long userId;
    private Long providerId;
}
