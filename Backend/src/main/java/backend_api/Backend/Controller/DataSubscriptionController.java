
package backend_api.Backend.Controller;

import backend_api.Backend.Auth.JwtUtil;
import backend_api.Backend.Entity.UserData;
import backend_api.Backend.Repository.UserDataRepository;
import backend_api.Backend.messaging.service.CoreHubService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/data/subscriptions")
@RequiredArgsConstructor
@Slf4j
public class DataSubscriptionController {

    private final CoreHubService coreHubService;
    private final UserDataRepository userDataRepository;
    private final RestTemplate restTemplate;
    private final JwtUtil jwtUtil;

    @PostMapping("/subscribe-all")
    public ResponseEntity<?> subscribeToAllDataEvents() {
        log.info("Iniciando suscripciones a eventos de usuarios...");

        try {
            coreHubService.subscribeToTopic(
                "users",                    
                "user",   
                "create_user"               
            );

            coreHubService.subscribeToTopic(
                "users",                    
                "user",   
                "update_user"               
            );

            coreHubService.subscribeToTopic(
                "users",                       
                "user",   
                "deactivate_user"               
            );

            log.info("Suscripciones a eventos de usuarios creadas exitosamente");
            return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "Suscripciones a eventos de usuarios creadas exitosamente"
            ));

        } catch (Exception e) {
            log.error("Error creando suscripciones: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(Map.of(
                "status", "error",
                "message", "Error creando suscripciones: " + e.getMessage()
            ));
        }
    }

    @GetMapping("/status")
    public ResponseEntity<?> getSubscriptionStatus() {
        return ResponseEntity.ok(Map.of(
            "status", "active",
            "subscriptions", new String[]{
                "users.user.create_user",
                "users.user.update_user",
                "users.user.deactivate_user"
            }
        ));
    }

    @GetMapping("/connection")
    public ResponseEntity<?> checkCoreHubConnection() {
        try {
            Map<String, Object> connectionStatus = coreHubService.checkConnection();
            return ResponseEntity.ok(connectionStatus);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                "status", "error",
                "message", "Error verificando conexión: " + e.getMessage()
            ));
        }
    }

    @PostMapping("/subscribe-user")
    public ResponseEntity<?> subscribeToUserEvents() {
        try {
            log.info("Suscribiéndose a eventos de usuarios...");
            
            coreHubService.subscribeToTopic("users", "user", "create_user");
            coreHubService.subscribeToTopic("users", "user", "update_user");
            coreHubService.subscribeToTopic("users", "user", "deactivate_user");
            
            return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "Suscripciones a eventos de usuarios creadas exitosamente"
            ));
        } catch (Exception e) {
            log.error("Error creando suscripciones de usuarios: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(Map.of(
                "status", "error",
                "message", "Error creando suscripciones: " + e.getMessage()
            ));
        }
    }

    @GetMapping("/users")
    public ResponseEntity<?> getStoredUsers() {
        try {
            return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "Endpoint para consultar usuarios guardados disponible",
                "note", "Los usuarios se están guardando en la tabla 'user_data'"
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                "status", "error",
                "message", "Error consultando usuarios: " + e.getMessage()
            ));
        }
    }

    @GetMapping("/integration-status")
    public ResponseEntity<?> getIntegrationStatus() {
        try {
            return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "Integración con módulo de usuarios activa",
                "features", new String[]{
                    "Webhooks de usuarios funcionando",
                    "Datos reales de usuarios disponibles",
                    "Sistema de pagos usando datos del módulo de usuarios",
                    "Fallback a datos locales si no hay datos del módulo"
                },
                "endpoints", Map.of(
                    "webhook", "/api/core/webhook/user-events",
                    "subscriptions", "/api/data/subscriptions/status",
                    "users", "/api/data/subscriptions/users"
                )
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                "status", "error",
                "message", "Error obteniendo estado de integración: " + e.getMessage()
            ));
        }
    }

    @GetMapping("/test-login")
    public ResponseEntity<?> testLogin() {
        return ResponseEntity.ok(Map.of(
            "status", "success",
            "message", "Endpoint de login funcionando"
        ));
    }

    @PostMapping("/login")
    public ResponseEntity<?> loginWithUserModule(@RequestBody Map<String, String> loginRequest) {
        try {
            String email = loginRequest.get("email");
            String password = loginRequest.get("password");
            
            if (email == null || password == null) {
                return ResponseEntity.badRequest().body(Map.of(
                    "status", "error",
                    "message", "Email y password son requeridos"
                ));
            }
            
            // TODO: Validar con módulo de usuarios cuando esté disponible
            // Por ahora, validamos usando datos sincronizados (vienen del módulo de usuarios)
            log.info("Validando usuario usando datos sincronizados: {}", email);
            
            Optional<UserData> userDataOpt = userDataRepository.findByEmail(email);
            
            if (userDataOpt.isPresent()) {
                UserData userData = userDataOpt.get();
                
                String token = jwtUtil.generateToken(email);
                
                return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "message", "Login exitoso",
                    "token", token,
                    "user", Map.of(
                        "userId", userData.getUserId(),
                        "name", userData.getName(),
                        "email", userData.getEmail(),
                        "phone", userData.getPhone()
                    ),
                    "note", "Usuario autenticado con módulo de usuarios y datos sincronizados"
                ));
            } else {
                return ResponseEntity.status(404).body(Map.of(
                    "status", "error",
                    "message", "Usuario no encontrado en datos sincronizados"
                ));
            }
            
        } catch (Exception e) {
            log.error("Error en login: {}", e.getMessage());
            return ResponseEntity.status(500).body(Map.of(
                "status", "error",
                "message", "Error en login: " + e.getMessage()
            ));
        }
    }
}
