package backend_api.Backend.Controller;

import backend_api.Backend.messaging.service.CoreHubService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/users/subscriptions")
@RequiredArgsConstructor
@Slf4j
public class UserSubscriptionController {

    private final CoreHubService coreHubService;

    @PostMapping("/subscribe-create")
    public ResponseEntity<?> subscribeToUserCreated() {
        log.info("Suscribiéndose a eventos de creación de usuarios...");

        try {
            coreHubService.subscribeToTopic(
                "users",
                "user",
                "user_created"
            );

            log.info("Suscripción a creación de usuarios creada exitosamente");
            return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "Suscripción a creación de usuarios creada exitosamente",
                "topic", "user",
                "eventName", "user_created"
            ));

        } catch (Exception e) {
            log.error("Error creando suscripción a creación de usuarios: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(Map.of(
                "status", "error",
                "message", "Error creando suscripción: " + e.getMessage()
            ));
        }
    }

    @PostMapping("/subscribe-update")
    public ResponseEntity<?> subscribeToUserUpdated() {
        log.info("Suscribiéndose a eventos de actualización de usuarios...");

        try {
            coreHubService.subscribeToTopic(
                "users",
                "user",
                "user_updated"
            );

            log.info("Suscripción a actualización de usuarios creada exitosamente");
            return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "Suscripción a actualización de usuarios creada exitosamente",
                "topic", "user",
                "eventName", "user_updated"
            ));

        } catch (Exception e) {
            log.error("Error creando suscripción a actualización de usuarios: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(Map.of(
                "status", "error",
                "message", "Error creando suscripción: " + e.getMessage()
            ));
        }
    }

    @PostMapping("/subscribe-deactivate")
    public ResponseEntity<?> subscribeToUserDeactivated() {
        log.info("Suscribiéndose a eventos de desactivación de usuarios...");

        try {
            coreHubService.subscribeToTopic(
                "users",
                "user",
                "user_deactivated"
            );

            log.info("Suscripción a desactivación de usuarios creada exitosamente");
            return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "Suscripción a desactivación de usuarios creada exitosamente",
                "topic", "user",
                "eventName", "user_deactivated"
            ));

        } catch (Exception e) {
            log.error("Error creando suscripción a desactivación de usuarios: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(Map.of(
                "status", "error",
                "message", "Error creando suscripción: " + e.getMessage()
            ));
        }
    }

    @PostMapping("/subscribe-rejected")
    public ResponseEntity<?> subscribeToUserRejected() {
        log.info("Suscribiéndose a eventos de rechazo de usuarios...");

        try {
            coreHubService.subscribeToTopic(
                "users",
                "user",
                "user_rejected"
            );

            log.info("Suscripción a rechazo de usuarios creada exitosamente");
            return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "Suscripción a rechazo de usuarios creada exitosamente",
                "topic", "user",
                "eventName", "user_rejected"
            ));

        } catch (Exception e) {
            log.error("Error creando suscripción a rechazo de usuarios: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(Map.of(
                "status", "error",
                "message", "Error creando suscripción: " + e.getMessage()
            ));
        }
    }

    @PostMapping("/subscribe-all")
    public ResponseEntity<?> subscribeToAllUserEvents() {
        log.info("Suscribiéndose a todos los eventos de usuarios...");

        try {
            coreHubService.subscribeToTopic("users", "user", "user_created");
            
            coreHubService.subscribeToTopic("users", "user", "user_updated");
            
            coreHubService.subscribeToTopic("users", "user", "user_deactivated");
            
            coreHubService.subscribeToTopic("users", "user", "user_rejected");

            log.info("Todas las suscripciones de usuarios creadas exitosamente");
            return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "Todas las suscripciones de usuarios creadas exitosamente",
                "subscriptions", new String[]{
                    "user.user_created",
                    "user.user_updated", 
                    "user.user_deactivated",
                    "user.user_rejected"
                }
            ));

        } catch (Exception e) {
            log.error("Error creando suscripciones de usuarios: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(Map.of(
                "status", "error",
                "message", "Error creando suscripciones: " + e.getMessage()
            ));
        }
    }

    @GetMapping("/status")
    public ResponseEntity<?> getUserSubscriptionStatus() {
        return ResponseEntity.ok(Map.of(
            "status", "active",
            "service", "User Event Subscriptions",
            "webhookUrl", "http://dev.desarrollo2-usuarios.shop:8082/api/core/webhook/user-events",
            "subscriptions", new String[]{
                "user.user_created",
                "user.user_updated", 
                "user.user_deactivated",
                "user.user_rejected"
            }
        ));
    }
}
