package backend_api.Backend.Controller;

import backend_api.Backend.messaging.service.CoreHubService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/data/subscriptions")
@RequiredArgsConstructor
@Slf4j
public class DataSubscriptionController {

    private final CoreHubService coreHubService;

    @PostMapping("/subscribe-all")
    public ResponseEntity<?> subscribeToAllDataEvents() {
        log.info("Iniciando suscripciones a todos los eventos de datos...");

        try {
            coreHubService.subscribeToTopic(
                "users",                    
                "users.user.create_user",   
                "create_user"               
            );

            coreHubService.subscribeToTopic(
                "users",                    
                "users.user.update_user",   
                "update_user"               
            );

            coreHubService.subscribeToTopic(
                "users",                       
                "users.user.deactivate_user",   
                "deactivate_user"               
            );

            coreHubService.subscribeToTopic(
                "providers",           
                "providers.provider.created",          
                "created"             
            );

            coreHubService.subscribeToTopic(
                "providers",           
                "providers.provider.updated",          
                "updated"             
            );

            coreHubService.subscribeToTopic(
                "matching",           
                "matching.solicitud.created",          
                "created"             
            );

            coreHubService.subscribeToTopic(
                "matching",           
                "matching.solicitud.updated",          
                "updated"             
            );

            log.info("Todas las suscripciones creadas exitosamente");
            return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "Suscripciones a eventos de datos creadas exitosamente"
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
                "users.user.deactivate_user",
                "providers.provider.created",
                "providers.provider.updated",
                "matching.solicitud.created",
                "matching.solicitud.updated"
            }
        ));
    }
}
