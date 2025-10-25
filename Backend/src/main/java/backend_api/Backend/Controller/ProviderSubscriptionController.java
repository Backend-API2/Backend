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
@CrossOrigin(origins = "*")
public class ProviderSubscriptionController {

    private final CoreHubService coreHubService;

    @PostMapping("/subscribe-alta")
    public ResponseEntity<?> subscribeAlta() {
        return subscribe("alta");
    }

    @PostMapping("/subscribe-modificacion")
    public ResponseEntity<?> subscribeModificacion() {
        return subscribe("modificacion");
    }

    @PostMapping("/subscribe-baja")
    public ResponseEntity<?> subscribeBaja() {
        return subscribe("baja");
    }

    @PostMapping("/subscribe-all")
    public ResponseEntity<?> subscribeAll() {
        subscribe("alta");
        subscribe("modificacion");
        subscribe("baja");
        return ResponseEntity.ok(Map.of(
                "status", "success",
                "subscriptions", new String[]{
                        "catalogue.prestador.alta",
                        "catalogue.prestador.modificacion",
                        "catalogue.prestador.baja"
                }
        ));
    }

    private ResponseEntity<?> subscribe(String action) {
        try {
            coreHubService.subscribeToTopic("catalogue", "prestador", action);
            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "topic", "catalogue.prestador." + action,
                    "eventName", action + "_prestador"
            ));
        } catch (Exception e) {
            log.error("Error creando suscripción a prestadores. action={}: {}", action, e.getMessage());
            return ResponseEntity.internalServerError().body(Map.of(
                    "status", "error",
                    "message", e.getMessage()
            ));
        }
    }
}