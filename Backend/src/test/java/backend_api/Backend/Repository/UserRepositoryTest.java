package backend_api.Backend.Repository;

import backend_api.Backend.Entity.user.User;
import backend_api.Backend.Entity.user.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class UserRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    private User testUser;

    @BeforeEach
    void setUp() {
        // Create test user
        testUser = new User();
        testUser.setEmail("test@example.com");
        testUser.setPassword("encodedPassword");
        testUser.setName("Test User");
        testUser.setPhone("123456789");
        testUser.setRole(UserRole.USER);
        testUser.setSaldo_disponible(BigDecimal.valueOf(25000.00));
        testUser = entityManager.persistAndFlush(testUser);
    }

    @Test
    void testFindByEmail_Success() {
        // When
        Optional<User> result = userRepository.findByEmail("test@example.com");

        // Then
        assertTrue(result.isPresent());
        assertEquals("test@example.com", result.get().getEmail());
        assertEquals("Test User", result.get().getName());
        assertEquals(UserRole.USER, result.get().getRole());
        assertEquals(testUser.getId(), result.get().getId());
    }

    @Test
    void testFindByEmail_NotFound() {
        // When
        Optional<User> result = userRepository.findByEmail("nonexistent@example.com");

        // Then
        assertFalse(result.isPresent());
    }

    @Test
    void testExistsByEmail_True() {
        // When
        boolean exists = userRepository.existsByEmail("test@example.com");

        // Then
        assertTrue(exists);
    }

    @Test
    void testExistsByEmail_False() {
        // When
        boolean exists = userRepository.existsByEmail("nonexistent@example.com");

        // Then
        assertFalse(exists);
    }

    @Test
    void testSaveUser_Success() {
        // Given
        User newUser = new User();
        newUser.setEmail("newuser@example.com");
        newUser.setPassword("encodedPassword");
        newUser.setName("New User");
        newUser.setPhone("987654321");
        newUser.setRole(UserRole.MERCHANT);
        newUser.setSaldo_disponible(BigDecimal.valueOf(50000.00));

        // When
        User savedUser = userRepository.save(newUser);

        // Then
        assertNotNull(savedUser);
        assertNotNull(savedUser.getId());
        assertEquals("newuser@example.com", savedUser.getEmail());
        assertEquals("New User", savedUser.getName());
        assertEquals(UserRole.MERCHANT, savedUser.getRole());
        assertEquals(BigDecimal.valueOf(50000.00), savedUser.getSaldo_disponible());
    }

    @Test
    void testUpdateUser_Success() {
        // Given
        testUser.setName("Updated User");
        testUser.setPhone("111111111");
        testUser.setSaldo_disponible(BigDecimal.valueOf(30000.00));

        // When
        User updatedUser = userRepository.save(testUser);

        // Then
        assertNotNull(updatedUser);
        assertEquals(testUser.getId(), updatedUser.getId());
        assertEquals("Updated User", updatedUser.getName());
        assertEquals("111111111", updatedUser.getPhone());
        assertEquals(BigDecimal.valueOf(30000.00), updatedUser.getSaldo_disponible());
    }

    @Test
    void testDeleteUser_Success() {
        // Given
        Long userId = testUser.getId();

        // When
        userRepository.deleteById(userId);

        // Then
        Optional<User> deletedUser = userRepository.findById(userId);
        assertFalse(deletedUser.isPresent());
    }

    @Test
    void testFindById_Success() {
        // When
        Optional<User> result = userRepository.findById(testUser.getId());

        // Then
        assertTrue(result.isPresent());
        assertEquals(testUser.getId(), result.get().getId());
        assertEquals("test@example.com", result.get().getEmail());
        assertEquals("Test User", result.get().getName());
    }

    @Test
    void testFindById_NotFound() {
        // When
        Optional<User> result = userRepository.findById(999L);

        // Then
        assertFalse(result.isPresent());
    }

    @Test
    void testFindAllUsers_Success() {
        // Given
        User user2 = new User();
        user2.setEmail("user2@example.com");
        user2.setPassword("encodedPassword");
        user2.setName("User 2");
        user2.setPhone("222222222");
        user2.setRole(UserRole.MERCHANT);
        user2.setSaldo_disponible(BigDecimal.valueOf(40000.00));
        entityManager.persistAndFlush(user2);

        // When
        Iterable<User> result = userRepository.findAll();

        // Then
        assertNotNull(result);
        int count = 0;
        for (User user : result) {
            count++;
            assertNotNull(user.getId());
            assertNotNull(user.getEmail());
            assertNotNull(user.getName());
        }
        assertEquals(2, count);
    }

    @Test
    void testFindByRole_Success() {
        // Given
        User merchant = new User();
        merchant.setEmail("merchant@example.com");
        merchant.setPassword("encodedPassword");
        merchant.setName("Merchant User");
        merchant.setPhone("333333333");
        merchant.setRole(UserRole.MERCHANT);
        merchant.setSaldo_disponible(BigDecimal.valueOf(60000.00));
        entityManager.persistAndFlush(merchant);

        // When
        Iterable<User> result = userRepository.findAll();

        // Then
        assertNotNull(result);
        int userCount = 0;
        int merchantCount = 0;
        for (User user : result) {
            if (user.getRole() == UserRole.USER) {
                userCount++;
            } else if (user.getRole() == UserRole.MERCHANT) {
                merchantCount++;
            }
        }
        assertEquals(1, userCount);
        assertEquals(1, merchantCount);
    }
}