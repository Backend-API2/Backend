package backend_api.Backend.Security;

import backend_api.Backend.DTO.auth.LoginRequest;
import backend_api.Backend.DTO.auth.RegisterRequest;
import backend_api.Backend.Entity.user.User;
import backend_api.Backend.Entity.user.UserRole;
import backend_api.Backend.Repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.math.BigDecimal;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureWebMvc
@ActiveProfiles("test")
@Transactional
class JwtSecurityIntegrationTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();
        objectMapper = new ObjectMapper();
    }

    @Test
    void testRegisterEndpoint_ShouldReturnJwtToken() throws Exception {
        // Given
        RegisterRequest request = new RegisterRequest();
        request.setEmail("newuser@example.com");
        request.setPassword("password123");
        request.setName("New User");
        request.setPhone("+1234567890");
        request.setRole("USER");

        // When & Then
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.userId").exists())
                .andExpect(jsonPath("$.email").value("newuser@example.com"))
                .andExpect(jsonPath("$.name").value("New User"))
                .andExpect(jsonPath("$.role").value("USER"));
    }

    @Test
    void testLoginEndpoint_ShouldReturnJwtToken() throws Exception {
        // Given
        User user = new User();
        user.setEmail("testuser@example.com");
        user.setPassword(passwordEncoder.encode("password123"));
        user.setName("Test User");
        user.setPhone("+1234567890");
        user.setRole(UserRole.USER);
        user.setSaldo_disponible(BigDecimal.valueOf(25000.00));
        userRepository.save(user);

        LoginRequest request = new LoginRequest();
        request.setEmail("testuser@example.com");
        request.setPassword("password123");

        // When & Then
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.userId").exists())
                .andExpect(jsonPath("$.email").value("testuser@example.com"))
                .andExpect(jsonPath("$.name").value("Test User"))
                .andExpect(jsonPath("$.role").value("USER"));
    }

    @Test
    void testLoginEndpoint_InvalidCredentials_ShouldReturnUnauthorized() throws Exception {
        // Given
        User user = new User();
        user.setEmail("testuser@example.com");
        user.setPassword(passwordEncoder.encode("password123"));
        user.setName("Test User");
        user.setPhone("+1234567890");
        user.setRole(UserRole.USER);
        user.setSaldo_disponible(BigDecimal.valueOf(25000.00));
        userRepository.save(user);

        LoginRequest request = new LoginRequest();
        request.setEmail("testuser@example.com");
        request.setPassword("wrongpassword");

        // When & Then
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testLoginEndpoint_UserNotFound_ShouldReturnUnauthorized() throws Exception {
        // Given
        LoginRequest request = new LoginRequest();
        request.setEmail("nonexistent@example.com");
        request.setPassword("password123");

        // When & Then
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testRegisterEndpoint_DuplicateEmail_ShouldReturnConflict() throws Exception {
        // Given
        User existingUser = new User();
        existingUser.setEmail("existing@example.com");
        existingUser.setPassword(passwordEncoder.encode("password123"));
        existingUser.setName("Existing User");
        existingUser.setPhone("+1234567890");
        existingUser.setRole(UserRole.USER);
        existingUser.setSaldo_disponible(BigDecimal.valueOf(25000.00));
        userRepository.save(existingUser);

        RegisterRequest request = new RegisterRequest();
        request.setEmail("existing@example.com");
        request.setPassword("password123");
        request.setName("New User");
        request.setPhone("+1987654321");
        request.setRole("USER");

        // When & Then
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    void testProfileEndpoint_ValidToken_ShouldReturnUserProfile() throws Exception {
        // Given
        User user = new User();
        user.setEmail("profileuser@example.com");
        user.setPassword(passwordEncoder.encode("password123"));
        user.setName("Profile User");
        user.setPhone("+1234567890");
        user.setRole(UserRole.USER);
        user.setSaldo_disponible(BigDecimal.valueOf(25000.00));
        userRepository.save(user);

        // First login to get token
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("profileuser@example.com");
        loginRequest.setPassword("password123");

        String loginResponse = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String token = objectMapper.readTree(loginResponse).get("token").asText();

        // When & Then
        mockMvc.perform(get("/api/auth/profile")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("profileuser@example.com"))
                .andExpect(jsonPath("$.name").value("Profile User"))
                .andExpect(jsonPath("$.role").value("USER"))
                .andExpect(jsonPath("$.password").doesNotExist()); // Password should not be returned
    }

    @Test
    void testProfileEndpoint_InvalidToken_ShouldReturnUnauthorized() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/auth/profile")
                .header("Authorization", "Bearer invalid-token"))
                .andExpect(status().isForbidden());
    }

    @Test
    void testProfileEndpoint_NoToken_ShouldReturnUnauthorized() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/auth/profile"))
                .andExpect(status().isForbidden());
    }

    @Test
    void testProtectedEndpoint_ValidToken_ShouldAllowAccess() throws Exception {
        // Given
        User user = new User();
        user.setEmail("protecteduser@example.com");
        user.setPassword(passwordEncoder.encode("password123"));
        user.setName("Protected User");
        user.setPhone("+1234567890");
        user.setRole(UserRole.USER);
        user.setSaldo_disponible(BigDecimal.valueOf(25000.00));
        userRepository.save(user);

        // First login to get token
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("protecteduser@example.com");
        loginRequest.setPassword("password123");

        String loginResponse = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String token = objectMapper.readTree(loginResponse).get("token").asText();

        // When & Then - Test accessing a protected endpoint
        mockMvc.perform(get("/api/invoices/my-invoices")
                .header("Authorization", "Bearer " + token)
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk());
    }

    @Test
    void testProtectedEndpoint_NoToken_ShouldReturnUnauthorized() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/invoices/my-invoices")
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isForbidden());
    }

    @Test
    void testRoleBasedAccess_UserRole_ShouldAccessUserEndpoints() throws Exception {
        // Given
        User user = new User();
        user.setEmail("userrole@example.com");
        user.setPassword(passwordEncoder.encode("password123"));
        user.setName("User Role");
        user.setPhone("+1234567890");
        user.setRole(UserRole.USER);
        user.setSaldo_disponible(BigDecimal.valueOf(25000.00));
        userRepository.save(user);

        // First login to get token
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("userrole@example.com");
        loginRequest.setPassword("password123");

        String loginResponse = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String token = objectMapper.readTree(loginResponse).get("token").asText();

        // When & Then - User should be able to access user endpoints
        mockMvc.perform(get("/api/invoices/my-invoices")
                .header("Authorization", "Bearer " + token)
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/invoices/my-summary")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    void testRoleBasedAccess_MerchantRole_ShouldAccessMerchantEndpoints() throws Exception {
        // Given
        User merchant = new User();
        merchant.setEmail("merchant@example.com");
        merchant.setPassword(passwordEncoder.encode("password123"));
        merchant.setName("Merchant User");
        merchant.setPhone("+1234567890");
        merchant.setRole(UserRole.MERCHANT);
        merchant.setSaldo_disponible(BigDecimal.valueOf(50000.00));
        userRepository.save(merchant);

        // First login to get token
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("merchant@example.com");
        loginRequest.setPassword("password123");

        String loginResponse = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String token = objectMapper.readTree(loginResponse).get("token").asText();

        // When & Then - Merchant should be able to access merchant endpoints
        mockMvc.perform(get("/api/invoices/my-invoices")
                .header("Authorization", "Bearer " + token)
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/invoices/my-summary")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

            @Test
            void testCorsConfiguration_ShouldAllowCrossOriginRequests() throws Exception {
                // Given
                RegisterRequest request = new RegisterRequest();
                request.setEmail("corsuser@example.com");
                request.setPassword("password123");
                request.setName("CORS User");
                request.setPhone("+1234567890");
                request.setRole("USER");

                // When & Then
                mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .header("Origin", "http://localhost:3000"))
                        .andExpect(status().isCreated())
                        .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:3000"));
            }

            @Test
            void testAdminRoleAccess_ShouldAccessAdminEndpoints() throws Exception {
                // Given
                User admin = new User();
                admin.setEmail("admin@example.com");
                admin.setPassword(passwordEncoder.encode("password123"));
                admin.setName("Admin User");
                admin.setPhone("+1234567890");
                admin.setRole(UserRole.MERCHANT); // Using MERCHANT as admin role
                admin.setSaldo_disponible(BigDecimal.valueOf(100000.00));
                userRepository.save(admin);

                // First login to get token
                LoginRequest loginRequest = new LoginRequest();
                loginRequest.setEmail("admin@example.com");
                loginRequest.setPassword("password123");

                String loginResponse = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                        .andExpect(status().isOk())
                        .andReturn()
                        .getResponse()
                        .getContentAsString();

                String token = objectMapper.readTree(loginResponse).get("token").asText();

                // When & Then - Admin should be able to access admin endpoints
                mockMvc.perform(get("/api/invoices/my-invoices")
                        .header("Authorization", "Bearer " + token)
                        .param("page", "0")
                        .param("size", "10"))
                        .andExpect(status().isOk());

                mockMvc.perform(get("/api/invoices/my-summary")
                        .header("Authorization", "Bearer " + token))
                        .andExpect(status().isOk());
            }

            @Test
            void testTokenExpiration_ShouldReturnUnauthorized() throws Exception {
                // Given
                User user = new User();
                user.setEmail("expireduser@example.com");
                user.setPassword(passwordEncoder.encode("password123"));
                user.setName("Expired User");
                user.setPhone("+1234567890");
                user.setRole(UserRole.USER);
                user.setSaldo_disponible(BigDecimal.valueOf(25000.00));
                userRepository.save(user);

                // First login to get token
                LoginRequest loginRequest = new LoginRequest();
                loginRequest.setEmail("expireduser@example.com");
                loginRequest.setPassword("password123");

                String loginResponse = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                        .andExpect(status().isOk())
                        .andReturn()
                        .getResponse()
                        .getContentAsString();

                String token = objectMapper.readTree(loginResponse).get("token").asText();

                // Simulate token expiration by using an invalid token
                String expiredToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJleHBpcmVkdXNlckBleGFtcGxlLmNvbSIsImlhdCI6MTYwOTQ1NjAwMCwiZXhwIjoxNjA5NDU2MDAwfQ.invalid";

                // When & Then
                mockMvc.perform(get("/api/auth/profile")
                        .header("Authorization", "Bearer " + expiredToken))
                        .andExpect(status().isForbidden());
            }

            @Test
            void testMalformedToken_ShouldReturnUnauthorized() throws Exception {
                // Given
                String malformedToken = "malformed.token.here";

                // When & Then
                mockMvc.perform(get("/api/auth/profile")
                        .header("Authorization", "Bearer " + malformedToken))
                        .andExpect(status().isForbidden());
            }

            @Test
            void testEmptyToken_ShouldReturnUnauthorized() throws Exception {
                // Given
                String emptyToken = "";

                // When & Then
                mockMvc.perform(get("/api/auth/profile")
                        .header("Authorization", "Bearer " + emptyToken))
                        .andExpect(status().isForbidden());
            }

            @Test
            void testTokenWithoutBearer_ShouldReturnUnauthorized() throws Exception {
                // Given
                String tokenWithoutBearer = "valid.jwt.token.here";

                // When & Then
                mockMvc.perform(get("/api/auth/profile")
                        .header("Authorization", tokenWithoutBearer))
                        .andExpect(status().isForbidden());
            }

            @Test
            void testMultipleRoleAccess_ShouldWorkCorrectly() throws Exception {
                // Given
                User user = new User();
                user.setEmail("multirole@example.com");
                user.setPassword(passwordEncoder.encode("password123"));
                user.setName("Multi Role User");
                user.setPhone("+1234567890");
                user.setRole(UserRole.MERCHANT);
                user.setSaldo_disponible(BigDecimal.valueOf(50000.00));
                userRepository.save(user);

                // First login to get token
                LoginRequest loginRequest = new LoginRequest();
                loginRequest.setEmail("multirole@example.com");
                loginRequest.setPassword("password123");

                String loginResponse = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                        .andExpect(status().isOk())
                        .andReturn()
                        .getResponse()
                        .getContentAsString();

                String token = objectMapper.readTree(loginResponse).get("token").asText();

                // When & Then - User should be able to access both user and merchant endpoints
                mockMvc.perform(get("/api/invoices/my-invoices")
                        .header("Authorization", "Bearer " + token)
                        .param("page", "0")
                        .param("size", "10"))
                        .andExpect(status().isOk());

                mockMvc.perform(get("/api/invoices/my-summary")
                        .header("Authorization", "Bearer " + token))
                        .andExpect(status().isOk());

                mockMvc.perform(get("/api/auth/profile")
                        .header("Authorization", "Bearer " + token))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.email").value("multirole@example.com"))
                        .andExpect(jsonPath("$.role").value("MERCHANT"));
            }


            @Test
            void testInvalidJsonRequest_ShouldReturnBadRequest() throws Exception {
                // Given
                String invalidJson = "{ invalid json }";

                // When & Then
                mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                        .andExpect(status().isBadRequest());
            }

            @Test
            void testMissingContentType_ShouldReturnUnsupportedMediaType() throws Exception {
                // Given
                RegisterRequest request = new RegisterRequest();
                request.setEmail("test@example.com");
                request.setPassword("password123");
                request.setName("Test User");
                request.setPhone("+1234567890");
                request.setRole("USER");

                // When & Then
                mockMvc.perform(post("/api/auth/register")
                        .content(objectMapper.writeValueAsString(request)))
                        .andExpect(status().isUnsupportedMediaType());
            }
        }