package backend_api.Backend.Controller;

import backend_api.Backend.messaging.dto.CoreResponseMessage;
import backend_api.Backend.messaging.service.CoreHubService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Endpoints para probar integración con CORE HUB
 */
@RestController
@RequestMapping("/api/core/integration")
@RequiredArgsConstructor
@Slf4j
public class CoreIntegrationController {

    private final CoreHubService coreHubService;

    /**
     * Verifica configuración y conexión con CORE HUB
     * GET /api/core/integration/status
     */
    @GetMapping("/status")
    public ResponseEntity<?> getCoreStatus() {
        Map<String, Object> status = coreHubService.checkConnection();
        return ResponseEntity.ok(status);
    }

    /**
     * Suscribe al webhook para recibir eventos de solicitudes de pago
     * POST /api/core/integration/subscribe-payment-requests?targetTeam=matching&domain=solicitud&action=created
     */
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

    /**
     * Publica un mensaje de prueba al CORE
     * POST /api/core/integration/test-publish
     */
    @PostMapping("/test-publish")
    public ResponseEntity<?> testPublish(
            @RequestParam(defaultValue = "payment") String domain,
            @RequestParam(defaultValue = "test") String action) {
        try {
            Map<String, Object> payload = new HashMap<>();
            payload.put("message", "Test desde Payments Backend");
            payload.put("timestamp", System.currentTimeMillis());

            String channel = String.format("payments.%s.%s", domain, action);

            CoreResponseMessage message = CoreResponseMessage.builder()
                .messageId(UUID.randomUUID().toString())
                .timestamp(Instant.now().toString())
                .source("payments")
                .destination(CoreResponseMessage.Destination.builder()
                    .channel(channel)
                    .eventName(action)
                    .build())
                .payload(payload)
                .build();

            coreHubService.publishMessage(message);

            return ResponseEntity.ok(Map.of(
                "status", "success",
                "messageId", message.getMessageId(),
                "channel", channel,
                "message", "Mensaje publicado al CORE"
            ));

        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                "status", "error",
                "message", e.getMessage()
            ));
        }
    }

    /**
     * Envía confirmación de IDs al CORE (simula el flujo real)
     * POST /api/core/integration/send-ids
     */
    @PostMapping("/send-ids")
    public ResponseEntity<?> sendIdsToCore(
            @RequestParam Long solicitudId,
            @RequestParam Long userId,
            @RequestParam Long providerId) {
        try {
            Map<String, Object> payload = new HashMap<>();
            payload.put("solicitudId", solicitudId);
            payload.put("userId", userId);
            payload.put("providerId", providerId);

            CoreResponseMessage message = CoreResponseMessage.builder()
                .messageId(UUID.randomUUID().toString())
                .timestamp(Instant.now().toString())
                .source("payments")
                .destination(CoreResponseMessage.Destination.builder()
                    .channel("payments.id.extracted")
                    .eventName("extracted")
                    .build())
                .payload(payload)
                .build();

            coreHubService.publishMessage(message);

            return ResponseEntity.ok(Map.of(
                "status", "success",
                "messageId", message.getMessageId(),
                "payload", payload
            ));

        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                "status", "error",
                "message", e.getMessage()
            ));
        }
    }
}
