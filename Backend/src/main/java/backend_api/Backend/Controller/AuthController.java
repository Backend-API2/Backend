package backend_api.Backend.Controller;

import backend_api.Backend.DTO.auth.LoginRequest;
import backend_api.Backend.DTO.auth.RegisterRequest;
import backend_api.Backend.DTO.auth.AuthResponse;
import backend_api.Backend.Entity.user.User;
import backend_api.Backend.Entity.user.UserRole;
import backend_api.Backend.Entity.UserData;
import backend_api.Backend.Repository.UserRepository;
import backend_api.Backend.Repository.UserDataRepository;
import backend_api.Backend.Auth.JwtUtil;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpEntity;
import java.util.List;
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
public class AuthController {

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private UserDataRepository userDataRepository;
    
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
    public ResponseEntity<AuthResponse> login(
        @Parameter(
            description = "Credenciales de login",
            required = true,
            schema = @Schema(implementation = LoginRequest.class)
        )
        @Valid @RequestBody LoginRequest request) {
        try {
            String email = request.getEmail();
            String password = request.getPassword();
            
            // 1. Primero buscar en usuarios locales (users table)
            Optional<User> localUser = userRepository.findByEmail(email);
            if (localUser.isPresent()) {
                User user = localUser.get();
                if (passwordEncoder.matches(password, user.getPassword())) {
                    String token = jwtUtil.generateToken(user.getEmail(), 86400000L, List.of(user.getRole().toString()));
                    AuthResponse response = new AuthResponse(
                        token, 
                        user.getId(), 
                        user.getEmail(), 
                        user.getName(), 
                        user.getRole().toString()
                    );
                    return new ResponseEntity<>(response, HttpStatus.OK);
                } else {
                    // Contraseña incorrecta para usuario local
                    return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
                }
            }
            
            // 2. Si no está en usuarios locales, buscar en usuarios sincronizados (user_data table)
            Optional<UserData> syncedUser = userDataRepository.findByEmail(email);
            if (syncedUser.isPresent()) {
                UserData userData = syncedUser.get();
                // Validar contraseña con el módulo de usuarios
                if (validatePasswordWithUserModule(email, password)) {
                    String token = jwtUtil.generateToken(userData.getEmail());
                    AuthResponse response = new AuthResponse(
                        token, 
                        userData.getUserId(), 
                        userData.getEmail(), 
                        userData.getName(), 
                        "USER"
                    );
                    return new ResponseEntity<>(response, HttpStatus.OK);
                } else {
                    // Contraseña incorrecta para usuario sincronizado
                    return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
                }
            }
            
            // 3. Usuario no encontrado en ninguna tabla
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
        } catch (Exception e) {
            // En caso de cualquier error (conexión, timeout, etc.), devolver false
            return false;
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
