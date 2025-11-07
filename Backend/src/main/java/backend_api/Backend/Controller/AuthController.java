package backend_api.Backend.Controller;

import backend_api.Backend.DTO.auth.LoginRequest;
import backend_api.Backend.DTO.auth.RegisterRequest;
import backend_api.Backend.DTO.auth.AuthResponse;
import backend_api.Backend.Entity.user.User;
import backend_api.Backend.Entity.user.UserRole;
import backend_api.Backend.Entity.UserData;
import backend_api.Backend.Entity.ProviderData;
import backend_api.Backend.Repository.UserRepository;
import backend_api.Backend.Repository.UserDataRepository;
import backend_api.Backend.Repository.ProviderDataRepository;
import backend_api.Backend.Auth.JwtUtil;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.util.Random;
import java.util.Map;
import java.util.Optional;
import java.util.List;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
@Tag(name = "Autenticación", description = "Endpoints para registro, login y gestión de perfiles de usuario")
@Slf4j
public class AuthController {

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private UserDataRepository userDataRepository;
    
    @Autowired
    private ProviderDataRepository providerDataRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    @Autowired
    private RestTemplate restTemplate;

    @Operation(
        summary = "Registrar nuevo usuario",
        description = "Crea una nueva cuenta de usuario en el sistema. Los usuarios tipo USER reciben un saldo inicial aleatorio entre $10,000 y $50,000."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201",
            description = "Usuario registrado exitosamente",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = AuthResponse.class),
                examples = @ExampleObject(
                    name = "Respuesta exitosa",
                    value = """
                    {
                        "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
                        "userId": 1,
                        "email": "usuario@example.com",
                        "name": "Juan Pérez",
                        "role": "USER"
                    }
                    """
                )
            )
        ),
        @ApiResponse(
            responseCode = "409",
            description = "El email ya está registrado",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = String.class),
                examples = @ExampleObject(
                    name = "Email duplicado",
                    value = "Conflict"
                )
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Datos de entrada inválidos",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = String.class),
                examples = @ExampleObject(
                    name = "Validación fallida",
                    value = "Bad Request"
                )
            )
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Error interno del servidor",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = String.class),
                examples = @ExampleObject(
                    name = "Error interno",
                    value = "Internal Server Error"
                )
            )
        )
    })
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(
        @Parameter(
            description = "Datos del nuevo usuario",
            required = true,
            schema = @Schema(implementation = RegisterRequest.class)
        )
        @Valid @RequestBody RegisterRequest request) {
        try {
            if (userRepository.existsByEmail(request.getEmail())) {
                return new ResponseEntity<>(HttpStatus.CONFLICT); 
            }
            
            User user = new User();
            user.setEmail(request.getEmail());
            user.setPassword(passwordEncoder.encode(request.getPassword()));
            user.setName(request.getName());
            user.setPhone(request.getPhone());
            
            try {
                user.setRole(UserRole.valueOf(request.getRole().toUpperCase()));
            } catch (Exception e) {
                user.setRole(UserRole.USER); // Por defecto USER
            }
            
            if (user.getRole() == UserRole.USER) {
                Random random = new Random();
                double saldo = 10000 + (random.nextDouble() * 40000);
                user.setSaldo_disponible(BigDecimal.valueOf(saldo).setScale(2, java.math.RoundingMode.HALF_UP));
            }
            
            User savedUser = userRepository.save(user);
            
            // Generate token with appropriate role
            String token = jwtUtil.generateToken(savedUser.getEmail(), 86400000L, List.of(savedUser.getRole().toString()));
            
            AuthResponse response = new AuthResponse(
                token, 
                savedUser.getId(), 
                savedUser.getEmail(), 
                savedUser.getName(), 
                savedUser.getRole().toString()
            );
            
            return new ResponseEntity<>(response, HttpStatus.CREATED);
            
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Operation(
        summary = "Iniciar sesión",
        description = "Autentica un usuario existente y retorna un token JWT para acceder a endpoints protegidos."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Login exitoso",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = AuthResponse.class),
                examples = @ExampleObject(
                    name = "Login exitoso",
                    value = """
                    {
                        "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
                        "userId": 1,
                        "email": "usuario@example.com",
                        "name": "Juan Pérez",
                        "role": "USER"
                    }
                    """
                )
            )
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Credenciales inválidas",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = String.class),
                examples = @ExampleObject(
                    name = "Credenciales inválidas",
                    value = "Unauthorized"
                )
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Datos de entrada inválidos",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = String.class),
                examples = @ExampleObject(
                    name = "Validación fallida",
                    value = "Bad Request"
                )
            )
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Error interno del servidor",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = String.class),
                examples = @ExampleObject(
                    name = "Error interno",
                    value = "Internal Server Error"
                )
            )
        )
    })
    @PostMapping("/login")
    public ResponseEntity<?> login(
        @Parameter(
            description = "Credenciales de login",
            required = true,
            schema = @Schema(implementation = LoginRequest.class)
        )
        @Valid @RequestBody LoginRequest request) {
        try {
            String email = request.getEmail();
            String password = request.getPassword();
            
            // 1. Primero buscar en usuarios sincronizados (user_data table) - PRIORIDAD
            log.info("🔍 Buscando usuario sincronizado para email: {}", email);
            Optional<UserData> syncedUser;
            UserData userData = null;
            try {
                syncedUser = userDataRepository.findFirstByEmail(email); // Usar findFirst para evitar NonUniqueResultException
                log.info("🔍 Resultado búsqueda user_data: {}", syncedUser.isPresent() ? "ENCONTRADO" : "NO ENCONTRADO");
                
                // Cerrar la transacción de lectura antes de la llamada HTTP externa
                if (syncedUser.isPresent()) {
                    userData = syncedUser.get();
                    log.info("✅ Usuario sincronizado encontrado - userId: {}, email: {}, name: {}, role: {}", 
                        userData.getUserId(), userData.getEmail(), userData.getName(), userData.getRole());
                }
            } catch (Exception e) {
                log.error("❌ Error buscando en user_data: {}", e.getMessage(), e);
                syncedUser = Optional.empty();
            }
            
            // Validar usuario sincronizado (fuera de la transacción de DB para evitar connection leak)
            if (userData != null) {
                try {
                    // Validar si el usuario está activo
                    if (userData.getActive() == null || !userData.getActive()) {
                        log.warn("❌ Usuario desactivado intentando hacer login: {}", email);
                        Map<String, Object> errorResponse = new java.util.HashMap<>();
                        errorResponse.put("error", "Usuario desactivado");
                        errorResponse.put("message", "Su cuenta ha sido desactivada. Por favor, contacte al administrador para más información.");
                        errorResponse.put("code", "USER_DEACTIVATED");
                        errorResponse.put("active", false);
                        return new ResponseEntity<Map<String, Object>>(errorResponse, HttpStatus.FORBIDDEN);
                    }
                    
                    // Para usuarios sincronizados, DEBE validarse contra el módulo externo
                    // NO hay fallback inseguro - si el módulo no responde, el login falla
                    boolean passwordValid = validatePasswordWithUserModule(email, password);
                    
                    log.info("🔐 Validación de contraseña: {}", passwordValid);
                    
                    if (passwordValid) {
                        String systemRole = convertUserModuleRoleToSystemRole(userData.getRole());
                        log.info("🔄 Rol convertido de '{}' a '{}'", userData.getRole(), systemRole);
                        
                        // Validar que no haya valores nulos antes de generar token
                        String userName = userData.getName() != null ? userData.getName() : "Usuario";
                        log.info("📝 Usando nombre: {}", userName);
                        
                        String token = jwtUtil.generateToken(userData.getEmail(), 86400000L, List.of(systemRole));
                        AuthResponse response = new AuthResponse(
                            token, 
                            userData.getUserId(), 
                            userData.getEmail(), 
                            userName, 
                            systemRole
                        );
                        log.info("🎉 Login exitoso con usuario sincronizado - userId: {}", userData.getUserId());
                        return new ResponseEntity<>(response, HttpStatus.OK);
                    } else {
                        log.warn("❌ Contraseña inválida o módulo de usuarios no disponible para usuario sincronizado: {}", email);
                        return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
                    }
                } catch (Exception e) {
                    log.error("❌ Error procesando usuario sincronizado: {}", e.getMessage(), e);
                    return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
                }
            } else {
                log.info("❌ Usuario sincronizado NO encontrado para email: {}", email);
            }
            
            // 1b. Buscar también en provider_data (prestadores sincronizados)
            ProviderData providerData = null;
            try {
                Optional<ProviderData> syncedProvider = providerDataRepository.findByEmail(email);
                log.info("🔍 Buscando prestador sincronizado para email: {}", email);
                if (syncedProvider.isPresent()) {
                    providerData = syncedProvider.get();
                    log.info("✅ Prestador sincronizado encontrado - providerId: {}, email: {}", 
                        providerData.getProviderId(), providerData.getEmail());
                }
            } catch (Exception e) {
                log.error("❌ Error buscando prestador sincronizado: {}", e.getMessage(), e);
            }
            
            // Validar prestador sincronizado (fuera de la transacción de DB)
            if (providerData != null) {
                // Validar si el prestador está activo
                if (providerData.getActive() == null || !providerData.getActive()) {
                    log.warn("❌ Prestador desactivado intentando hacer login: {}", email);
                    Map<String, Object> errorResponse = new java.util.HashMap<>();
                    errorResponse.put("error", "Prestador desactivado");
                    errorResponse.put("message", "Su cuenta ha sido desactivada. Por favor, contacte al administrador para más información.");
                    errorResponse.put("code", "PROVIDER_DEACTIVATED");
                    errorResponse.put("active", false);
                    return new ResponseEntity<Map<String, Object>>(errorResponse, HttpStatus.FORBIDDEN);
                }
                
                // Validar contraseña con el módulo externo - NO hay fallback inseguro
                boolean passwordValid = validatePasswordWithUserModule(email, password);
                
                log.info("🔐 Validación de contraseña para prestador: {}", passwordValid);
                
                if (passwordValid) {
                    // Prestadores son MERCHANT por defecto
                    String token = jwtUtil.generateToken(providerData.getEmail(), 86400000L, List.of("MERCHANT"));
                    AuthResponse response = new AuthResponse(
                        token, 
                        providerData.getProviderId(), 
                        providerData.getEmail(), 
                        providerData.getName(), 
                        "MERCHANT"
                    );
                    log.info("🎉 Login exitoso con prestador sincronizado - providerId: {}", providerData.getProviderId());
                    return new ResponseEntity<>(response, HttpStatus.OK);
                } else {
                    log.warn("❌ Contraseña inválida o módulo de usuarios no disponible para prestador sincronizado: {}", email);
                    return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
                }
            } else {
                log.info("❌ Prestador sincronizado NO encontrado para email: {}", email);
            }
            
            // 2. Si no está en usuarios sincronizados, buscar en usuarios locales (users table)
            Optional<User> localUser = userRepository.findByEmail(email);
            if (localUser.isPresent()) {
                User user = localUser.get();
                log.info("✅ Usuario local encontrado - userId: {}, email: {}, role: {}", user.getId(), user.getEmail(), user.getRole());
                if (passwordEncoder.matches(password, user.getPassword())) {
                    String token = jwtUtil.generateToken(user.getEmail(), 86400000L, List.of(user.getRole().toString()));
                    log.info("🎉 Token generado con rol: {}", user.getRole().toString());
                    AuthResponse response = new AuthResponse(
                        token, 
                        user.getId(), 
                        user.getEmail(), 
                        user.getName(), 
                        user.getRole().toString()
                    );
                    return new ResponseEntity<>(response, HttpStatus.OK);
                } else {
                    return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
                }
            } else {
                // 3. Usuario no sincronizado - validar y sincronizar automáticamente
                Map<String, Object> userModuleData = validateAndGetUserDataFromUserModule(email, password);
                if (userModuleData != null) {
                    UserData newUser = createUserFromModuleData(email, userModuleData);
                    if (newUser != null) {
                        String systemRole = convertUserModuleRoleToSystemRole(newUser.getRole());
                        String token = jwtUtil.generateToken(newUser.getEmail(), 86400000L, List.of(systemRole));
                        AuthResponse response = new AuthResponse(
                            token, 
                            newUser.getUserId(), 
                            newUser.getEmail(), 
                            newUser.getName(), 
                            systemRole
                        );
                        return new ResponseEntity<>(response, HttpStatus.OK);
                    }
                }
            }
            
            // 4. Usuario no encontrado en ninguna tabla
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
            
        } catch (Exception e) {
            // En caso de error inesperado, devolver 500 para excepciones de base de datos
            // y 401 para otros errores de autenticación
            if (e instanceof DataAccessException || 
                e.getMessage() != null && (
                    e.getMessage().toLowerCase().contains("database") ||
                    e.getMessage().toLowerCase().contains("sql") ||
                    e.getMessage().toLowerCase().contains("connection")
                )) {
                return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
            }
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
    }
    
    private String convertUserModuleRoleToSystemRole(String userModuleRole) {
        log.debug("🔄 Convirtiendo rol del módulo: '{}'", userModuleRole);
        
        if (userModuleRole == null || userModuleRole.trim().isEmpty()) {
            log.warn("⚠️ Rol nulo o vacío, usando USER por defecto");
            return "USER";
        }
        
        String normalizedRole = userModuleRole.trim().toUpperCase();
        log.debug("🔄 Rol normalizado: '{}'", normalizedRole);
        
        switch (normalizedRole) {
            case "CLIENTE":
            case "USER":
                log.debug("✅ Rol CLIENTE/USER convertido a USER");
                return "USER";
            case "PRESTADOR":
            case "MERCHANT":
                log.debug("✅ Rol PRESTADOR/MERCHANT convertido a MERCHANT");
                return "MERCHANT";
            case "ADMIN":
                log.debug("✅ Rol ADMIN se mantiene como ADMIN");
                return "ADMIN";
            default:
                log.warn("⚠️ Rol desconocido '{}', usando USER por defecto", normalizedRole);
                return "USER";
        }
    }

    /**
     * Valida la contraseña con el módulo de usuarios externo
     */
    private boolean validatePasswordWithUserModule(String email, String password) {
        try {
            // Solo intentar validar si tenemos los datos necesarios
            if (email == null || password == null || email.trim().isEmpty() || password.trim().isEmpty()) {
                return false;
            }
            
            String userModuleUrl = "http://dev.desarrollo2-usuarios.shop:8081/api/users/login";
            Map<String, String> loginRequest = Map.of(
                "email", email,
                "password", password
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
            return response.getStatusCode().is2xxSuccessful();
        } catch (org.springframework.web.client.ResourceAccessException e) {
            // Timeout o problemas de conexión
            if (e.getMessage() != null && e.getMessage().contains("timeout")) {
                log.warn("⏱️ Timeout validando contraseña con módulo de usuarios para {}: {}", email, e.getMessage());
            } else {
                log.warn("🔌 Error de conexión validando contraseña con módulo de usuarios para {}: {}", email, e.getMessage());
            }
            return false;
        } catch (Exception e) {
            // En caso de cualquier otro error, devolver false
            log.warn("❌ Error validando contraseña con módulo de usuarios para {}: {}", email, e.getMessage());
            return false;
        }
    }
    
    /**
     * Valida credenciales y obtiene datos del usuario del módulo de usuarios
     */
    private Map<String, Object> validateAndGetUserDataFromUserModule(String email, String password) {
        try {
            String userModuleUrl = "http://dev.desarrollo2-usuarios.shop:8081/api/users/login";
            Map<String, String> loginRequest = Map.of(
                "email", email,
                "password", password
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
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Map<String, Object> responseData = response.getBody();
                log.info("Datos obtenidos del módulo de usuarios para {}: {}", email, responseData);
                
                // El módulo devuelve un userInfo anidado, extraerlo
                @SuppressWarnings("unchecked")
                Map<String, Object> userInfo = (Map<String, Object>) responseData.get("userInfo");
                if (userInfo == null) {
                    log.warn("No se encontró userInfo en la respuesta del módulo");
                    return null;
                }
                
                // Mapear los datos del módulo de usuarios a nuestro formato
                // Basado en la estructura de UserCreatedMessage
                Map<String, Object> mappedData = new java.util.HashMap<>();
                mappedData.put("userId", userInfo.get("id")); // El campo se llama id en userInfo
                
                String firstName = userInfo.get("firstName") != null ? userInfo.get("firstName").toString() : "";
                String lastName = userInfo.get("lastName") != null ? userInfo.get("lastName").toString() : "";
                String fullName = (firstName + " " + lastName).trim();
                mappedData.put("name", fullName.isEmpty() ? "Usuario Sincronizado" : fullName);
                
                mappedData.put("phone", userInfo.get("phoneNumber"));
                mappedData.put("role", userInfo.get("role"));
                mappedData.put("secondaryId", userInfo.get("dni"));
                
                return mappedData;
            }
            
        } catch (org.springframework.web.client.ResourceAccessException e) {
            // Timeout o problemas de conexión
            if (e.getMessage() != null && e.getMessage().contains("timeout")) {
                log.warn("⏱️ Timeout obteniendo datos del usuario {} del módulo de usuarios: {}", email, e.getMessage());
            } else {
                log.warn("🔌 Error de conexión obteniendo datos del usuario {} del módulo de usuarios: {}", email, e.getMessage());
            }
            return null;
        } catch (Exception e) {
            log.warn("❌ Error obteniendo datos del usuario {} del módulo de usuarios: {}", email, e.getMessage());
            
            // En caso de error, no crear datos de prueba - devolver null
            // para que el login falle correctamente
            return null;
        }
        
        return null;
    }
    
    
    /**
     * Crea un UserData a partir de los datos del módulo de usuarios
     */
    private UserData createUserFromModuleData(String email, Map<String, Object> userModuleData) {
        try {
            UserData newUser = new UserData();
            newUser.setEmail(email);
            newUser.setName((String) userModuleData.getOrDefault("name", "Usuario Sincronizado"));
            newUser.setPhone((String) userModuleData.getOrDefault("phone", ""));
            newUser.setSecondaryId((String) userModuleData.getOrDefault("secondaryId", "sync_" + System.currentTimeMillis()));
            newUser.setRole((String) userModuleData.getOrDefault("role", "USER"));
            newUser.setUserId(((Number) userModuleData.getOrDefault("userId", System.currentTimeMillis() % 1000000)).longValue());
            
            // Generar sueldo aleatorio
            Random random = new Random();
            double saldo = 10000 + (random.nextDouble() * 40000);
            newUser.setSaldoDisponible(BigDecimal.valueOf(saldo).setScale(2, java.math.RoundingMode.HALF_UP));
            
            // Guardar en la base de datos
            return userDataRepository.save(newUser);
            
        } catch (Exception e) {
            log.error("Error creando usuario desde datos del módulo: {}", e.getMessage());
            return null;
        }
    }

    @Operation(
        summary = "Obtener perfil del usuario",
        description = "Retorna la información del perfil del usuario autenticado. Requiere token JWT válido."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Perfil obtenido exitosamente",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = User.class),
                examples = @ExampleObject(
                    name = "Perfil del usuario",
                    value = """
                    {
                        "id": 1,
                        "email": "usuario@example.com",
                        "name": "Juan Pérez",
                        "phone": "123456789",
                        "role": "USER",
                        "saldo_disponible": 25000.00
                    }
                    """
                )
            )
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Token inválido o expirado",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = String.class),
                examples = @ExampleObject(
                    name = "Token inválido",
                    value = "Unauthorized"
                )
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Usuario no encontrado",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = String.class),
                examples = @ExampleObject(
                    name = "Usuario no encontrado",
                    value = "Not Found"
                )
            )
        )
    })
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/profile")
    public ResponseEntity<User> getProfile(
        @Parameter(
            description = "Token JWT en formato 'Bearer {token}'",
            required = true,
            example = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
        )
        @RequestHeader("Authorization") String authHeader) {
        try {
            String token = authHeader.replace("Bearer ", "");
            String email = jwtUtil.getSubject(token);
            
            if (email == null) {
                return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
            }
            
            User user = userRepository.findByEmail(email)
                    .orElse(null);
            
            if (user == null) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
            
            // NO devolver la password
            user.setPassword(null);
            
            return new ResponseEntity<>(user, HttpStatus.OK);
            
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
    }
}
