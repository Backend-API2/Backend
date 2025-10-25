package backend_api.Backend.Controller;

import backend_api.Backend.messaging.dto.CoreResponseMessage;
import backend_api.Backend.messaging.service.CoreHubService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;


@RestController
@RequestMapping("/api/core/integration")
@RequiredArgsConstructor
@Slf4j
public class CoreIntegrationController {

    private final CoreHubService coreHubService;

    
    @GetMapping("/status")
    public ResponseEntity<?> getCoreStatus() {
        Map<String, Object> status = coreHubService.checkConnection();
        return ResponseEntity.ok(status);
    }

    @PostMapping("/subscribe-payment-requests")
    public ResponseEntity<?> subscribeToPaymentRequests(
            @RequestParam(defaultValue = "matching") String targetTeam,
            @RequestParam(defaultValue = "solicitud") String domain,
            @RequestParam(defaultValue = "created") String action) {
        try {
            String topic = String.format("%s.%s.%s", targetTeam, domain, action);

            coreHubService.subscribeToTopic(targetTeam, domain, action);

            return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "Suscripción creada para recibir solicitudes de pago",
                "topic", topic
            ));

        } catch (Exception e) {
            log.error("Error en suscripción: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(Map.of(
                "status", "error",
                "message", "Error en suscripción al CORE",
                "detail", e.getMessage()
            ));
        }
    }
}
