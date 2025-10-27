
package backend_api.Backend.Controller;

import backend_api.Backend.Auth.JwtUtil;
import backend_api.Backend.Entity.UserData;
import backend_api.Backend.Repository.UserDataRepository;
import backend_api.Backend.Service.Implementation.DataStorageServiceImpl;
import backend_api.Backend.messaging.service.CoreHubService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import backend_api.Backend.Repository.ProviderDataRepository;

import java.util.List;
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
    private final DataStorageServiceImpl dataStorageService;
    private final ProviderDataRepository providerDataRepository;

    @PostMapping("/subscribe-all")
    public ResponseEntity<?> subscribeToAllDataEvents() {
        log.info("Iniciando suscripciones a eventos de usuarios...");

        try {
            coreHubService.subscribeToTopic(
                "users",                    
                "user",   
                "user_created"               
            );

            coreHubService.subscribeToTopic(
                "users",                    
                "user",   
                "user_updated"               
            );

            coreHubService.subscribeToTopic(
                "users",                       
                "user",   
                "user_deactivated"               
            );
            
            coreHubService.subscribeToTopic(
                "users",                       
                "user",   
                "user_rejected"               
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

    @PostMapping("/subscribe-matching-payments")
    public ResponseEntity<?> subscribeToMatchingPayments() {
        log.info("Iniciando suscripción a solicitudes de pago de matching...");

        try {
            coreHubService.subscribeToTopic(
                "matching",                    
                "pago",   
                "emitida"               
            );

            log.info("Suscripción a solicitudes de pago de matching creada exitosamente");
            return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "Suscripción a solicitudes de pago de matching creada exitosamente",
                "topic", "pago",
                "eventName", "emitida",
                "webhookUrl", "https://3aadd844682e.ngrok-free.app/api/core/webhook/matching-payment-requests"
            ));

        } catch (Exception e) {
            log.error("Error creando suscripción a matching: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(Map.of(
                "status", "error",
                "message", "Error creando suscripción a matching: " + e.getMessage()
            ));
        }
    }

    @GetMapping("/status")
    public ResponseEntity<?> getSubscriptionStatus() {
        return ResponseEntity.ok(Map.of(
            "status", "active",
            "subscriptions", new String[]{
                "user.user_created",
                "user.user_updated",
                "user.user_deactivated",
                "user.user_rejected",
                "pago.emitida"
            },
            "webhooks", Map.of(
                "user-events", "/api/core/webhook/user-events",
                "payment-events", "/api/core/webhook/payment-events",
                "matching-payment-requests", "/api/core/webhook/matching-payment-requests"
            )
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

    @GetMapping("/users")
    public ResponseEntity<?> getStoredUsers() {
        try {
            List<UserData> users = userDataRepository.findAll();
            
            List<Map<String, Object>> userList = users.stream()
                .map(user -> {
                    Map<String, Object> userMap = new java.util.HashMap<>();
                    userMap.put("userId", user.getUserId());
                    userMap.put("name", user.getName());
                    userMap.put("email", user.getEmail());
                    userMap.put("phone", user.getPhone() != null ? user.getPhone() : "");
                    userMap.put("secondaryId", user.getSecondaryId() != null ? user.getSecondaryId() : "");
                    userMap.put("role", user.getRole());
                    userMap.put("saldoDisponible", user.getSaldoDisponible());
                    userMap.put("createdAt", user.getCreatedAt());
                    userMap.put("updatedAt", user.getUpdatedAt());
                    return userMap;
                })
                .collect(java.util.stream.Collectors.toList());
            
            return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "Usuarios sincronizados obtenidos exitosamente",
                "count", users.size(),
                "users", userList
            ));
        } catch (Exception e) {
            log.error("Error consultando usuarios: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(Map.of(
                "status", "error",
                "message", "Error consultando usuarios: " + e.getMessage()
            ));
        }
    }

    @GetMapping("/providers")
    public ResponseEntity<?> getStoredProviders() {
        try {
            var providers = providerDataRepository.findAll();

            var response = providers.stream()
                    .map(p -> {
                        Map<String, Object> providerMap = new java.util.HashMap<>();
                        providerMap.put("providerId", p.getProviderId());
                        providerMap.put("name", java.util.Optional.ofNullable(p.getName()).orElse(""));
                        providerMap.put("email", java.util.Optional.ofNullable(p.getEmail()).orElse(""));
                        providerMap.put("phone", java.util.Optional.ofNullable(p.getPhone()).orElse(""));
                        providerMap.put("secondaryId", java.util.Optional.ofNullable(p.getSecondaryId()).orElse(""));
                        providerMap.put("active", java.util.Optional.ofNullable(p.getActive()).orElse(Boolean.TRUE));
                        providerMap.put("photo", java.util.Optional.ofNullable(p.getPhoto()).orElse(""));

                        Map<String, Object> address = new java.util.HashMap<>();
                        address.put("state", java.util.Optional.ofNullable(p.getState()).orElse(""));
                        address.put("city", java.util.Optional.ofNullable(p.getCity()).orElse(""));
                        address.put("street", java.util.Optional.ofNullable(p.getStreet()).orElse(""));
                        address.put("number", java.util.Optional.ofNullable(p.getNumber()).orElse(""));
                        address.put("floor", java.util.Optional.ofNullable(p.getFloor()).orElse(""));
                        address.put("apartment", java.util.Optional.ofNullable(p.getApartment()).orElse(""));
                        providerMap.put("address", address);

                        providerMap.put("habilidades", java.util.Optional.ofNullable(p.getSkills()).orElse(java.util.List.of()));
                        providerMap.put("zonas", java.util.Optional.ofNullable(p.getZones()).orElse(java.util.List.of()));
                        providerMap.put("createdAt", p.getCreatedAt());
                        providerMap.put("updatedAt", p.getUpdatedAt());

                        return providerMap;
                    })
                    .collect(java.util.stream.Collectors.toList());

            return ResponseEntity.ok(java.util.Map.of(
                    "status", "success",
                    "count", providers.size(),
                    "providers", response
            ));
        } catch (Exception e) {
            log.error("Error consultando providers: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(java.util.Map.of(
                    "status", "error",
                    "message", "Error consultando providers: " + e.getMessage()
            ));
        }
    }

    @GetMapping("/logs")
    public ResponseEntity<?> getRecentLogs() {
        try {
            return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "Para ver los logs en tiempo real, ejecuta:",
                "commands", new String[]{
                    "tail -f logs/application.log | grep -E '(Pago|Payment|CORE|Evento)'",
                    "grep -E '(Pago|Payment|CORE|Evento)' logs/application.log | tail -20",
                    "docker logs -f <container_name> | grep -E '(Pago|Payment|CORE|Evento)'"
                },
                "note", "Los logs muestran cuando se envían eventos al CORE Hub",
                "lookFor", new String[]{
                    "🚀 Enviando evento de pago al CORE Hub",
                    "✅ Evento enviado al CORE Hub exitosamente",
                    "❌ Error enviando evento al CORE Hub"
                }
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                "status", "error",
                "message", "Error obteniendo información de logs: " + e.getMessage()
            ));
        }
    }

    @GetMapping("/payment-events-status")
    public ResponseEntity<?> getPaymentEventsStatus() {
        try {
            return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "Estado de eventos de pagos",
                "features", new String[]{
                    "Envío de eventos de pagos al CORE Hub",
                    "Confirmación de recepción de eventos",
                    "Logs detallados de envío",
                    "Reintentos automáticos en caso de fallo"
                },
                "endpoints", Map.of(
                    "sendEvent", "/api/data/subscriptions/payment-events",
                    "webhook", "/api/core/webhook/payment-events",
                    "status", "/api/data/subscriptions/payment-events-status"
                ),
                "lastEventSent", "N/A - Usar payment-events para enviar eventos"
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                "status", "error",
                "message", "Error obteniendo estado de eventos de pagos: " + e.getMessage()
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

    @GetMapping("/core-hub-verification")
    public ResponseEntity<?> verifyCoreHubReception() {
        try {
            // Verificar conexión al CORE Hub
            Map<String, Object> connectionStatus = coreHubService.checkConnection();
            
            return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "Verificación de recepción en CORE Hub",
                "coreHubConnection", connectionStatus,
                "verificationSteps", new String[]{
                    "1. Revisa los logs de tu aplicación para ver '✅ Mensaje publicado exitosamente al CORE'",
                    "2. Verifica que aparezca '📋 Respuesta del CORE Hub: {...}' en los logs",
                    "3. Si hay errores, aparecerá '❌ Error publicando mensaje al CORE'",
                    "4. El CORE Hub debería confirmar la recepción con un status 200"
                },
                "logPatterns", Map.of(
                    "success", "✅ Mensaje publicado exitosamente al CORE",
                    "response", "📋 Respuesta del CORE Hub:",
                    "error", "❌ Error publicando mensaje al CORE"
                ),
                "note", "Los logs muestran si el CORE Hub recibió y procesó el mensaje correctamente"
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                "status", "error",
                "message", "Error verificando CORE Hub: " + e.getMessage()
            ));
        }
    }


    @PostMapping("/payment-events")
    public ResponseEntity<?> sendPaymentEvent(@RequestBody Map<String, Object> paymentData) {
        try {
            log.info("🚀 Enviando evento de pago al CORE Hub: {}", paymentData);
            
            Long paymentId = Long.valueOf(paymentData.get("paymentId").toString());
            String status = (String) paymentData.get("status");
            String amount = paymentData.get("amount").toString();
            String currency = (String) paymentData.get("currency");
            Long userId = Long.valueOf(paymentData.get("userId").toString());
            
            backend_api.Backend.messaging.dto.CoreResponseMessage coreMessage = 
                new backend_api.Backend.messaging.dto.CoreResponseMessage();
            
            coreMessage.setMessageId("payment_" + paymentId + "_" + System.currentTimeMillis());
            coreMessage.setTimestamp(java.time.Instant.now().toString().substring(0, 23) + "Z");
            
            backend_api.Backend.messaging.dto.CoreResponseMessage.Destination destination = 
                new backend_api.Backend.messaging.dto.CoreResponseMessage.Destination();
            destination.setTopic("payment"); 
            destination.setEventName("status_updated");
            coreMessage.setDestination(destination);
            
            // Crear payload
            Map<String, Object> payload = new java.util.HashMap<>();
            payload.put("paymentId", paymentId);
            payload.put("userId", userId);
            payload.put("status", status);
            payload.put("amount", amount);
            payload.put("currency", currency);
            payload.put("timestamp", java.time.LocalDateTime.now().toString());
            payload.put("gatewayTxnId", "gw_" + paymentId);
            payload.put("reason", "Payment processed successfully");
            
            coreMessage.setPayload(payload);
            
            log.info("📊 Mensaje CORE Hub creado: {}", coreMessage);
            
            // Enviar al CORE Hub
            try {
                Map<String, Object> coreHubResponse = coreHubService.publishMessage(coreMessage);
                
                if ((Boolean) coreHubResponse.get("success")) {
                    log.info("✅ Evento enviado al CORE Hub exitosamente - MessageId: {}", coreMessage.getMessageId());
                    
                    return ResponseEntity.ok(Map.of(
                        "status", "success",
                        "message", "Evento de pago enviado al CORE Hub exitosamente",
                        "paymentId", paymentId,
                        "userId", userId,
                        "messageId", coreMessage.getMessageId(),
                        "coreHubResponse", coreHubResponse
                    ));
                } else {
                    log.error("❌ Error enviando evento al CORE Hub: {}", coreHubResponse.get("error"));
                    return ResponseEntity.status(500).body(Map.of(
                        "status", "error",
                        "message", "Error enviando evento al CORE Hub: " + coreHubResponse.get("error"),
                        "coreHubResponse", coreHubResponse
                    ));
                }
                
            } catch (Exception e) {
                log.error("❌ Error enviando evento al CORE Hub: {}", e.getMessage(), e);
                return ResponseEntity.status(500).body(Map.of(
                    "status", "error",
                    "message", "Error enviando evento al CORE Hub: " + e.getMessage()
                ));
            }
            
        } catch (Exception e) {
            log.error("Error enviando evento de pago: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(Map.of(
                "status", "error",
                "message", "Error enviando evento de pago: " + e.getMessage()
            ));
        }
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
            
            log.info("Validando usuario usando datos sincronizados: {}", email);
            
            // Manejar duplicados temporalmente - tomar el más reciente
            List<UserData> userDataList = userDataRepository.findAllByEmail(email);
            Optional<UserData> userDataOpt = userDataList.isEmpty() ? 
                Optional.empty() : 
                Optional.of(userDataList.get(0)); // Tomar el primero (más reciente por orden de creación)
            
            if (userDataOpt.isPresent()) {
                UserData userData = userDataOpt.get();
                
                // Validar contraseña contra el módulo de usuarios
                boolean passwordValid = validatePasswordWithUserModule(email, password);
                if (!passwordValid) {
                    log.warn("Contraseña inválida para usuario: {}", email);
                    return ResponseEntity.status(401).body(Map.of(
                        "status", "error",
                        "message", "Credenciales inválidas"
                    ));
                }
                
                log.info("Usuario autenticado exitosamente: userId={}, name={}, role={}", 
                    userData.getUserId(), userData.getName(), userData.getRole());
                
                String systemRole = convertUserModuleRoleToSystemRole(userData.getRole());
                String token = jwtUtil.generateToken(email, 86400000L, List.of(systemRole));
                
                return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "message", "Login exitoso",
                    "token", token,
                    "user", Map.of(
                        "userId", userData.getUserId(),
                        "name", userData.getName(),
                        "email", userData.getEmail(),
                        "phone", userData.getPhone(),
                        "secondaryId", userData.getSecondaryId()
                    ),
                    "source", "USER_MODULE_SYNC",
                    "note", "Usuario autenticado con datos sincronizados del módulo de usuarios"
                ));
            } else {
                log.warn("Usuario no encontrado en datos sincronizados: {}", email);
                return ResponseEntity.status(404).body(Map.of(
                    "status", "error",
                    "message", "Usuario no encontrado. Asegúrate de que el usuario esté registrado en el módulo de usuarios y que la sincronización esté funcionando."
                ));
            }
            
        } catch (Exception e) {
            log.error("Error en login: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(Map.of(
                "status", "error",
                "message", "Error en login: " + e.getMessage()
            ));
        }
    }

    private boolean validatePasswordWithUserModule(String email, String password) {
        try {
            // URL del módulo de usuarios
            String userModuleUrl = "http://dev.desarrollo2-usuarios.shop:8081/api/users/login";
            
            // Crear request body
            Map<String, String> loginRequest = Map.of(
                "email", email,
                "password", password
            );
            
            // Configurar headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            // Crear entidad HTTP
            HttpEntity<Map<String, String>> requestEntity = new HttpEntity<>(loginRequest, headers);
            
            // Hacer petición POST
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                userModuleUrl, 
                HttpMethod.POST,
                requestEntity, 
                new org.springframework.core.ParameterizedTypeReference<Map<String, Object>>() {}
            );
            
            // Verificar si la respuesta es exitosa (200-299)
            return response.getStatusCode().is2xxSuccessful();
            
        } catch (Exception e) {
            log.error("Error validando contraseña con módulo de usuarios: {}", e.getMessage());
            return false;
        }
    }

    @GetMapping("/users/{email}/role")
    public ResponseEntity<Map<String, Object>> getUserRole(@PathVariable String email) {
        try {
            // Manejar duplicados temporalmente - tomar el más reciente
            List<UserData> userDataList = userDataRepository.findAllByEmail(email);
            Optional<UserData> userDataOpt = userDataList.isEmpty() ? 
                Optional.empty() : 
                Optional.of(userDataList.get(0)); // Tomar el primero (más reciente por orden de creación)
            if (userDataOpt.isPresent()) {
                UserData userData = userDataOpt.get();
                String systemRole = convertUserModuleRoleToSystemRole(userData.getRole());
                return ResponseEntity.ok(Map.of(
                    "email", userData.getEmail(),
                    "role", userData.getRole(),
                    "systemRole", systemRole,
                    "userId", userData.getUserId(),
                    "name", userData.getName()
                ));
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("Error obteniendo rol del usuario: {}", e.getMessage());
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/sync-user/{email}")
    public ResponseEntity<?> syncUserFromModule(@PathVariable String email) {
        try {
            log.info("Sincronizando usuario desde módulo de usuarios: {}", email);
            
            // Obtener datos del usuario desde el módulo de usuarios
            Map<String, Object> userData = getUserDataFromUserModule(email);
            
            if (userData == null) {
                return ResponseEntity.status(404).body(Map.of(
                    "status", "error",
                    "message", "Usuario no encontrado en el módulo de usuarios"
                ));
            }
            
            // Procesar como evento de creación de usuario
            Long userId = Long.valueOf(userData.get("id").toString());
            String firstName = (String) userData.get("firstName");
            String lastName = (String) userData.get("lastName");
            String phoneNumber = (String) userData.get("phoneNumber");
            String role = (String) userData.get("role");
            String dni = (String) userData.get("dni");
            
            // Crear datos para guardar
            Map<String, Object> userDataToSave = new java.util.HashMap<>();
            userDataToSave.put("name", firstName + " " + lastName);
            userDataToSave.put("email", email);
            userDataToSave.put("phone", phoneNumber);
            userDataToSave.put("role", role);
            userDataToSave.put("dni", dni);
            
            // Generar saldo aleatorio
            java.util.Random random = new java.util.Random();
            double saldo = 10000 + (random.nextDouble() * 40000);
            userDataToSave.put("saldoDisponible", java.math.BigDecimal.valueOf(saldo).setScale(2, java.math.RoundingMode.HALF_UP));
            
            // Guardar en base de datos
            dataStorageService.saveUserData(userId, userDataToSave, "manual-sync-" + System.currentTimeMillis());
            
            log.info("Usuario sincronizado exitosamente: userId={}, email={}", userId, email);
            
            return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "Usuario sincronizado exitosamente",
                "userId", userId,
                "email", email,
                "name", firstName + " " + lastName,
                "phone", phoneNumber,
                "role", role
            ));
            
        } catch (Exception e) {
            log.error("Error sincronizando usuario: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(Map.of(
                "status", "error",
                "message", "Error sincronizando usuario: " + e.getMessage()
            ));
        }
    }
    
    private Map<String, Object> getUserDataFromUserModule(String email) {
        try {
            // Hacer login para obtener datos del usuario
            String userModuleUrl = "http://dev.desarrollo2-usuarios.shop:8081/api/users/login";
            
            Map<String, String> loginRequest = Map.of(
                "email", email,
                "password", "123456" // Contraseña por defecto para sincronización
            );
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            HttpEntity<Map<String, String>> requestEntity = new HttpEntity<>(loginRequest, headers);
            
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                userModuleUrl, 
                HttpMethod.POST,
                requestEntity, 
                new org.springframework.core.ParameterizedTypeReference<Map<String, Object>>() {}
            );
            
            if (response.getStatusCode().is2xxSuccessful()) {
                Map<String, Object> responseBody = response.getBody();
                if (responseBody != null) {
                    Object userInfoObj = responseBody.get("userInfo");
                    if (userInfoObj instanceof Map) {
                        @SuppressWarnings("unchecked")
                        Map<String, Object> userInfo = (Map<String, Object>) userInfoObj;
                        log.info("Datos obtenidos del módulo de usuarios: {}", userInfo);
                        return userInfo;
                    }
                }
            }
            
            return null;
            
        } catch (Exception e) {
            log.error("Error obteniendo datos del módulo de usuarios: {}", e.getMessage());
            return null;
        }
    }

    private String convertUserModuleRoleToSystemRole(String userModuleRole) {
        if (userModuleRole == null) {
            return "USER";
        }
        
        switch (userModuleRole.toUpperCase()) {
            case "CLIENTE":
                return "USER";
            case "PRESTADOR":
                return "MERCHANT";
            case "ADMIN":
                return "ADMIN";
            default:
                return "USER";
        }
    }
}
