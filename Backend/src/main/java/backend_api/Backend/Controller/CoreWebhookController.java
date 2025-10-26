package backend_api.Backend.Controller;

import backend_api.Backend.messaging.dto.CoreEventMessage;
import backend_api.Backend.messaging.dto.PaymentRequestMessage;
import backend_api.Backend.messaging.service.CoreEventProcessorService;
import backend_api.Backend.messaging.service.UserEventProcessorService;
import backend_api.Backend.messaging.service.CoreHubService;
import backend_api.Backend.messaging.service.PaymentRequestProcessorService;
import backend_api.Backend.messaging.service.ProviderEventProcessorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;


/**
 * Core Webhook Controller
 * Handles incoming webhook events from the Core system
 */
@RestController
@RequestMapping("/api/core/webhook")
@RequiredArgsConstructor
@Slf4j
public class CoreWebhookController {

    private final CoreEventProcessorService coreEventProcessorService;
    private final UserEventProcessorService userEventProcessorService;
    private final CoreHubService coreHubService;
    private final PaymentRequestProcessorService paymentRequestProcessorService;
    private final ProviderEventProcessorService providerEventProcessorService;


  
    @PostMapping("/payment-events")
    public ResponseEntity<Map<String, String>> receivePaymentEvent(@RequestBody CoreEventMessage message) {
        try {
            log.info("Webhook recibido del CORE - MessageId: {}, EventName: {}, Topic: {}",
                message.getMessageId(),
                message.getDestination() != null ? message.getDestination().getEventName() : "null",
                message.getDestination() != null ? message.getDestination().getTopic() : "null");

            String subscriptionId = extractSubscriptionId(message);

            String eventName = message.getDestination().getEventName();

            switch (eventName) {
                case "CREATE_PAYMENT":
                case "PAYMENT_REQUEST":
                case "created":
                    coreEventProcessorService.processPaymentRequestFromCore(message);
                    break;

                case "status_updated":
                    log.info("✅ Evento de pago procesado exitosamente - MessageId: {}", message.getMessageId());
                    // Los eventos de status_updated son confirmaciones de que el pago fue procesado
                    // No necesitamos procesamiento adicional, solo confirmar recepción
                    break;

                case "USER_PROVIDER_DATA":
                case "DATA_RESPONSE":
                    coreEventProcessorService.processUserProviderDataFromCore(message);
                    break;

                default:
                    log.warn("Evento no reconocido: {}", eventName);
            }

            if (subscriptionId != null) {
                coreHubService.sendAck(message.getMessageId(), subscriptionId);
            }

            return ResponseEntity.ok(Map.of(
                "status", "processed",
                "messageId", message.getMessageId()
            ));

        } catch (Exception e) {
            log.error("Error procesando webhook del CORE - MessageId: {}, Error: {}",
                message.getMessageId(), e.getMessage(), e);

            return ResponseEntity.status(500).body(Map.of(
                "status", "error",
                "messageId", message.getMessageId(),
                "error", e.getMessage(),
                "retryAfter","30"
            ));
        }
    }

   
    @PostMapping("/user-events")
    public ResponseEntity<Map<String, String>> receiveUserEvent(@RequestBody CoreEventMessage message) {
        try {
            log.info("Webhook de usuarios recibido del CORE - MessageId: {}, EventName: {}, Topic: {}",
                message.getMessageId(),
                message.getDestination() != null ? message.getDestination().getEventName() : "null",
                message.getDestination() != null ? message.getDestination().getTopic() : "null");

            String subscriptionId = extractSubscriptionId(message);

            String eventName = message.getDestination().getEventName();

            switch (eventName) {
                case "user_created":
                    userEventProcessorService.processUserCreatedFromCore(message);
                    break;

                case "user_updated":
                    userEventProcessorService.processUserUpdatedFromCore(message);
                    break;

                case "user_deactivated":
                    userEventProcessorService.processUserDeactivatedFromCore(message);
                    break;
                    
                case "user_rejected":
                    userEventProcessorService.processUserRejectedFromCore(message);
                    break;

                default:
                    log.warn("Evento de usuario no reconocido: {}", eventName);
            }

            if (subscriptionId != null) {
                coreHubService.sendAck(message.getMessageId(), subscriptionId);
            }

            return ResponseEntity.ok(Map.of(
                "status", "processed",
                "messageId", message.getMessageId()
            ));

        } catch (Exception e) {
            log.error("Error procesando webhook de usuarios del CORE - MessageId: {}, Error: {}",
                message.getMessageId(), e.getMessage(), e);

            return ResponseEntity.status(500).body(Map.of(
                "status", "error",
                "messageId", message.getMessageId(),
                "error", e.getMessage(),
                "retryAfter","30"
            ));
        }
    }

    @PostMapping("/provider-events")
    public ResponseEntity<Map<String, String>> receiveProviderEvent(@RequestBody CoreEventMessage message) {
        try {
            log.info("Webhook de prestadores recibido del CORE - MessageId: {}, EventName: {}, Topic: {}",
                    message.getMessageId(),
                    message.getDestination() != null ? message.getDestination().getEventName() : "null",
                    message.getDestination() != null ? message.getDestination().getChannel() : "null");

            String subscriptionId = extractSubscriptionId(message);

            providerEventProcessorService.processProviderFromCore(message);

            if (subscriptionId != null) {
                coreHubService.sendAck(message.getMessageId(), subscriptionId);
            }

            return ResponseEntity.ok(Map.of(
                    "status", "processed",
                    "messageId", message.getMessageId()
            ));
        } catch (Exception e) {
            log.error("Error procesando webhook de prestadores - MessageId: {}, Error: {}",
                    message.getMessageId(), e.getMessage(), e);

            return ResponseEntity.status(500).body(Map.of(
                    "status", "error",
                    "messageId", message.getMessageId(),
                    "error", e.getMessage(),
                    "retryAfter","30"
            ));
        }
    }

    @PostMapping("/matching-payment-requests")
    public ResponseEntity<Map<String, Object>> receiveMatchingPaymentRequest(@RequestBody PaymentRequestMessage message) {
        try {
            log.info("🔄 Webhook de solicitud de pago de matching recibido - MessageId: {}, EventName: {}, Topic: {}",
                message.getMessageId(),
                message.getDestination() != null ? message.getDestination().getEventName() : "null",
                message.getDestination() != null ? message.getDestination().getTopic() : "null");

            // Procesar la solicitud de pago
            Map<String, Object> result = paymentRequestProcessorService.processPaymentRequest(message);

            // Enviar ACK solo si el procesamiento fue exitoso
            Boolean success = (Boolean) result.get("success");
            if (Boolean.TRUE.equals(success)) {
                String subscriptionId = extractSubscriptionIdFromPaymentRequest(message);
                if (subscriptionId != null) {
                    coreHubService.sendAck(message.getMessageId(), subscriptionId);
                }
            }

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            log.error("❌ Error procesando solicitud de pago de matching - MessageId: {}, Error: {}",
                message.getMessageId(), e.getMessage(), e);

            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "status", "error",
                "messageId", message.getMessageId(),
                "error", e.getMessage(),
                "retryAfter", "30"
            ));
        }
    }

    
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

    private String extractSubscriptionIdFromPaymentRequest(PaymentRequestMessage message) {
        try {
            // Primero intentar extraer subscriptionId del payload (formato CORE)
            if (message.getPayload() != null && message.getPayload().getPago() != null) {
                PaymentRequestMessage.Pago pago = message.getPayload().getPago();
                // El subscriptionId NO viene en el objeto pago, lo obtenemos del contexto de la suscripción
                // Para ahora, vamos a usar el idCorrelacion como identificador temporal
                return pago.getIdCorrelacion();
            } else if (message.getPayload() != null && message.getPayload().getCuerpo() != null) {
                // Formato antiguo
                return message.getPayload().getCuerpo().getIdCorrelacion();
            }
        } catch (Exception e) {
            log.warn("No se pudo extraer subscriptionId de PaymentRequestMessage: {}", e.getMessage());
        }
        return null;
    }
}
