package backend_api.Backend.Controller;

import backend_api.Backend.messaging.service.CoreHubService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;


@RestController
@RequestMapping("/api/payments/subscriptions")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class PaymentSubscriptionController {

    private final CoreHubService coreHubService;

   
    @PostMapping("/payment-confirmed")
    public ResponseEntity<?> subscribeToPaymentConfirmed(
            @RequestParam String subscriberTeam,
            @RequestParam String subscriberWebhookUrl) {
        try {
            log.info("Nueva suscripción a payment-confirmed - Team: {}, Webhook: {}", 
                subscriberTeam, subscriberWebhookUrl);

            coreHubService.subscribeToTopic(
                "payments",
                "payment",
                "status_updated"
            );

            return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "Suscripción creada para eventos de pago confirmado",
                "subscriberTeam", subscriberTeam,
                "subscriberWebhookUrl", subscriberWebhookUrl,
                "topic", "payments.payment.status_updated",
                "eventTypes", new String[]{"APPROVED", "COMPLETED", "REJECTED", "CANCELLED"}
            ));

        } catch (Exception e) {
            log.error("Error creando suscripción: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(Map.of(
                "status", "error",
                "message", "Error creando suscripción a eventos de pago",
                "detail", e.getMessage()
            ));
        }
    }

    
    @PostMapping("/payment-coordination")
    public ResponseEntity<?> subscribeToPaymentCoordination(
            @RequestParam String subscriberTeam,
            @RequestParam String subscriberWebhookUrl) {
        try {
            log.info("Nueva suscripción a payment-coordination - Team: {}, Webhook: {}", 
                subscriberTeam, subscriberWebhookUrl);

            coreHubService.subscribeToTopic(
                "payments",
                "payment", 
                "coordination"
            );

            return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "Suscripción creada para eventos de coordinación de pago",
                "subscriberTeam", subscriberTeam,
                "subscriberWebhookUrl", subscriberWebhookUrl,
                "topic", "payments.payment.coordination"
            ));

        } catch (Exception e) {
            log.error("Error creando suscripción: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(Map.of(
                "status", "error",
                "message", "Error creando suscripción a coordinación de pago",
                "detail", e.getMessage()
            ));
        }
    }

    
   
    @PostMapping("/payment-method-selected")
    public ResponseEntity<?> subscribeToPaymentMethodSelected(
            @RequestParam String subscriberTeam,
            @RequestParam String subscriberWebhookUrl) {
        try {
            log.info("Nueva suscripción a payment-method-selected - Team: {}, Webhook: {}", 
                subscriberTeam, subscriberWebhookUrl);

            coreHubService.subscribeToTopic(
                "payments",
                "payment",
                "method_selected"
            );

            return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "Suscripción creada para eventos de método de pago seleccionado",
                "subscriberTeam", subscriberTeam,
                "subscriberWebhookUrl", subscriberWebhookUrl,
                "topic", "payments.payment.method_selected"
            ));

        } catch (Exception e) {
            log.error("Error creando suscripción: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(Map.of(
                "status", "error",
                "message", "Error creando suscripción a método de pago seleccionado",
                "detail", e.getMessage()
            ));
        }
    }

    
     
    @GetMapping("/available-events")
    public ResponseEntity<?> getAvailableEvents() {
        return ResponseEntity.ok(Map.of(
            "availableEvents", new String[]{
                "payment-confirmed",
                "payment-coordination", 
                "payment-method-selected",
                "payment-timeline-event"
            },
            "topics", new String[]{
                "payments.payment.status_updated",
                "payments.payment.coordination",
                "payments.payment.method_selected",
                "payments.payment.timeline_event"
            },
            "description", "Eventos disponibles para suscripción desde el módulo de pagos"
        ));
    }
}
