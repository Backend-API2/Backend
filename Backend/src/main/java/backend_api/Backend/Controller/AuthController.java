package backend_api.Backend.Controller;

import backend_api.Backend.DTO.auth.LoginRequest;
import backend_api.Backend.DTO.auth.RegisterRequest;
import backend_api.Backend.DTO.auth.AuthResponse;
import backend_api.Backend.Entity.user.User;
import backend_api.Backend.Entity.user.UserRole;
import backend_api.Backend.Repository.UserRepository;
import backend_api.Backend.Auth.JwtUtil;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private JwtUtil jwtUtil;

    //Crear nueva cuenta
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
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
            
            User savedUser = userRepository.save(user);
            
            String token = jwtUtil.generateToken(savedUser.getEmail());
            
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

    //  Iniciar sesión
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        try {
            User user = userRepository.findByEmail(request.getEmail())
                    .orElse(null);
            
            if (user == null) {
                return new ResponseEntity<>(HttpStatus.UNAUTHORIZED); 
            }
            
            if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
                return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
            }
            
            String token = jwtUtil.generateToken(user.getEmail());
            
            AuthResponse response = new AuthResponse(
                token, 
                user.getId(), 
                user.getEmail(), 
                user.getName(), 
                user.getRole().toString()
            );
            
            return new ResponseEntity<>(response, HttpStatus.OK);
            
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Obtener perfil del usuario autenticado
    @GetMapping("/profile")
    public ResponseEntity<User> getProfile(@RequestHeader("Authorization") String authHeader) {
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
