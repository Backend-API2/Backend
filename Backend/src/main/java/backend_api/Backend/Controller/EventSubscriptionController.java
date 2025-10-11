package backend_api.Backend.Controller;

import backend_api.Backend.Entity.payment.Payment;
import backend_api.Backend.Service.Common.EntityValidationService;
import backend_api.Backend.events.entity.EventSubscription;
import backend_api.Backend.events.entity.EventType;
import backend_api.Backend.events.service.EventPublisherService;
import backend_api.Backend.events.service.SubscriptionService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/events")
@Tag(name = "Eventos HTTP (Webhooks)")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class EventSubscriptionController {

    private final SubscriptionService subscriptionService;
    private final EventPublisherService eventPublisherService;
    private final EntityValidationService entityValidationService;

    // ---- ADMIN CRUD suscripciones ----
    @GetMapping("/subscriptions")
    @PreAuthorize("hasRole('ADMIN')")
    public List<EventSubscription> list() { return subscriptionService.listAll(); }

    @PostMapping("/subscriptions")
    @PreAuthorize("hasRole('ADMIN')")
    public EventSubscription create(@RequestBody CreateSubscriptionReq req) {
        return subscriptionService.create(req.getName(), req.getTargetUrl(), req.getEventTypes());
    }

    @PutMapping("/subscriptions/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public EventSubscription update(@PathVariable Long id, @RequestBody EventSubscription patch) {
        return subscriptionService.update(id, patch);
    }

    @DeleteMapping("/subscriptions/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        subscriptionService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/subscriptions/{id}/rotate-secret")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> rotate(@PathVariable Long id) {
        return ResponseEntity.ok(subscriptionService.rotateSecret(id));
    }

    // ---- Test de publicación (ADMIN o MERCHANT) ----
    @PostMapping("/test/payment-finalized/{paymentId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MERCHANT')")
    public ResponseEntity<Void> testPublish(@PathVariable Long paymentId,
                                            @RequestParam(required = false) String correlationId) {
        Payment p = entityValidationService.getPaymentOrThrow(paymentId);
        eventPublisherService.publishPaymentFinalStatus(p, correlationId);
        return ResponseEntity.accepted().build();
    }

    @Data
    public static class CreateSubscriptionReq {
        @NotBlank private String name;
        @NotBlank private String targetUrl;
        @NotNull  private List<EventType> eventTypes;
    }
}