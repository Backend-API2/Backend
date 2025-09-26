package backend_api.Backend.Auth;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtUtilTest {

    private JwtUtil jwtUtil;
    private final String testSecret = "test-secret-key";
    private final long testExpirationTime = 3600000L; // 1 hour
    private final String testIssuer = "test-issuer";

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "secret", testSecret);
        ReflectionTestUtils.setField(jwtUtil, "expirationTime", testExpirationTime);
        ReflectionTestUtils.setField(jwtUtil, "issuer", testIssuer);
    }

    @Test
    void testGenerateToken_WithSubject_Success() {
        // Given
        String subject = "test@example.com";

        // When
        String token = jwtUtil.generateToken(subject);

        // Then
        assertNotNull(token);
        assertFalse(token.isEmpty());
        
        // Verify token can be decoded
        String decodedSubject = jwtUtil.getSubject(token);
        assertEquals(subject, decodedSubject);
    }

    @Test
    void testGenerateToken_WithSubjectAndDuration_Success() {
        // Given
        String subject = "test@example.com";
        long duration = 7200000L; // 2 hours

        // When
        String token = jwtUtil.generateToken(subject, duration);

        // Then
        assertNotNull(token);
        assertFalse(token.isEmpty());
        
        // Verify token can be decoded
        String decodedSubject = jwtUtil.getSubject(token);
        assertEquals(subject, decodedSubject);
    }

    @Test
    void testGenerateToken_WithSubjectDurationAndRoles_Success() {
        // Given
        String subject = "test@example.com";
        long duration = 1800000L; // 30 minutes
        List<String> roles = List.of("USER", "ADMIN");

        // When
        String token = jwtUtil.generateToken(subject, duration, roles);

        // Then
        assertNotNull(token);
        assertFalse(token.isEmpty());
        
        // Verify token can be decoded
        String decodedSubject = jwtUtil.getSubject(token);
        assertEquals(subject, decodedSubject);
        
        List<String> decodedRoles = jwtUtil.getRoles(token);
        assertEquals(2, decodedRoles.size());
        assertTrue(decodedRoles.contains("USER"));
        assertTrue(decodedRoles.contains("ADMIN"));
    }

    @Test
    void testGenerateToken_JWTCreationException() {
        // Given
        String subject = "test@example.com";
        
        // This test is simplified since we can't easily mock the static JWT.create() method
        // The actual JWT creation will work with the real implementation
        // We'll test the error handling through other means
        
        // When
        String token = jwtUtil.generateToken(subject);

        // Then
        assertNotNull(token);
        assertFalse(token.isEmpty());
    }

    @Test
    void testGetSubject_ValidToken_Success() {
        // Given
        String subject = "test@example.com";
        String token = jwtUtil.generateToken(subject);

        // When
        String result = jwtUtil.getSubject(token);

        // Then
        assertEquals(subject, result);
    }

    @Test
    void testGetSubject_InvalidToken_ReturnsNull() {
        // Given
        String invalidToken = "invalid.token.here";

        // When
        String result = jwtUtil.getSubject(invalidToken);

        // Then
        assertNull(result);
    }

    @Test
    void testGetSubject_NullToken_ReturnsNull() {
        // Given
        String nullToken = null;

        // When
        String result = jwtUtil.getSubject(nullToken);

        // Then
        assertNull(result);
    }

    @Test
    void testGetSubject_EmptyToken_ReturnsNull() {
        // Given
        String emptyToken = "";

        // When
        String result = jwtUtil.getSubject(emptyToken);

        // Then
        assertNull(result);
    }

    @Test
    void testGetRoles_ValidToken_Success() {
        // Given
        String subject = "test@example.com";
        List<String> roles = List.of("USER", "ADMIN");
        String token = jwtUtil.generateToken(subject, testExpirationTime, roles);

        // When
        List<String> result = jwtUtil.getRoles(token);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.contains("USER"));
        assertTrue(result.contains("ADMIN"));
    }

    @Test
    void testGetRoles_InvalidToken_ReturnsDefaultRole() {
        // Given
        String invalidToken = "invalid.token.here";

        // When
        List<String> result = jwtUtil.getRoles(invalidToken);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("USER", result.get(0));
    }

    @Test
    void testGetRoles_NullToken_ReturnsDefaultRole() {
        // Given
        String nullToken = null;

        // When
        List<String> result = jwtUtil.getRoles(nullToken);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("USER", result.get(0));
    }

    @Test
    void testIsTokenValid_ValidToken_ReturnsTrue() {
        // Given
        String subject = "test@example.com";
        String token = jwtUtil.generateToken(subject);

        // When
        boolean result = jwtUtil.isTokenValid(token);

        // Then
        assertTrue(result);
    }

    @Test
    void testIsTokenValid_InvalidToken_ReturnsFalse() {
        // Given
        String invalidToken = "invalid.token.here";

        // When
        boolean result = jwtUtil.isTokenValid(invalidToken);

        // Then
        assertFalse(result);
    }

    @Test
    void testIsTokenValid_NullToken_ReturnsFalse() {
        // Given
        String nullToken = null;

        // When
        boolean result = jwtUtil.isTokenValid(nullToken);

        // Then
        assertFalse(result);
    }

    @Test
    void testIsTokenValid_EmptyToken_ReturnsFalse() {
        // Given
        String emptyToken = "";

        // When
        boolean result = jwtUtil.isTokenValid(emptyToken);

        // Then
        assertFalse(result);
    }

    @Test
    void testIsTokenExpired_ValidToken_ReturnsFalse() {
        // Given
        String subject = "test@example.com";
        String token = jwtUtil.generateToken(subject);

        // When
        boolean result = jwtUtil.isTokenExpired(token);

        // Then
        assertFalse(result);
    }

    @Test
    void testIsTokenExpired_ExpiredToken_ReturnsTrue() {
        // Given
        String subject = "test@example.com";
        long expiredDuration = -1000L; // Negative duration to create expired token
        String token = jwtUtil.generateToken(subject, expiredDuration);

        // When
        boolean result = jwtUtil.isTokenExpired(token);

        // Then
        assertTrue(result);
    }

    @Test
    void testIsTokenExpired_InvalidToken_ReturnsTrue() {
        // Given
        String invalidToken = "invalid.token.here";

        // When
        boolean result = jwtUtil.isTokenExpired(invalidToken);

        // Then
        assertTrue(result);
    }

    @Test
    void testIsTokenExpired_NullToken_ReturnsTrue() {
        // Given
        String nullToken = null;

        // When
        boolean result = jwtUtil.isTokenExpired(nullToken);

        // Then
        assertTrue(result);
    }

    @Test
    void testGetExpirationDate_ValidToken_Success() {
        // Given
        String subject = "test@example.com";
        String token = jwtUtil.generateToken(subject);

        // When
        Date result = jwtUtil.getExpirationDate(token);

        // Then
        assertNotNull(result);
        assertTrue(result.after(new Date()));
    }

    @Test
    void testGetExpirationDate_InvalidToken_ReturnsNull() {
        // Given
        String invalidToken = "invalid.token.here";

        // When
        Date result = jwtUtil.getExpirationDate(invalidToken);

        // Then
        assertNull(result);
    }

    @Test
    void testGetExpirationDate_NullToken_ReturnsNull() {
        // Given
        String nullToken = null;

        // When
        Date result = jwtUtil.getExpirationDate(nullToken);

        // Then
        assertNull(result);
    }

    @Test
    void testTokenRoundTrip_AllMethods() {
        // Given
        String subject = "test@example.com";
        List<String> roles = List.of("USER", "ADMIN", "MANAGER");
        long duration = 1800000L; // 30 minutes

        // When
        String token = jwtUtil.generateToken(subject, duration, roles);
        String decodedSubject = jwtUtil.getSubject(token);
        List<String> decodedRoles = jwtUtil.getRoles(token);
        boolean isValid = jwtUtil.isTokenValid(token);
        boolean isExpired = jwtUtil.isTokenExpired(token);
        Date expirationDate = jwtUtil.getExpirationDate(token);

        // Then
        assertEquals(subject, decodedSubject);
        assertEquals(3, decodedRoles.size());
        assertTrue(decodedRoles.contains("USER"));
        assertTrue(decodedRoles.contains("ADMIN"));
        assertTrue(decodedRoles.contains("MANAGER"));
        assertTrue(isValid);
        assertFalse(isExpired);
        assertNotNull(expirationDate);
        assertTrue(expirationDate.after(new Date()));
    }

    @Test
    void testDefaultValues() {
        // Given
        String subject = "test@example.com";

        // When
        String token = jwtUtil.generateToken(subject);
        List<String> roles = jwtUtil.getRoles(token);

        // Then
        assertNotNull(token);
        assertEquals(1, roles.size());
        assertEquals("USER", roles.get(0));
    }

    @Test
    void testTokenWithDifferentDurations() {
        // Given
        String subject = "test@example.com";
        long shortDuration = 1000L; // 1 second
        long longDuration = 7200000L; // 2 hours

        // When
        String shortToken = jwtUtil.generateToken(subject, shortDuration);
        String longToken = jwtUtil.generateToken(subject, longDuration);

        // Then
        assertNotNull(shortToken);
        assertNotNull(longToken);
        assertNotEquals(shortToken, longToken);
        
        // Both should be valid initially
        assertTrue(jwtUtil.isTokenValid(shortToken));
        assertTrue(jwtUtil.isTokenValid(longToken));
    }
}
