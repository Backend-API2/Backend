package backend_api.Backend.Controller;

import backend_api.Backend.messaging.dto.CoreEventMessage;
import backend_api.Backend.messaging.service.CoreEventProcessorService;
import backend_api.Backend.messaging.service.CoreHubService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Webhook para recibir eventos del CORE HUB
 */
@RestController
@RequestMapping("/api/core/webhook")
@RequiredArgsConstructor
@Slf4j
public class CoreWebhookController {

    private final CoreEventProcessorService coreEventProcessorService;
    private final CoreHubService coreHubService;

    /**
     * Endpoint que el CORE HUB llama cuando hay un evento
     * POST /api/core/webhook/payment-events
     */
    @PostMapping("/payment-events")
    public ResponseEntity<Map<String, String>> receivePaymentEvent(@RequestBody CoreEventMessage message) {
        try {
            log.info("Webhook recibido del CORE - MessageId: {}, EventName: {}, Source: {}",
                message.getMessageId(),
                message.getDestination().getEventName(),
                message.getSource());

            // Extraer subscriptionId del payload si viene
            String subscriptionId = extractSubscriptionId(message);

            // Procesar según el tipo de evento
            String eventName = message.getDestination().getEventName();

            switch (eventName) {
                case "CREATE_PAYMENT":
                case "PAYMENT_REQUEST":
                case "created":
                    coreEventProcessorService.processPaymentRequestFromCore(message);
                    break;

                case "USER_PROVIDER_DATA":
                case "DATA_RESPONSE":
                    coreEventProcessorService.processUserProviderDataFromCore(message);
                    break;

                default:
                    log.warn("Evento no reconocido: {}", eventName);
            }

            // Enviar ACK al CORE
            if (subscriptionId != null) {
                coreHubService.sendAck(message.getMessageId(), subscriptionId);
            }

            // Responder 200 en < 3s como recomienda la doc
            return ResponseEntity.ok(Map.of(
                "status", "processed",
                "messageId", message.getMessageId()
            ));

        } catch (Exception e) {
            log.error("Error procesando webhook del CORE - MessageId: {}, Error: {}",
                message.getMessageId(), e.getMessage(), e);

            // Devolver error para que el CORE reintente
            return ResponseEntity.status(500).body(Map.of(
                "status", "error",
                "messageId", message.getMessageId(),
                "error", e.getMessage(),
                "retryAfter","30"
            ));
        }
    }

    /**
     * Health check del webhook
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> webhookHealth() {
        return ResponseEntity.ok(Map.of(
            "status", "UP",
            "service", "CORE Webhook Receiver"
        ));
    }

    private String extractSubscriptionId(CoreEventMessage message) {
        try {
            Map<String, Object> payload = message.getPayload();
            if (payload != null && payload.containsKey("subscriptionId")) {
                return payload.get("subscriptionId").toString();
            }
        } catch (Exception e) {
            log.warn("No se pudo extraer subscriptionId: {}", e.getMessage());
        }
        return null;
    }
}
