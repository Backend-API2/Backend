package backend_api.Backend.Service.Implementation;

import backend_api.Backend.Entity.payment.Payment;
import backend_api.Backend.Entity.payment.PaymentStatus;
import backend_api.Backend.Entity.user.User;
import backend_api.Backend.Entity.user.UserRole;
import backend_api.Backend.Repository.PaymentRepository;
import backend_api.Backend.Repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

/**
 * Unit tests for BalanceServiceImpl
 * Testing balance management operations including balance checks, deductions, and additions
 */
@ExtendWith(MockitoExtension.class)
class BalanceServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PaymentRepository paymentRepository;

    @InjectMocks
    private BalanceServiceImpl balanceService;

    private User testUser;
    private User merchantUser;
    private Payment testPayment;

    @BeforeEach
    void setUp() {
        // Setup regular user
        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");
        testUser.setName("Test User");
        testUser.setRole(UserRole.USER);
        testUser.setSaldo_disponible(BigDecimal.valueOf(100.00));

        // Setup merchant user
        merchantUser = new User();
        merchantUser.setId(2L);
        merchantUser.setEmail("merchant@example.com");
        merchantUser.setName("Merchant User");
        merchantUser.setRole(UserRole.MERCHANT);
        merchantUser.setSaldo_disponible(BigDecimal.valueOf(0.00));

        // Setup test payment
        testPayment = new Payment();
        testPayment.setId(1L);
        testPayment.setUser_id(1L);
        testPayment.setAmount_total(BigDecimal.valueOf(50.00));
        testPayment.setStatus(PaymentStatus.REJECTED);
        testPayment.setRejected_by_balance(true);
        testPayment.setRetry_attempts(1);
        testPayment.setCreated_at(LocalDateTime.now());
        testPayment.setUpdated_at(LocalDateTime.now());
    }

    @Test
    void testHasSufficientBalance_RegularUser_SufficientBalance() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        // When
        boolean result = balanceService.hasSufficientBalance(1L, BigDecimal.valueOf(50.00));

        // Then
        assertTrue(result);
        verify(userRepository).findById(1L);
    }

    @Test
    void testHasSufficientBalance_RegularUser_InsufficientBalance() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        // When
        boolean result = balanceService.hasSufficientBalance(1L, BigDecimal.valueOf(150.00));

        // Then
        assertFalse(result);
        verify(userRepository).findById(1L);
    }

    @Test
    void testHasSufficientBalance_MerchantUser_AlwaysTrue() {
        // Given
        when(userRepository.findById(2L)).thenReturn(Optional.of(merchantUser));

        // When
        boolean result = balanceService.hasSufficientBalance(2L, BigDecimal.valueOf(1000.00));

        // Then
        assertTrue(result);
        verify(userRepository).findById(2L);
    }

    @Test
    void testHasSufficientBalance_UserNotFound() {
        // Given
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                balanceService.hasSufficientBalance(1L, BigDecimal.valueOf(50.00)));
        assertEquals("Usuario no encontrado", exception.getMessage());
    }

    @Test
    void testDeductBalance_RegularUser_Success() {
        // Given
        User updatedUser = new User();
        updatedUser.setId(1L);
        updatedUser.setEmail(testUser.getEmail());
        updatedUser.setName(testUser.getName());
        updatedUser.setRole(testUser.getRole());
        updatedUser.setSaldo_disponible(BigDecimal.valueOf(50.00));

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(updatedUser);

        // When
        User result = balanceService.deductBalance(1L, BigDecimal.valueOf(50.00));

        // Then
        assertNotNull(result);
        assertEquals(BigDecimal.valueOf(50.00), result.getSaldo_disponible());
        verify(userRepository, times(2)).findById(1L); // Called twice: once in deductBalance, once in hasSufficientBalance
        verify(userRepository).save(any(User.class));
    }

    @Test
    void testDeductBalance_RegularUser_InsufficientBalance() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        // When & Then
        IllegalStateException exception = assertThrows(IllegalStateException.class, () ->
                balanceService.deductBalance(1L, BigDecimal.valueOf(150.00)));
        assertEquals("Saldo insuficiente", exception.getMessage());
        verify(userRepository, times(2)).findById(1L); // Called twice: once in deductBalance, once in hasSufficientBalance
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void testDeductBalance_MerchantUser_NoDeduction() {
        // Given
        when(userRepository.findById(2L)).thenReturn(Optional.of(merchantUser));

        // When
        User result = balanceService.deductBalance(2L, BigDecimal.valueOf(50.00));

        // Then
        assertNotNull(result);
        assertEquals(merchantUser, result);
        verify(userRepository).findById(2L);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void testDeductBalance_UserNotFound() {
        // Given
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                balanceService.deductBalance(1L, BigDecimal.valueOf(50.00)));
        assertEquals("Usuario no encontrado", exception.getMessage());
    }

    @Test
    void testAddBalance_RegularUser_Success() {
        // Given
        User updatedUser = new User();
        updatedUser.setId(1L);
        updatedUser.setEmail(testUser.getEmail());
        updatedUser.setName(testUser.getName());
        updatedUser.setRole(testUser.getRole());
        updatedUser.setSaldo_disponible(BigDecimal.valueOf(150.00));

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(updatedUser);

        // When
        User result = balanceService.addBalance(1L, BigDecimal.valueOf(50.00));

        // Then
        assertNotNull(result);
        assertEquals(BigDecimal.valueOf(150.00), result.getSaldo_disponible());
        verify(userRepository).findById(1L);
        verify(userRepository).save(any(User.class));
    }

    @Test
    void testAddBalance_MerchantUser_NoAddition() {
        // Given
        when(userRepository.findById(2L)).thenReturn(Optional.of(merchantUser));

        // When
        User result = balanceService.addBalance(2L, BigDecimal.valueOf(50.00));

        // Then
        assertNotNull(result);
        assertEquals(merchantUser, result);
        verify(userRepository).findById(2L);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void testAddBalance_UserNotFound() {
        // Given
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                balanceService.addBalance(1L, BigDecimal.valueOf(50.00)));
        assertEquals("Usuario no encontrado", exception.getMessage());
    }

    @Test
    void testGetCurrentBalance_Success() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        // When
        BigDecimal result = balanceService.getCurrentBalance(1L);

        // Then
        assertEquals(BigDecimal.valueOf(100.00), result);
        verify(userRepository).findById(1L);
    }

    @Test
    void testGetCurrentBalance_UserNotFound() {
        // Given
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                balanceService.getCurrentBalance(1L));
        assertEquals("Usuario no encontrado", exception.getMessage());
    }

    @Test
    void testCanRetryPayment_CanRetry() {
        // Given
        when(paymentRepository.findById(1L)).thenReturn(Optional.of(testPayment));

        // When
        boolean result = balanceService.canRetryPayment(1L);

        // Then
        assertTrue(result);
        verify(paymentRepository).findById(1L);
    }

    @Test
    void testCanRetryPayment_MaxAttemptsReached() {
        // Given
        testPayment.setRetry_attempts(3);
        when(paymentRepository.findById(1L)).thenReturn(Optional.of(testPayment));

        // When
        boolean result = balanceService.canRetryPayment(1L);

        // Then
        assertFalse(result);
        verify(paymentRepository).findById(1L);
    }

    @Test
    void testCanRetryPayment_NotRejectedByBalance() {
        // Given
        testPayment.setRejected_by_balance(false);
        when(paymentRepository.findById(1L)).thenReturn(Optional.of(testPayment));

        // When
        boolean result = balanceService.canRetryPayment(1L);

        // Then
        assertFalse(result);
        verify(paymentRepository).findById(1L);
    }

    @Test
    void testCanRetryPayment_RejectedByBalanceNull() {
        // Given
        testPayment.setRejected_by_balance(null);
        when(paymentRepository.findById(1L)).thenReturn(Optional.of(testPayment));

        // When
        boolean result = balanceService.canRetryPayment(1L);

        // Then
        assertFalse(result);
        verify(paymentRepository).findById(1L);
    }

    @Test
    void testCanRetryPayment_PaymentNotFound() {
        // Given
        when(paymentRepository.findById(anyLong())).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                balanceService.canRetryPayment(1L));
        assertEquals("Pago no encontrado", exception.getMessage());
    }
}
