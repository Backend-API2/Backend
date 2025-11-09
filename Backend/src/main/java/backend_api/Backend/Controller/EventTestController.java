package backend_api.Backend.Controller;

import backend_api.Backend.messaging.dto.CoreResponseMessage;
import backend_api.Backend.messaging.dto.PaymentStatusUpdateMessage;
import backend_api.Backend.messaging.dto.PaymentMethodSelectedMessage;
import backend_api.Backend.messaging.service.CoreHubService;
import backend_api.Backend.Entity.payment.PaymentStatus;
import backend_api.Backend.messaging.publisher.PaymentStatusPublisher;
import backend_api.Backend.messaging.publisher.PaymentMethodSelectedPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Controlador de prueba para publicar eventos al CORE
 * Permite probar los 3 eventos principales:
 * 1. created - cuando se recibe una solicitud de pago
 * 2. status_updated - cuando se aprueba el pago
 * 3. method_selected - cuando se selecciona un método de pago
 */
@RestController
@RequestMapping("/api/test/events")
@RequiredArgsConstructor
@Slf4j
public class EventTestController {

    private final CoreHubService coreHubService;
    private final PaymentStatusPublisher paymentStatusPublisher;
    private final PaymentMethodSelectedPublisher paymentMethodSelectedPublisher;

    /**
     * 1. Publica evento "created" - cuando se recibe una solicitud de pago
     * Este es el evento que se envía cuando PaymentRequestProcessorService procesa una solicitud
     */
    @PostMapping("/publish-payment-created")
    public ResponseEntity<?> publishPaymentCreated(@RequestBody(required = false) Map<String, Object> customPayload) {
        try {
            log.info("📤 Publicando evento 'created' al CORE");

            Map<String, Object> payload = new HashMap<>();
            if (customPayload != null && !customPayload.isEmpty()) {
                payload.putAll(customPayload);
            } else {
                // Payload por defecto (mismo formato que PaymentRequestProcessorService.sendPaymentCreatedEvent)
                payload.put("paymentId", 1L);
                payload.put("solicitudId", 100L);
                payload.put("status", "PENDING_PAYMENT");
                payload.put("amount", 1000.50);
                payload.put("currency", "ARS");
                payload.put("userId", 1L);
                payload.put("providerId", 2L);
                payload.put("originalMessageId", UUID.randomUUID().toString());
            }

            CoreResponseMessage coreMessage = CoreResponseMessage.builder()
                .messageId(UUID.randomUUID().toString())
                .timestamp(Instant.now().toString())
                .destination(CoreResponseMessage.Destination.builder()
                    .topic("payment")
                    .eventName("created")
                    .build())
                .payload(payload)
                .build();

            Map<String, Object> coreResponse = coreHubService.publishMessage(coreMessage);

            Boolean success = (Boolean) coreResponse.get("success");
            if (Boolean.TRUE.equals(success)) {
                log.info("✅ Evento 'created' publicado exitosamente al CORE - MessageId: {}", coreMessage.getMessageId());
                return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "message", "Evento 'created' publicado al CORE exitosamente",
                    "eventType", "created",
                    "messageId", coreMessage.getMessageId(),
                    "payload", payload,
                    "coreResponse", coreResponse,
                    "note", "Suscríbete a 'payment.created' desde tu módulo para recibir este evento"
                ));
            } else {
                log.error("❌ Error publicando evento 'created' al CORE: {}", coreResponse.get("error"));
                return ResponseEntity.status(500).body(Map.of(
                    "status", "error",
                    "message", "Error publicando evento al CORE: " + coreResponse.get("error"),
                    "coreResponse", coreResponse
                ));
            }

        } catch (Exception e) {
            log.error("❌ Error publicando evento 'created': {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(Map.of(
                "status", "error",
                "message", "Error: " + e.getMessage()
            ));
        }
    }

    /**
     * 2. Publica evento "status_updated" - cuando se aprueba el pago
     * Este es el evento que se envía cuando PaymentStatusPublisher publica una actualización de estado
     */
    @PostMapping("/publish-payment-status-updated")
    public ResponseEntity<?> publishPaymentStatusUpdated(@RequestBody(required = false) Map<String, Object> customPayload) {
        try {
            log.info("📤 Publicando evento 'status_updated' al CORE");

            PaymentStatusUpdateMessage message = new PaymentStatusUpdateMessage();
            message.setMessageId(UUID.randomUUID().toString());

            if (customPayload != null && !customPayload.isEmpty()) {
                // Usar payload personalizado
                message.setPaymentId(((Number) customPayload.getOrDefault("paymentId", 1L)).longValue());
                message.setOldStatus(PaymentStatus.valueOf((String) customPayload.getOrDefault("oldStatus", "PENDING_PAYMENT")));
                message.setNewStatus(PaymentStatus.valueOf((String) customPayload.getOrDefault("newStatus", "APPROVED")));
                message.setReason((String) customPayload.getOrDefault("reason", "Payment approved successfully"));
                message.setAmountTotal(new BigDecimal(customPayload.getOrDefault("amountTotal", "1000.50").toString()));
                message.setCurrency((String) customPayload.getOrDefault("currency", "ARS"));
                message.setGatewayTxnId((String) customPayload.getOrDefault("gatewayTxnId", "gw_" + UUID.randomUUID().toString()));
                message.setUpdatedAt(LocalDateTime.now());
            } else {
                // Valores por defecto (mismo formato que PaymentStatusPublisher)
                message.setPaymentId(1L);
                message.setOldStatus(PaymentStatus.PENDING_PAYMENT);
                message.setNewStatus(PaymentStatus.APPROVED);
                message.setReason("Payment approved successfully");
                message.setAmountTotal(new BigDecimal("1000.50"));
                message.setCurrency("ARS");
                message.setGatewayTxnId("gw_" + UUID.randomUUID().toString());
                message.setUpdatedAt(LocalDateTime.now());
            }

            // Publicar usando el mismo publisher que usa el código real
            paymentStatusPublisher.publishPaymentStatusUpdate(message);

            log.info("✅ Evento 'status_updated' publicado exitosamente al CORE - MessageId: {}", message.getMessageId());
            return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "Evento 'status_updated' publicado al CORE exitosamente",
                "eventType", "status_updated",
                "messageId", message.getMessageId(),
                "paymentId", message.getPaymentId(),
                "oldStatus", message.getOldStatus(),
                "newStatus", message.getNewStatus(),
                "note", "Suscríbete a 'payment.status_updated' desde tu módulo para recibir este evento"
            ));

        } catch (Exception e) {
            log.error("❌ Error publicando evento 'status_updated': {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(Map.of(
                "status", "error",
                "message", "Error: " + e.getMessage()
            ));
        }
    }

    /**
     * 3. Publica evento "method_selected" - cuando se selecciona un método de pago
     * Este es el evento que se envía cuando PaymentMethodSelectedPublisher publica la selección
     */
    @PostMapping("/publish-payment-method-selected")
    public ResponseEntity<?> publishPaymentMethodSelected(@RequestBody(required = false) Map<String, Object> customPayload) {
        try {
            log.info("📤 Publicando evento 'method_selected' al CORE");

            PaymentMethodSelectedMessage message = new PaymentMethodSelectedMessage();
            message.setMessageId(UUID.randomUUID().toString());

            if (customPayload != null && !customPayload.isEmpty()) {
                // Usar payload personalizado
                message.setPaymentId(((Number) customPayload.getOrDefault("paymentId", 1L)).longValue());
                message.setUserId(((Number) customPayload.getOrDefault("userId", 1L)).longValue());
                message.setMethodType((String) customPayload.getOrDefault("methodType", "CREDIT_CARD"));
                message.setMethodId(((Number) customPayload.getOrDefault("methodId", 1L)).longValue());
                message.setSelectedAt(LocalDateTime.now());
                
                @SuppressWarnings("unchecked")
                Map<String, Object> methodSnapshot = (Map<String, Object>) customPayload.get("methodSnapshot");
                if (methodSnapshot != null) {
                    message.setMethodSnapshot(methodSnapshot);
                } else {
                    // Snapshot por defecto
                    Map<String, Object> snapshot = new HashMap<>();
                    snapshot.put("last4Digits", "1234");
                    snapshot.put("cardNetwork", "VISA");
                    snapshot.put("holderName", "Test User");
                    message.setMethodSnapshot(snapshot);
                }
            } else {
                // Valores por defecto (mismo formato que PaymentMethodSelectedPublisher)
                message.setPaymentId(1L);
                message.setUserId(1L);
                message.setMethodType("CREDIT_CARD");
                message.setMethodId(1L);
                message.setSelectedAt(LocalDateTime.now());
                
                Map<String, Object> methodSnapshot = new HashMap<>();
                methodSnapshot.put("last4Digits", "1234");
                methodSnapshot.put("cardNetwork", "VISA");
                methodSnapshot.put("holderName", "Test User");
                message.setMethodSnapshot(methodSnapshot);
            }

            // Publicar usando el mismo publisher que usa el código real
            paymentMethodSelectedPublisher.publish(message);

            log.info("✅ Evento 'method_selected' publicado exitosamente al CORE - MessageId: {}", message.getMessageId());
            return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "Evento 'method_selected' publicado al CORE exitosamente",
                "eventType", "method_selected",
                "messageId", message.getMessageId(),
                "paymentId", message.getPaymentId(),
                "methodType", message.getMethodType(),
                "note", "Suscríbete a 'payment.method_selected' desde tu módulo para recibir este evento"
            ));

        } catch (Exception e) {
            log.error("❌ Error publicando evento 'method_selected': {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(Map.of(
                "status", "error",
                "message", "Error: " + e.getMessage()
            ));
        }
    }

    /**
     * Publica los 3 eventos de una vez para probar todo
     */
    @PostMapping("/publish-all-events")
    public ResponseEntity<?> publishAllEvents() {
        try {
            log.info("📤 Publicando los 3 eventos al CORE");

            Map<String, Object> results = new HashMap<>();

            // 1. Evento created
            try {
                ResponseEntity<?> createdResponse = publishPaymentCreated(null);
                results.put("created", createdResponse.getBody());
            } catch (Exception e) {
                results.put("created", Map.of("error", e.getMessage()));
            }

            // 2. Evento status_updated
            try {
                ResponseEntity<?> statusResponse = publishPaymentStatusUpdated(null);
                results.put("status_updated", statusResponse.getBody());
            } catch (Exception e) {
                results.put("status_updated", Map.of("error", e.getMessage()));
            }

            // 3. Evento method_selected
            try {
                ResponseEntity<?> methodResponse = publishPaymentMethodSelected(null);
                results.put("method_selected", methodResponse.getBody());
            } catch (Exception e) {
                results.put("method_selected", Map.of("error", e.getMessage()));
            }

            return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "Los 3 eventos han sido publicados al CORE",
                "results", results,
                "note", "Suscríbete a 'payment.created', 'payment.status_updated' y 'payment.method_selected' desde tu módulo"
            ));

        } catch (Exception e) {
            log.error("❌ Error publicando todos los eventos: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(Map.of(
                "status", "error",
                "message", "Error: " + e.getMessage()
            ));
        }
    }

    /**
     * Información sobre los endpoints y cómo suscribirse
     */
    @GetMapping("/info")
    public ResponseEntity<?> getInfo() {
        return ResponseEntity.ok(Map.of(
            "status", "success",
            "message", "Endpoints para publicar eventos al CORE",
            "endpoints", Map.of(
                "publishPaymentCreated", "POST /api/test/events/publish-payment-created - Publica evento 'created'",
                "publishPaymentStatusUpdated", "POST /api/test/events/publish-payment-status-updated - Publica evento 'status_updated'",
                "publishPaymentMethodSelected", "POST /api/test/events/publish-payment-method-selected - Publica evento 'method_selected'",
                "publishAllEvents", "POST /api/test/events/publish-all-events - Publica los 3 eventos de una vez"
            ),
            "events", Map.of(
                "created", Map.of(
                    "topic", "payment",
                    "eventName", "created",
                    "description", "Se envía cuando se recibe una solicitud de pago",
                    "when", "En PaymentRequestProcessorService.sendPaymentCreatedEvent"
                ),
                "status_updated", Map.of(
                    "topic", "payment",
                    "eventName", "status_updated",
                    "description", "Se envía cuando se aprueba/rechaza un pago",
                    "when", "En PaymentStatusPublisher.publishPaymentStatusUpdate"
                ),
                "method_selected", Map.of(
                    "topic", "payment",
                    "eventName", "method_selected",
                    "description", "Se envía cuando se selecciona un método de pago",
                    "when", "En PaymentMethodSelectedPublisher.publish"
                )
            ),
            "subscription", Map.of(
                "note", "Para recibir estos eventos, suscríbete desde tu módulo a:",
                "topics", new String[]{
                    "payment.created",
                    "payment.status_updated",
                    "payment.method_selected"
                },
                "coreUrl", "https://api.arreglacore.click"
            )
        ));
    }
}
