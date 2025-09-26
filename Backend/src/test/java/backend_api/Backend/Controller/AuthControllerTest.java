package backend_api.Backend.Controller;

import backend_api.Backend.Auth.JwtUtil;
import backend_api.Backend.DTO.auth.AuthResponse;
import backend_api.Backend.DTO.auth.LoginRequest;
import backend_api.Backend.DTO.auth.RegisterRequest;
import backend_api.Backend.Entity.user.User;
import backend_api.Backend.Entity.user.UserRole;
import backend_api.Backend.Repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private AuthController authController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(authController).build();
        objectMapper = new ObjectMapper();
    }

    @Test
    void testRegister_Success() throws Exception {
        // Given
        RegisterRequest request = new RegisterRequest();
        request.setEmail("test@example.com");
        request.setPassword("password123");
        request.setName("Test User");
        request.setPhone("123456789");
        request.setRole("USER");

        User savedUser = new User();
        savedUser.setId(1L);
        savedUser.setEmail("test@example.com");
        savedUser.setName("Test User");
        savedUser.setRole(UserRole.USER);
        savedUser.setSaldo_disponible(BigDecimal.valueOf(25000.00));

        when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(jwtUtil.generateToken("test@example.com")).thenReturn("jwt-token");

        // When
        ResponseEntity<AuthResponse> response = authController.register(request);

        // Then
        assertEquals(201, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals("jwt-token", response.getBody().getToken());
        assertEquals(1L, response.getBody().getUserId());
        assertEquals("test@example.com", response.getBody().getEmail());
        assertEquals("Test User", response.getBody().getName());
        assertEquals("USER", response.getBody().getRole());

        verify(userRepository).existsByEmail("test@example.com");
        verify(passwordEncoder).encode("password123");
        verify(userRepository).save(any(User.class));
        verify(jwtUtil).generateToken("test@example.com");
    }

    @Test
    void testRegister_EmailAlreadyExists() throws Exception {
        // Given
        RegisterRequest request = new RegisterRequest();
        request.setEmail("existing@example.com");
        request.setPassword("password123");
        request.setName("Test User");
        request.setPhone("123456789");
        request.setRole("USER");

        when(userRepository.existsByEmail("existing@example.com")).thenReturn(true);

        // When
        ResponseEntity<AuthResponse> response = authController.register(request);

        // Then
        assertEquals(409, response.getStatusCode().value());
        assertNull(response.getBody());

        verify(userRepository).existsByEmail("existing@example.com");
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void testLogin_Success() throws Exception {
        // Given
        LoginRequest request = new LoginRequest();
        request.setEmail("test@example.com");
        request.setPassword("password123");

        User user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");
        user.setPassword("encodedPassword");
        user.setName("Test User");
        user.setRole(UserRole.USER);

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password123", "encodedPassword")).thenReturn(true);
        when(jwtUtil.generateToken("test@example.com")).thenReturn("jwt-token");

        // When
        ResponseEntity<AuthResponse> response = authController.login(request);

        // Then
        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals("jwt-token", response.getBody().getToken());
        assertEquals(1L, response.getBody().getUserId());
        assertEquals("test@example.com", response.getBody().getEmail());
        assertEquals("Test User", response.getBody().getName());
        assertEquals("USER", response.getBody().getRole());

        verify(userRepository).findByEmail("test@example.com");
        verify(passwordEncoder).matches("password123", "encodedPassword");
        verify(jwtUtil).generateToken("test@example.com");
    }

    @Test
    void testLogin_UserNotFound() throws Exception {
        // Given
        LoginRequest request = new LoginRequest();
        request.setEmail("nonexistent@example.com");
        request.setPassword("password123");

        when(userRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

        // When
        ResponseEntity<AuthResponse> response = authController.login(request);

        // Then
        assertEquals(401, response.getStatusCode().value());
        assertNull(response.getBody());

        verify(userRepository).findByEmail("nonexistent@example.com");
        verify(passwordEncoder, never()).matches(anyString(), anyString());
    }

    @Test
    void testGetProfile_Success() throws Exception {
        // Given
        String token = "valid-jwt-token";
        String authHeader = "Bearer " + token;

        User user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");
        user.setPassword("encodedPassword");
        user.setName("Test User");
        user.setRole(UserRole.USER);

        when(jwtUtil.getSubject(token)).thenReturn("test@example.com");
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));

        // When
        ResponseEntity<User> response = authController.getProfile(authHeader);

        // Then
        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(1L, response.getBody().getId());
        assertEquals("test@example.com", response.getBody().getEmail());
        assertEquals("Test User", response.getBody().getName());
        assertNull(response.getBody().getPassword()); // Password should be null

        verify(jwtUtil).getSubject(token);
        verify(userRepository).findByEmail("test@example.com");
    }

    @Test
    void testGetProfile_InvalidToken() throws Exception {
        // Given
        String token = "invalid-jwt-token";
        String authHeader = "Bearer " + token;

        when(jwtUtil.getSubject(token)).thenReturn(null);

        // When
        ResponseEntity<User> response = authController.getProfile(authHeader);

        // Then
        assertEquals(401, response.getStatusCode().value());
        assertNull(response.getBody());

        verify(jwtUtil).getSubject(token);
        verify(userRepository, never()).findByEmail(anyString());
    }

    @Test
    void testRegister_InvalidEmail() {
        // Given
        RegisterRequest request = new RegisterRequest();
        request.setEmail("invalid-email");
        request.setPassword("password123");
        request.setName("Test User");
        request.setPhone("+1234567890");
        request.setRole("USER");

        // When
        ResponseEntity<AuthResponse> response = authController.register(request);

        // Then
        assertEquals(500, response.getStatusCode().value());
        assertNull(response.getBody());
        verify(userRepository).existsByEmail("invalid-email");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void testRegister_ShortPassword() {
        // Given
        RegisterRequest request = new RegisterRequest();
        request.setEmail("test@example.com");
        request.setPassword("12345"); // Less than 6 characters
        request.setName("Test User");
        request.setPhone("+1234567890");
        request.setRole("USER");

        // When
        ResponseEntity<AuthResponse> response = authController.register(request);

        // Then
        assertEquals(500, response.getStatusCode().value());
        assertNull(response.getBody());
        verify(userRepository).existsByEmail("test@example.com");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void testRegister_BlankName() {
        // Given
        RegisterRequest request = new RegisterRequest();
        request.setEmail("test@example.com");
        request.setPassword("password123");
        request.setName(""); // Blank name
        request.setPhone("+1234567890");
        request.setRole("USER");

        // When
        ResponseEntity<AuthResponse> response = authController.register(request);

        // Then
        assertEquals(500, response.getStatusCode().value());
        assertNull(response.getBody());
        verify(userRepository).existsByEmail("test@example.com");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void testRegister_InvalidPhone() {
        // Given
        RegisterRequest request = new RegisterRequest();
        request.setEmail("test@example.com");
        request.setPassword("password123");
        request.setName("Test User");
        request.setPhone("invalid-phone");
        request.setRole("USER");

        // When
        ResponseEntity<AuthResponse> response = authController.register(request);

        // Then
        assertEquals(500, response.getStatusCode().value());
        assertNull(response.getBody());
        verify(userRepository).existsByEmail("test@example.com");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void testRegister_DatabaseException() {
        // Given
        RegisterRequest request = new RegisterRequest();
        request.setEmail("test@example.com");
        request.setPassword("password123");
        request.setName("Test User");
        request.setPhone("+1234567890");
        request.setRole("USER");

        when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenThrow(new RuntimeException("Database error"));

        // When
        ResponseEntity<AuthResponse> response = authController.register(request);

        // Then
        assertEquals(500, response.getStatusCode().value());
        assertNull(response.getBody());
        verify(userRepository).existsByEmail("test@example.com");
        verify(passwordEncoder).encode("password123");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void testLogin_InvalidEmail() {
        // Given
        LoginRequest request = new LoginRequest();
        request.setEmail("invalid-email");
        request.setPassword("password123");

        // When
        ResponseEntity<AuthResponse> response = authController.login(request);

        // Then
        assertEquals(401, response.getStatusCode().value());
        assertNull(response.getBody());
        verify(userRepository).findByEmail("invalid-email");
    }

    @Test
    void testLogin_ShortPassword() {
        // Given
        LoginRequest request = new LoginRequest();
        request.setEmail("test@example.com");
        request.setPassword("12345"); // Less than 6 characters

        // When
        ResponseEntity<AuthResponse> response = authController.login(request);

        // Then
        assertEquals(401, response.getStatusCode().value());
        assertNull(response.getBody());
        verify(userRepository).findByEmail("test@example.com");
    }

    @Test
    void testLogin_DatabaseException() {
        // Given
        LoginRequest request = new LoginRequest();
        request.setEmail("test@example.com");
        request.setPassword("password123");

        when(userRepository.findByEmail("test@example.com")).thenThrow(new RuntimeException("Database error"));

        // When
        ResponseEntity<AuthResponse> response = authController.login(request);

        // Then
        assertEquals(500, response.getStatusCode().value());
        assertNull(response.getBody());
        verify(userRepository).findByEmail("test@example.com");
    }

    @Test
    void testGetProfile_UserNotFound() {
        // Given
        String token = "valid-jwt-token";
        String authHeader = "Bearer " + token;
        String email = "nonexistent@example.com";

        when(jwtUtil.getSubject(token)).thenReturn(email);
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        // When
        ResponseEntity<User> response = authController.getProfile(authHeader);

        // Then
        assertEquals(404, response.getStatusCode().value());
        assertNull(response.getBody());
        verify(jwtUtil).getSubject(token);
        verify(userRepository).findByEmail(email);
    }

    @Test
    void testGetProfile_DatabaseException() {
        // Given
        String token = "valid-jwt-token";
        String authHeader = "Bearer " + token;
        String email = "test@example.com";

        when(jwtUtil.getSubject(token)).thenReturn(email);
        when(userRepository.findByEmail(email)).thenThrow(new RuntimeException("Database error"));

        // When
        ResponseEntity<User> response = authController.getProfile(authHeader);

        // Then
        assertEquals(401, response.getStatusCode().value());
        assertNull(response.getBody());
        verify(jwtUtil).getSubject(token);
        verify(userRepository).findByEmail(email);
    }

    @Test
    void testGetProfile_InvalidAuthHeader() {
        // Given
        String invalidHeader = "InvalidHeader";

        // When
        ResponseEntity<User> response = authController.getProfile(invalidHeader);

        // Then
        assertEquals(401, response.getStatusCode().value());
        assertNull(response.getBody());
        verify(jwtUtil).getSubject("InvalidHeader");
        verify(userRepository, never()).findByEmail(anyString());
    }

    @Test
    void testGetProfile_EmptyAuthHeader() {
        // Given
        String emptyHeader = "";

        // When
        ResponseEntity<User> response = authController.getProfile(emptyHeader);

        // Then
        assertEquals(401, response.getStatusCode().value());
        assertNull(response.getBody());
        verify(jwtUtil).getSubject("");
        verify(userRepository, never()).findByEmail(anyString());
    }

    @Test
    void testLogin_WrongPassword() {
        // Given
        LoginRequest request = new LoginRequest();
        request.setEmail("test@example.com");
        request.setPassword("wrongpassword");

        User user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");
        user.setPassword("encodedPassword");
        user.setName("Test User");
        user.setRole(UserRole.USER);

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrongpassword", "encodedPassword")).thenReturn(false);

        // When
        ResponseEntity<AuthResponse> response = authController.login(request);

        // Then
        assertEquals(401, response.getStatusCode().value());
        assertNull(response.getBody());

        verify(userRepository).findByEmail("test@example.com");
        verify(passwordEncoder).matches("wrongpassword", "encodedPassword");
        verify(jwtUtil, never()).generateToken(anyString());
    }

    @Test
    void testRegister_InvalidRole() {
        // Given
        RegisterRequest request = new RegisterRequest();
        request.setEmail("test@example.com");
        request.setPassword("password123");
        request.setName("Test User");
        request.setPhone("123456789");
        request.setRole("INVALID_ROLE");

        User savedUser = new User();
        savedUser.setId(1L);
        savedUser.setEmail("test@example.com");
        savedUser.setName("Test User");
        savedUser.setRole(UserRole.USER); // Should default to USER
        savedUser.setSaldo_disponible(BigDecimal.valueOf(25000.00));

        when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(jwtUtil.generateToken("test@example.com")).thenReturn("jwt-token");

        // When
        ResponseEntity<AuthResponse> response = authController.register(request);

        // Then
        assertEquals(201, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals("USER", response.getBody().getRole()); // Should default to USER

        verify(userRepository).existsByEmail("test@example.com");
        verify(passwordEncoder).encode("password123");
        verify(userRepository).save(any(User.class));
        verify(jwtUtil).generateToken("test@example.com");
    }

    @Test
    void testRegister_AdminRole() {
        // Given
        RegisterRequest request = new RegisterRequest();
        request.setEmail("admin@example.com");
        request.setPassword("password123");
        request.setName("Admin User");
        request.setPhone("123456789");
        request.setRole("MERCHANT");

        User savedUser = new User();
        savedUser.setId(1L);
        savedUser.setEmail("admin@example.com");
        savedUser.setName("Admin User");
        savedUser.setRole(UserRole.MERCHANT);
        savedUser.setSaldo_disponible(null); // Admin should not have saldo

        when(userRepository.existsByEmail("admin@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(jwtUtil.generateToken("admin@example.com")).thenReturn("jwt-token");

        // When
        ResponseEntity<AuthResponse> response = authController.register(request);

        // Then
        assertEquals(201, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals("MERCHANT", response.getBody().getRole());

        verify(userRepository).existsByEmail("admin@example.com");
        verify(passwordEncoder).encode("password123");
        verify(userRepository).save(any(User.class));
        verify(jwtUtil).generateToken("admin@example.com");
    }

    @Test
    void testRegister_MerchantRole() {
        // Given
        RegisterRequest request = new RegisterRequest();
        request.setEmail("merchant@example.com");
        request.setPassword("password123");
        request.setName("Merchant User");
        request.setPhone("123456789");
        request.setRole("MERCHANT");

        User savedUser = new User();
        savedUser.setId(1L);
        savedUser.setEmail("merchant@example.com");
        savedUser.setName("Merchant User");
        savedUser.setRole(UserRole.MERCHANT);
        savedUser.setSaldo_disponible(null); // Merchant should not have saldo

        when(userRepository.existsByEmail("merchant@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(jwtUtil.generateToken("merchant@example.com")).thenReturn("jwt-token");

        // When
        ResponseEntity<AuthResponse> response = authController.register(request);

        // Then
        assertEquals(201, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals("MERCHANT", response.getBody().getRole());

        verify(userRepository).existsByEmail("merchant@example.com");
        verify(passwordEncoder).encode("password123");
        verify(userRepository).save(any(User.class));
        verify(jwtUtil).generateToken("merchant@example.com");
    }

    @Test
    void testGetProfile_ValidTokenButUserNotFound() {
        // Given
        String token = "valid-jwt-token";
        String authHeader = "Bearer " + token;
        String email = "nonexistent@example.com";

        when(jwtUtil.getSubject(token)).thenReturn(email);
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        // When
        ResponseEntity<User> response = authController.getProfile(authHeader);

        // Then
        assertEquals(404, response.getStatusCode().value());
        assertNull(response.getBody());

        verify(jwtUtil).getSubject(token);
        verify(userRepository).findByEmail(email);
    }

    @Test
    void testGetProfile_ExceptionThrown() {
        // Given
        String token = "valid-jwt-token";
        String authHeader = "Bearer " + token;

        when(jwtUtil.getSubject(token)).thenThrow(new RuntimeException("JWT error"));

        // When
        ResponseEntity<User> response = authController.getProfile(authHeader);

        // Then
        assertEquals(401, response.getStatusCode().value());
        assertNull(response.getBody());

        verify(jwtUtil).getSubject(token);
        verify(userRepository, never()).findByEmail(anyString());
    }
}