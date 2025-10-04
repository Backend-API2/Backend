package backend_api.Backend.messaging.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CoreResponseMessage {
    private String messageId;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    private LocalDateTime timestamp;

    private String source;

    private Destination destination;

    private Map<String, Object> payload;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Destination {
        private String channel;
        private String eventName;
    }
}
