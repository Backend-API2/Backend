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
                "users.user.create_user",
                "create_user"
            );

            log.info("Suscripción a creación de usuarios creada exitosamente");
            return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "Suscripción a creación de usuarios creada exitosamente",
                "topic", "users.user.create_user",
                "eventName", "create_user"
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
                "users.user.update_user",
                "update_user"
            );

            log.info("Suscripción a actualización de usuarios creada exitosamente");
            return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "Suscripción a actualización de usuarios creada exitosamente",
                "topic", "users.user.update_user",
                "eventName", "update_user"
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
                "users.user.deactivate_user",
                "deactivate_user"
            );

            log.info("Suscripción a desactivación de usuarios creada exitosamente");
            return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "Suscripción a desactivación de usuarios creada exitosamente",
                "topic", "users.user.deactivate_user",
                "eventName", "deactivate_user"
            ));

        } catch (Exception e) {
            log.error("Error creando suscripción a desactivación de usuarios: {}", e.getMessage());
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
            coreHubService.subscribeToTopic("users", "users.user.create_user", "create_user");
            
            coreHubService.subscribeToTopic("users", "users.user.update_user", "update_user");
            
            coreHubService.subscribeToTopic("users", "users.user.deactivate_user", "deactivate_user");

            log.info("Todas las suscripciones de usuarios creadas exitosamente");
            return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "Todas las suscripciones de usuarios creadas exitosamente",
                "subscriptions", new String[]{
                    "users.user.create_user",
                    "users.user.update_user", 
                    "users.user.deactivate_user"
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
            "webhookUrl", "http://dev.desarrollo2-usuarios.shop:8081/api/core/webhook/user-events",
            "subscriptions", new String[]{
                "users.user.create_user",
                "users.user.update_user",
                "users.user.deactivate_user"
            }
        ));
    }
}
