package backend_api.Backend.Controller;

import backend_api.Backend.messaging.service.CoreHubService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/test/users")
@RequiredArgsConstructor
@Slf4j
public class TestUserSubscriptionController {

    private final CoreHubService coreHubService;

    @PostMapping("/subscribe-create")
    public ResponseEntity<?> testSubscribeToUserCreated() {
        log.info("🧪 TEST: Suscribiéndose a eventos de creación de usuarios...");

        try {
            coreHubService.subscribeToTopic(
                "users",                    // targetTeamName
                "user",                     // domain  
                "create_user"               // action
            );

            return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "✅ TEST: Suscripción a creación de usuarios exitosa",
                "topic", "users.user.create_user",
                "eventName", "create_user",
                "webhookUrl", "http://dev.desarrollo2-usuarios.shop:8081/api/core/webhook/user-events"
            ));

        } catch (Exception e) {
            log.error("❌ TEST: Error en suscripción a creación de usuarios: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(Map.of(
                "status", "error",
                "message", "❌ TEST: Error: " + e.getMessage()
            ));
        }
    }

    @PostMapping("/subscribe-update")
    public ResponseEntity<?> testSubscribeToUserUpdated() {
        log.info("🧪 TEST: Suscribiéndose a eventos de actualización de usuarios...");

        try {
            coreHubService.subscribeToTopic(
                "users",                    // targetTeamName
                "user",                     // domain
                "update_user"               // action
            );

            return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "✅ TEST: Suscripción a actualización de usuarios exitosa",
                "topic", "users.user.update_user",
                "eventName", "update_user",
                "webhookUrl", "http://dev.desarrollo2-usuarios.shop:8081/api/core/webhook/user-events"
            ));

        } catch (Exception e) {
            log.error("❌ TEST: Error en suscripción a actualización de usuarios: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(Map.of(
                "status", "error",
                "message", "❌ TEST: Error: " + e.getMessage()
            ));
        }
    }

    @PostMapping("/subscribe-deactivate")
    public ResponseEntity<?> testSubscribeToUserDeactivated() {
        log.info("🧪 TEST: Suscribiéndose a eventos de desactivación de usuarios...");

        try {
            coreHubService.subscribeToTopic(
                "users",                    // targetTeamName
                "user",                     // domain
                "deactivate_user"           // action
            );

            return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "✅ TEST: Suscripción a desactivación de usuarios exitosa",
                "topic", "users.user.deactivate_user",
                "eventName", "deactivate_user",
                "webhookUrl", "http://dev.desarrollo2-usuarios.shop:8081/api/core/webhook/user-events"
            ));

        } catch (Exception e) {
            log.error("❌ TEST: Error en suscripción a desactivación de usuarios: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(Map.of(
                "status", "error",
                "message", "❌ TEST: Error: " + e.getMessage()
            ));
        }
    }

    @PostMapping("/subscribe-all")
    public ResponseEntity<?> testSubscribeToAllUserEvents() {
        log.info("🧪 TEST: Suscribiéndose a TODOS los eventos de usuarios...");

        try {
            // Crear usuario
            coreHubService.subscribeToTopic("users", "user", "create_user");
            
            // Actualizar usuario
            coreHubService.subscribeToTopic("users", "user", "update_user");
            
            // Desactivar usuario
            coreHubService.subscribeToTopic("users", "user", "deactivate_user");

            return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "✅ TEST: Todas las suscripciones de usuarios creadas exitosamente",
                "subscriptions", new String[]{
                    "users.user.create_user",
                    "users.user.update_user", 
                    "users.user.deactivate_user"
                },
                "webhookUrl", "http://dev.desarrollo2-usuarios.shop:8081/api/core/webhook/user-events"
            ));

        } catch (Exception e) {
            log.error("❌ TEST: Error creando suscripciones de usuarios: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(Map.of(
                "status", "error",
                "message", "❌ TEST: Error: " + e.getMessage()
            ));
        }
    }

    @GetMapping("/status")
    public ResponseEntity<?> testUserSubscriptionStatus() {
        return ResponseEntity.ok(Map.of(
            "status", "active",
            "service", "🧪 TEST User Event Subscriptions",
            "webhookUrl", "http://dev.desarrollo2-usuarios.shop:8081/api/core/webhook/user-events",
            "subscriptions", new String[]{
                "users.user.create_user",
                "users.user.update_user",
                "users.user.deactivate_user"
            },
            "note", "Estos son endpoints de prueba para testing"
        ));
    }
}
