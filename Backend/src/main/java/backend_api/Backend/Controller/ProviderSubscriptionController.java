package backend_api.Backend.Controller;

import backend_api.Backend.messaging.service.CoreHubService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/providers/subscriptions")
@RequiredArgsConstructor
@Slf4j
public class ProviderSubscriptionController {

    private final CoreHubService coreHubService;

    @PostMapping("/subscribe-create")
    public ResponseEntity<?> subscribeToProviderCreated() {
        log.info("Suscribiéndose a ALTA de prestadores...");
        try {
            coreHubService.subscribeToTopic("catalogue", "prestador", "alta_prestador");
            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "message", "Suscripción creada: catalogue.prestador.alta_prestador",
                    "topic", "catalogue.prestador.alta_prestador",
                    "eventName", "alta_prestador"
            ));
        } catch (Exception e) {
            log.error("Error creando suscripción alta_prestador: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(Map.of(
                    "status", "error",
                    "message", e.getMessage()
            ));
        }
    }

    @PostMapping("/subscribe-update")
    public ResponseEntity<?> subscribeToProviderUpdated() {
        log.info("Suscribiéndose a MODIFICACION de prestadores...");
        try {
            coreHubService.subscribeToTopic("catalogue", "prestador", "modificacion_prestador");
            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "message", "Suscripción creada: catalogue.prestador.modificacion_prestador",
                    "topic", "catalogue.prestador.modificacion_prestador",
                    "eventName", "modificacion_prestador"
            ));
        } catch (Exception e) {
            log.error("Error creando suscripción modificacion_prestador: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(Map.of(
                    "status", "error",
                    "message", e.getMessage()
            ));
        }
    }

    @PostMapping("/subscribe-deactivate")
    public ResponseEntity<?> subscribeToProviderDeactivated() {
        log.info("Suscribiéndose a BAJA de prestadores...");
        try {
            coreHubService.subscribeToTopic("catalogue", "prestador", "baja_prestador");
            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "message", "Suscripción creada: catalogue.prestador.baja_prestador",
                    "topic", "catalogue.prestador.baja_prestador",
                    "eventName", "baja_prestador"
            ));
        } catch (Exception e) {
            log.error("Error creando suscripción baja_prestador: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(Map.of(
                    "status", "error",
                    "message", e.getMessage()
            ));
        }
    }

    @PostMapping("/subscribe-all")
    public ResponseEntity<?> subscribeToAllProviderEvents() {
        log.info("Suscribiéndose a TODOS los eventos de prestadores...");
        try {
            coreHubService.subscribeToTopic("catalogue", "prestador", "alta_prestador");
            coreHubService.subscribeToTopic("catalogue", "prestador", "modificacion_prestador");
            coreHubService.subscribeToTopic("catalogue", "prestador", "baja_prestador");

            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "message", "Suscripciones creadas",
                    "subscriptions", new String[]{
                            "catalogue.prestador.alta_prestador",
                            "catalogue.prestador.modificacion_prestador",
                            "catalogue.prestador.baja_prestador"
                    }
            ));
        } catch (Exception e) {
            log.error("Error creando suscripciones de providers: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(Map.of(
                    "status", "error",
                    "message", e.getMessage()
            ));
        }
    }

    @GetMapping("/status")
    public ResponseEntity<?> getProviderSubscriptionStatus() {
        return ResponseEntity.ok(Map.of(
                "status", "active",
                "service", "Provider Event Subscriptions",
                "webhookUrl", "/api/core/webhook/provider-events",
                "subscriptions", new String[]{
                        "catalogue.prestador.alta_prestador",
                        "catalogue.prestador.modificacion_prestador",
                        "catalogue.prestador.baja_prestador"
                }
        ));
    }
}