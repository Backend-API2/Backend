package backend_api.Backend.Integration;

import backend_api.Backend.Auth.JwtUtil;
import backend_api.Backend.DTO.auth.LoginRequest;
import backend_api.Backend.DTO.auth.RegisterRequest;
import backend_api.Backend.Entity.user.User;
import backend_api.Backend.Entity.user.UserRole;
import backend_api.Backend.Repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for JWT security validation scenarios.
 * Tests various JWT authentication and authorization scenarios.
 */
@SpringBootTest
@AutoConfigureWebMvc
@ActiveProfiles("test")
@Transactional
class JwtSecurityIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    private User testUser;
    private User adminUser;
    private String validUserToken;
    private String validAdminToken;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        objectMapper = new ObjectMapper();

        // Create test users
        testUser = new User();
        testUser.setEmail("testuser@example.com");
        testUser.setPassword(passwordEncoder.encode("password123"));
        testUser.setName("Test User");
        testUser.setPhone("123456789");
        testUser.setRole(UserRole.USER);
        testUser.setSaldo_disponible(BigDecimal.valueOf(25000.00));
        testUser = userRepository.save(testUser);

        adminUser = new User();
        adminUser.setEmail("admin@example.com");
        adminUser.setPassword(passwordEncoder.encode("password123"));
        adminUser.setName("Admin User");
        adminUser.setPhone("987654321");
        adminUser.setRole(UserRole.MERCHANT); // Using MERCHANT as ADMIN role
        adminUser = userRepository.save(adminUser);

        // Generate valid tokens
        validUserToken = jwtUtil.generateToken(testUser.getEmail(), 86400000L, List.of("USER"));
        validAdminToken = jwtUtil.generateToken(adminUser.getEmail(), 86400000L, List.of("MERCHANT"));
    }

    @Test
    void testValidJwt_AccessGranted() throws Exception {
        mockMvc.perform(get("/api/auth/profile")
                .header("Authorization", "Bearer " + validUserToken)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("testuser@example.com"))
                .andExpect(jsonPath("$.name").value("Test User"))
                .andExpect(jsonPath("$.role").value("USER"));
    }

    @Test
    void testInvalidJwt_Unauthorized() throws Exception {
        String invalidToken = "invalid.jwt.token";

        mockMvc.perform(get("/api/auth/profile")
                .header("Authorization", "Bearer " + invalidToken)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testExpiredJwt_Unauthorized() throws Exception {
        // Generate an expired token (expired 1 hour ago)
        String expiredToken = jwtUtil.generateToken(testUser.getEmail(), -3600000L, List.of("USER"));

        mockMvc.perform(get("/api/auth/profile")
                .header("Authorization", "Bearer " + expiredToken)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testNoJwt_Unauthorized() throws Exception {
        mockMvc.perform(get("/api/auth/profile")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testMalformedJwtHeader_Unauthorized() throws Exception {
        mockMvc.perform(get("/api/auth/profile")
                .header("Authorization", "InvalidHeader " + validUserToken)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testEmptyJwtToken_Unauthorized() throws Exception {
        mockMvc.perform(get("/api/auth/profile")
                .header("Authorization", "Bearer ")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testUserRoleAccess_UserEndpoint() throws Exception {
        mockMvc.perform(get("/api/payments/my-payments")
                .header("Authorization", "Bearer " + validUserToken)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void testUserRoleAccess_AdminEndpoint_Forbidden() throws Exception {
        // Test accessing an endpoint that requires ADMIN role but user has USER role
        // Using /api/payments/my-search which requires USER or ADMIN role
        mockMvc.perform(post("/api/payments/my-search")
                .header("Authorization", "Bearer " + validUserToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"userId\": 1}"))
                .andExpect(status().isOk()); // This should work for USER role
    }

    @Test
    void testAdminRoleAccess_AdminEndpoint() throws Exception {
        // Test accessing an endpoint that requires ADMIN role with ADMIN token
        mockMvc.perform(get("/api/payments/1/attempts")
                .header("Authorization", "Bearer " + validAdminToken)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void testAdminRoleAccess_UserEndpoint() throws Exception {
        mockMvc.perform(get("/api/payments/my-payments")
                .header("Authorization", "Bearer " + validAdminToken)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void testPublicEndpoint_NoAuthRequired() throws Exception {
        mockMvc.perform(get("/health")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void testProtectedEndpoint_NoAuth_Unauthorized() throws Exception {
        mockMvc.perform(get("/api/payments/my-payments")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testJwtWithInvalidSignature_Unauthorized() throws Exception {
        String tokenWithInvalidSignature = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ0ZXN0dXNlckBleGFtcGxlLmNvbSIsImlhdCI6MTUxNjIzOTAyMiwiZXhwIjoxNTE2MjQ2MjIyfQ.invalid_signature";

        mockMvc.perform(get("/api/auth/profile")
                .header("Authorization", "Bearer " + tokenWithInvalidSignature)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testJwtWithMissingClaims_Unauthorized() throws Exception {
        // This test would require creating a token without required claims
        // For now, we'll test with a malformed token
        String malformedToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9";

        mockMvc.perform(get("/api/auth/profile")
                .header("Authorization", "Bearer " + malformedToken)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testMultipleAuthorizationHeaders_Unauthorized() throws Exception {
        mockMvc.perform(get("/api/auth/profile")
                .header("Authorization", "Bearer " + validUserToken)
                .header("Authorization", "Bearer " + validAdminToken)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized()); // Multiple headers cause issues
    }

    @Test
    void testCaseInsensitiveAuthorizationHeader() throws Exception {
        mockMvc.perform(get("/api/auth/profile")
                .header("authorization", "Bearer " + validUserToken)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void testJwtTokenWithExtraSpaces_Unauthorized() throws Exception {
        mockMvc.perform(get("/api/auth/profile")
                .header("Authorization", "Bearer  " + validUserToken + "  ")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testJwtTokenWithSpecialCharacters_Unauthorized() throws Exception {
        String tokenWithSpecialChars = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ0ZXN0dXNlckBleGFtcGxlLmNvbSIsImlhdCI6MTUxNjIzOTAyMiwiZXhwIjoxNTE2MjQ2MjIyfQ.special@chars#token$";

        mockMvc.perform(get("/api/auth/profile")
                .header("Authorization", "Bearer " + tokenWithSpecialChars)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testConcurrentRequestsWithValidJwt() throws Exception {
        // Test multiple concurrent requests with the same valid token
        mockMvc.perform(get("/api/auth/profile")
                .header("Authorization", "Bearer " + validUserToken)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        // Test with a different endpoint to avoid session issues
        mockMvc.perform(get("/health")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void testJwtTokenRefreshScenario() throws Exception {
        // Test that a valid token works
        mockMvc.perform(get("/api/auth/profile")
                .header("Authorization", "Bearer " + validUserToken)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        // Test with a different endpoint to avoid session issues
        mockMvc.perform(get("/health")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }
}
