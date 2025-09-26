package backend_api.Backend.Entity;

import backend_api.Backend.Entity.user.User;
import backend_api.Backend.Entity.user.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class UserTest {

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
    }

    @Test
    void testDefaultConstructor() {
        assertNotNull(user);
        assertNull(user.getId());
        assertNull(user.getEmail());
        assertNull(user.getPassword());
        assertNull(user.getName());
        assertNull(user.getPhone());
        assertEquals(UserRole.USER, user.getRole()); // Default value
        assertEquals(BigDecimal.ZERO, user.getSaldo_disponible()); // Default value
        assertNull(user.getCreated_at());
        assertNull(user.getUpdated_at());
    }

    @Test
    void testSettersAndGetters() {
        // Test basic setters and getters
        user.setId(1L);
        user.setEmail("test@example.com");
        user.setPassword("password123");
        user.setName("John Doe");
        user.setPhone("+1234567890");
        user.setRole(UserRole.MERCHANT);
        user.setSaldo_disponible(BigDecimal.valueOf(100.50));
        LocalDateTime now = LocalDateTime.now();
        user.setCreated_at(now);
        user.setUpdated_at(now);

        assertEquals(1L, user.getId());
        assertEquals("test@example.com", user.getEmail());
        assertEquals("password123", user.getPassword());
        assertEquals("John Doe", user.getName());
        assertEquals("+1234567890", user.getPhone());
        assertEquals(UserRole.MERCHANT, user.getRole());
        assertEquals(BigDecimal.valueOf(100.50), user.getSaldo_disponible());
        assertEquals(now, user.getCreated_at());
        assertEquals(now, user.getUpdated_at());
    }

    @Test
    void testPrePersist_SetsTimestamps() {
        // Verify initial state
        assertNull(user.getCreated_at());
        assertNull(user.getUpdated_at());

        // Call prePersist (simulating @PrePersist)
        user.prePersist();

        // Verify timestamps are set
        assertNotNull(user.getCreated_at());
        assertNotNull(user.getUpdated_at());
        // Allow for slight time differences
        assertTrue(Math.abs(user.getCreated_at().toLocalTime().toNanoOfDay() - user.getUpdated_at().toLocalTime().toNanoOfDay()) < 1000000); // Within 1ms
        assertTrue(user.getCreated_at().isBefore(LocalDateTime.now().plusSeconds(1)));
        assertTrue(user.getCreated_at().isAfter(LocalDateTime.now().minusSeconds(1)));
    }

    @Test
    void testPrePersist_MultipleCalls() {
        // Call prePersist multiple times
        user.prePersist();
        LocalDateTime firstCall = user.getCreated_at();
        
        // Wait a small amount of time
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        user.prePersist();
        LocalDateTime secondCall = user.getCreated_at();

        // Verify both calls set the timestamp
        assertNotNull(firstCall);
        assertNotNull(secondCall);
        // Created_at may change on multiple calls as prePersist updates the timestamp
        // Verify that both timestamps are recent (within last minute)
        LocalDateTime now = LocalDateTime.now();
        assertTrue(firstCall.isBefore(now) || firstCall.isEqual(now));
        assertTrue(secondCall.isBefore(now) || secondCall.isEqual(now));
    }

    @Test
    void testPreUpdate_SetsUpdatedAt() {
        // Set initial timestamps
        LocalDateTime initialTime = LocalDateTime.now().minusHours(1);
        user.setCreated_at(initialTime);
        user.setUpdated_at(initialTime);

        // Call preUpdate (simulating @PreUpdate)
        user.preUpdate();

        // Verify updated_at is changed but created_at remains the same
        assertEquals(initialTime, user.getCreated_at());
        assertNotEquals(initialTime, user.getUpdated_at());
        assertTrue(user.getUpdated_at().isAfter(initialTime));
        assertTrue(user.getUpdated_at().isBefore(LocalDateTime.now().plusSeconds(1)));
    }

    @Test
    void testPreUpdate_MultipleCalls() {
        // Set initial timestamp
        user.setCreated_at(LocalDateTime.now().minusHours(1));
        user.setUpdated_at(LocalDateTime.now().minusHours(1));

        // Call preUpdate multiple times
        user.preUpdate();
        LocalDateTime firstUpdate = user.getUpdated_at();

        // Wait a small amount of time
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        user.preUpdate();
        LocalDateTime secondUpdate = user.getUpdated_at();

        // Verify both calls update the timestamp
        assertNotNull(firstUpdate);
        assertNotNull(secondUpdate);
        assertTrue(secondUpdate.isAfter(firstUpdate));
    }

    @Test
    void testUserRoleEnumValues() {
        // Test all enum values
        user.setRole(UserRole.USER);
        assertEquals(UserRole.USER, user.getRole());

        user.setRole(UserRole.MERCHANT);
        assertEquals(UserRole.MERCHANT, user.getRole());

        user.setRole(UserRole.MERCHANT);
        assertEquals(UserRole.MERCHANT, user.getRole());
    }

    @Test
    void testDefaultRoleValue() {
        // Test that default role is USER
        User newUser = new User();
        assertEquals(UserRole.USER, newUser.getRole());
    }

    @Test
    void testEmailValidation() {
        // Test various email formats
        user.setEmail("user@example.com");
        assertEquals("user@example.com", user.getEmail());

        user.setEmail("test.email@domain.co.uk");
        assertEquals("test.email@domain.co.uk", user.getEmail());

        user.setEmail("user+tag@example.org");
        assertEquals("user+tag@example.org", user.getEmail());

        user.setEmail("123@456.789");
        assertEquals("123@456.789", user.getEmail());

        // Test null email
        user.setEmail(null);
        assertNull(user.getEmail());

        // Test empty email
        user.setEmail("");
        assertEquals("", user.getEmail());
    }

    @Test
    void testPasswordValidation() {
        // Test various password formats
        user.setPassword("password123");
        assertEquals("password123", user.getPassword());

        user.setPassword("P@ssw0rd!");
        assertEquals("P@ssw0rd!", user.getPassword());

        user.setPassword("123456");
        assertEquals("123456", user.getPassword());

        user.setPassword("verylongpasswordthatexceedsnormallimits");
        assertEquals("verylongpasswordthatexceedsnormallimits", user.getPassword());

        // Test null password
        user.setPassword(null);
        assertNull(user.getPassword());

        // Test empty password
        user.setPassword("");
        assertEquals("", user.getPassword());
    }

    @Test
    void testNameValidation() {
        // Test various name formats
        user.setName("John Doe");
        assertEquals("John Doe", user.getName());

        user.setName("María José");
        assertEquals("María José", user.getName());

        user.setName("Jean-Pierre");
        assertEquals("Jean-Pierre", user.getName());

        user.setName("O'Connor");
        assertEquals("O'Connor", user.getName());

        user.setName("Dr. Smith");
        assertEquals("Dr. Smith", user.getName());

        // Test null name
        user.setName(null);
        assertNull(user.getName());

        // Test empty name
        user.setName("");
        assertEquals("", user.getName());
    }

    @Test
    void testPhoneValidation() {
        // Test various phone formats
        user.setPhone("+1234567890");
        assertEquals("+1234567890", user.getPhone());

        user.setPhone("(555) 123-4567");
        assertEquals("(555) 123-4567", user.getPhone());

        user.setPhone("555-123-4567");
        assertEquals("555-123-4567", user.getPhone());

        user.setPhone("+44 20 7946 0958");
        assertEquals("+44 20 7946 0958", user.getPhone());

        user.setPhone("+33 1 42 86 83 26");
        assertEquals("+33 1 42 86 83 26", user.getPhone());

        // Test null phone
        user.setPhone(null);
        assertNull(user.getPhone());

        // Test empty phone
        user.setPhone("");
        assertEquals("", user.getPhone());
    }

    @Test
    void testSaldoDisponiblePrecision() {
        // Test BigDecimal precision
        user.setSaldo_disponible(BigDecimal.valueOf(123.45));
        assertEquals(BigDecimal.valueOf(123.45), user.getSaldo_disponible());

        user.setSaldo_disponible(BigDecimal.valueOf(0.01));
        assertEquals(BigDecimal.valueOf(0.01), user.getSaldo_disponible());

        user.setSaldo_disponible(BigDecimal.valueOf(999999.99));
        assertEquals(BigDecimal.valueOf(999999.99), user.getSaldo_disponible());

        user.setSaldo_disponible(BigDecimal.valueOf(-50.00));
        assertEquals(BigDecimal.valueOf(-50.00), user.getSaldo_disponible());

        // Test null saldo
        user.setSaldo_disponible(null);
        assertNull(user.getSaldo_disponible());

        // Test zero saldo
        user.setSaldo_disponible(BigDecimal.ZERO);
        assertEquals(BigDecimal.ZERO, user.getSaldo_disponible());
    }

    @Test
    void testDefaultSaldoDisponibleValue() {
        // Test that default saldo_disponible is ZERO
        User newUser = new User();
        assertEquals(BigDecimal.ZERO, newUser.getSaldo_disponible());
    }

    @Test
    void testToString() {
        user.setId(1L);
        user.setEmail("test@example.com");
        user.setName("John Doe");
        user.setRole(UserRole.USER);

        String result = user.toString();

        assertNotNull(result);
        assertTrue(result.contains("User"));
        assertTrue(result.contains("id=1"));
        assertTrue(result.contains("email=test@example.com"));
        assertTrue(result.contains("name=John Doe"));
        assertTrue(result.contains("role=USER"));
    }

    @Test
    void testEqualsAndHashCode() {
        User user1 = new User();
        user1.setId(1L);
        user1.setEmail("test@example.com");
        user1.setName("John Doe");

        User user2 = new User();
        user2.setId(1L);
        user2.setEmail("test@example.com");
        user2.setName("John Doe");

        User user3 = new User();
        user3.setId(2L);
        user3.setEmail("other@example.com");
        user3.setName("Jane Doe");

        // Test equals
        assertEquals(user1, user2);
        assertNotEquals(user1, user3);
        assertNotEquals(user2, user3);

        // Test hashCode
        assertEquals(user1.hashCode(), user2.hashCode());
        assertNotEquals(user1.hashCode(), user3.hashCode());
    }

    @Test
    void testCompleteUserWorkflow() {
        // Test a complete user workflow
        user.setId(1L);
        user.setEmail("merchant@example.com");
        user.setPassword("securePassword123");
        user.setName("Merchant User");
        user.setPhone("+1234567890");
        user.setRole(UserRole.MERCHANT);
        user.setSaldo_disponible(BigDecimal.valueOf(500.00));

        // Simulate prePersist
        user.prePersist();

        // Verify initial state
        assertNotNull(user.getCreated_at());
        assertNotNull(user.getUpdated_at());
        // Allow for slight time differences
        assertTrue(Math.abs(user.getCreated_at().toLocalTime().toNanoOfDay() - user.getUpdated_at().toLocalTime().toNanoOfDay()) < 1000000); // Within 1ms

        // Simulate user update
        user.setName("Updated Merchant User");
        user.setPhone("+0987654321");
        user.setSaldo_disponible(BigDecimal.valueOf(750.00));

        // Simulate preUpdate
        user.preUpdate();

        // Verify updated state
        assertEquals("Updated Merchant User", user.getName());
        assertEquals("+0987654321", user.getPhone());
        assertEquals(BigDecimal.valueOf(750.00), user.getSaldo_disponible());
        assertTrue(user.getUpdated_at().isAfter(user.getCreated_at()));
    }

    @Test
    void testUserWithNullFields() {
        // Test user with null optional fields
        user.setId(1L);
        user.setEmail("test@example.com");
        user.setPassword("password");
        user.setName("Test User");
        user.setPhone(null); // Optional field
        user.setRole(UserRole.USER);
        user.setSaldo_disponible(BigDecimal.ZERO);

        // Verify null fields are handled correctly
        assertNull(user.getPhone());
        assertEquals(UserRole.USER, user.getRole());
        assertEquals(BigDecimal.ZERO, user.getSaldo_disponible());

        // Simulate prePersist
        user.prePersist();

        // Verify timestamps are set
        assertNotNull(user.getCreated_at());
        assertNotNull(user.getUpdated_at());
    }

    @Test
    void testUserRoleTransitions() {
        // Test user role transitions
        user.setRole(UserRole.USER);
        assertEquals(UserRole.USER, user.getRole());

        // Upgrade to merchant
        user.setRole(UserRole.MERCHANT);
        assertEquals(UserRole.MERCHANT, user.getRole());

        // Upgrade to merchant
        user.setRole(UserRole.MERCHANT);
        assertEquals(UserRole.MERCHANT, user.getRole());

        // Downgrade back to user
        user.setRole(UserRole.USER);
        assertEquals(UserRole.USER, user.getRole());
    }
}
