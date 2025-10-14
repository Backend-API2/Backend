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
public class SolicitudDataWebhookController {

    private final CoreEventProcessorService coreEventProcessorService;

    @PostMapping("/solicitud-events")
    public ResponseEntity<?> handleSolicitudEvents(@RequestBody CoreEventMessage message) {
        log.info("Recibido evento de solicitud: {}", message.getMessageId());
        
        try {
            // Procesar evento de solicitud
            coreEventProcessorService.processSolicitudDataFromCore(message);
            
            return ResponseEntity.ok(Map.of(
                "messageId", message.getMessageId(),
                "status", "processed"
            ));
        } catch (Exception e) {
            log.error("Error procesando evento de solicitud: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(Map.of(
                "messageId", message.getMessageId(),
                "status", "error",
                "error", e.getMessage()
            ));
        }
    }
}
