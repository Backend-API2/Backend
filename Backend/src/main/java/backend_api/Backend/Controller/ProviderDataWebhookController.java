package backend_api.Backend.Controller;

import backend_api.Backend.messaging.dto.CoreEventMessage;
import backend_api.Backend.messaging.service.CoreEventProcessorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/core/webhook")
@RequiredArgsConstructor
@Slf4j
public class ProviderDataWebhookController {

    private final CoreEventProcessorService coreEventProcessorService;

    @PostMapping("/provider-events")
    public ResponseEntity<?> handleProviderEvents(@RequestBody CoreEventMessage message) {
        log.info("Recibido evento de prestador: {}", message.getMessageId());
        
        try {
            coreEventProcessorService.processProviderDataFromCore(message);
            
            return ResponseEntity.ok(Map.of(
                "messageId", message.getMessageId(),
                "status", "processed"
            ));
        } catch (Exception e) {
            log.error("Error procesando evento de prestador: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(Map.of(
                "messageId", message.getMessageId(),
                "status", "error",
                "error", e.getMessage()
            ));
        }
    }
}
