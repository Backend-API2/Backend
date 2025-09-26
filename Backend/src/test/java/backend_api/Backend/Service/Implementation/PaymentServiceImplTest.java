package backend_api.Backend.Service.Implementation;

import backend_api.Backend.Entity.payment.Payment;
import backend_api.Backend.Entity.payment.PaymentMethod;
import backend_api.Backend.Entity.payment.PaymentStatus;
import backend_api.Backend.Entity.payment.PaymentEventType;
import backend_api.Backend.Entity.payment.types.CreditCardPayment;
import backend_api.Backend.Repository.PaymentRepository;
import backend_api.Backend.Service.Interface.PaymentAttemptService;
import backend_api.Backend.Service.Interface.PaymentEventService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceImplTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private PaymentEventService paymentEventService;

    @Mock
    private PaymentAttemptService paymentAttemptService;

    @InjectMocks
    private PaymentServiceImpl paymentService;

    private Payment testPayment;
    private CreditCardPayment testPaymentMethod;

    @BeforeEach
    void setUp() {
        testPayment = new Payment();
        testPayment.setId(1L);
        testPayment.setUser_id(100L);
        testPayment.setProvider_id(200L);
        testPayment.setAmount_total(BigDecimal.valueOf(100.00));
        testPayment.setStatus(PaymentStatus.PENDING_PAYMENT);
        testPayment.setCreated_at(LocalDateTime.now());
        testPayment.setUpdated_at(LocalDateTime.now());

        testPaymentMethod = new CreditCardPayment();
        testPaymentMethod.setId(1L);
        testPaymentMethod.setType(backend_api.Backend.Entity.payment.types.PaymentMethodType.CREDIT_CARD);
    }

    @Test
    void testCreatePayment_Success() {
        // Given
        when(paymentRepository.save(any(Payment.class))).thenReturn(testPayment);

        // When
        Payment result = paymentService.createPayment(testPayment);

        // Then
        assertNotNull(result);
        assertEquals(testPayment.getId(), result.getId());
        assertNotNull(result.getCreated_at());
        assertNotNull(result.getUpdated_at());
        verify(paymentRepository).save(testPayment);
    }

    @Test
    void testGetPaymentById_Success() {
        // Given
        Long paymentId = 1L;
        when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(testPayment));

        // When
        Optional<Payment> result = paymentService.getPaymentById(paymentId);

        // Then
        assertTrue(result.isPresent());
        assertEquals(testPayment.getId(), result.get().getId());
        verify(paymentRepository).findById(paymentId);
    }

    @Test
    void testGetPaymentById_NotFound() {
        // Given
        Long paymentId = 999L;
        when(paymentRepository.findById(paymentId)).thenReturn(Optional.empty());

        // When
        Optional<Payment> result = paymentService.getPaymentById(paymentId);

        // Then
        assertFalse(result.isPresent());
        verify(paymentRepository).findById(paymentId);
    }

    @Test
    void testGetAllPayments_Success() {
        // Given
        List<Payment> payments = Arrays.asList(testPayment);
        when(paymentRepository.findAll()).thenReturn(payments);

        // When
        List<Payment> result = paymentService.getAllPayments();

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testPayment.getId(), result.get(0).getId());
        verify(paymentRepository).findAll();
    }

    @Test
    void testGetPaymentsByUserId_Success() {
        // Given
        Long userId = 100L;
        List<Payment> payments = Arrays.asList(testPayment);
        when(paymentRepository.findByUserId(userId)).thenReturn(payments);

        // When
        List<Payment> result = paymentService.getPaymentsByUserId(userId);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testPayment.getId(), result.get(0).getId());
        verify(paymentRepository).findByUserId(userId);
    }

    @Test
    void testGetPaymentsByProviderId_Success() {
        // Given
        Long providerId = 200L;
        List<Payment> payments = Arrays.asList(testPayment);
        when(paymentRepository.findByProviderId(providerId)).thenReturn(payments);

        // When
        List<Payment> result = paymentService.getPaymentsByProviderId(providerId);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testPayment.getId(), result.get(0).getId());
        verify(paymentRepository).findByProviderId(providerId);
    }

    @Test
    void testGetPaymentsByStatus_Success() {
        // Given
        PaymentStatus status = PaymentStatus.PENDING_PAYMENT;
        List<Payment> payments = Arrays.asList(testPayment);
        when(paymentRepository.findByStatus(status)).thenReturn(payments);

        // When
        List<Payment> result = paymentService.getPaymentsByStatus(status);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testPayment.getId(), result.get(0).getId());
        verify(paymentRepository).findByStatus(status);
    }

    @Test
    void testGetPaymentsByMethod_Success() {
        // Given
        List<Payment> payments = Arrays.asList(testPayment);
        when(paymentRepository.findByMethod(testPaymentMethod)).thenReturn(payments);

        // When
        List<Payment> result = paymentService.getPaymentsByMethod(testPaymentMethod);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testPayment.getId(), result.get(0).getId());
        verify(paymentRepository).findByMethod(testPaymentMethod);
    }

    @Test
    void testGetPaymentsByGatewayTxnId_Success() {
        // Given
        String gatewayTxnId = "txn_123";
        when(paymentRepository.findByGatewayTxnId(gatewayTxnId)).thenReturn(Optional.of(testPayment));

        // When
        Optional<Payment> result = paymentService.getPaymentsByGatewayTxnId(gatewayTxnId);

        // Then
        assertTrue(result.isPresent());
        assertEquals(testPayment.getId(), result.get().getId());
        verify(paymentRepository).findByGatewayTxnId(gatewayTxnId);
    }

    @Test
    void testGetPaymentsBySolicitudId_Success() {
        // Given
        Long solicitudId = 1L;
        List<Payment> payments = Arrays.asList(testPayment);
        when(paymentRepository.findBySolicitudId(solicitudId)).thenReturn(payments);

        // When
        List<Payment> result = paymentService.getPaymentsBySolicitudId(solicitudId);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testPayment.getId(), result.get(0).getId());
        verify(paymentRepository).findBySolicitudId(solicitudId);
    }

    @Test
    void testGetPaymentsByCotizacionId_Success() {
        // Given
        Long cotizacionId = 1L;
        List<Payment> payments = Arrays.asList(testPayment);
        when(paymentRepository.findByCotizacionId(cotizacionId)).thenReturn(payments);

        // When
        List<Payment> result = paymentService.getPaymentsByCotizacionId(cotizacionId);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testPayment.getId(), result.get(0).getId());
        verify(paymentRepository).findByCotizacionId(cotizacionId);
    }

    @Test
    void testGetPaymentsByAmountGreaterThan_Success() {
        // Given
        BigDecimal minAmount = BigDecimal.valueOf(50.00);
        List<Payment> payments = Arrays.asList(testPayment);
        when(paymentRepository.findByAmountTotalGreaterThanEqual(minAmount)).thenReturn(payments);

        // When
        List<Payment> result = paymentService.getPaymentsByAmountGreaterThan(minAmount);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testPayment.getId(), result.get(0).getId());
        verify(paymentRepository).findByAmountTotalGreaterThanEqual(minAmount);
    }

    @Test
    void testGetPaymentsByDateRange_Success() {
        // Given
        LocalDate startDate = LocalDate.now().minusDays(7);
        LocalDate endDate = LocalDate.now();
        List<Payment> payments = Arrays.asList(testPayment);
        when(paymentRepository.findByCreatedAtBetween(startDate, endDate)).thenReturn(payments);

        // When
        List<Payment> result = paymentService.getPaymentsByDateRange(startDate, endDate);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testPayment.getId(), result.get(0).getId());
        verify(paymentRepository).findByCreatedAtBetween(startDate, endDate);
    }

    @Test
    void testGetPaymentsByUserAndStatus_Success() {
        // Given
        Long userId = 100L;
        PaymentStatus status = PaymentStatus.PENDING_PAYMENT;
        List<Payment> payments = Arrays.asList(testPayment);
        when(paymentRepository.findByUserIdAndStatus(userId, status)).thenReturn(payments);

        // When
        List<Payment> result = paymentService.getPaymentsByUserAndStatus(userId, status);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testPayment.getId(), result.get(0).getId());
        verify(paymentRepository).findByUserIdAndStatus(userId, status);
    }

    @Test
    void testGetPaymentsByProviderAndStatus_Success() {
        // Given
        Long providerId = 200L;
        PaymentStatus status = PaymentStatus.PENDING_PAYMENT;
        List<Payment> payments = Arrays.asList(testPayment);
        when(paymentRepository.findByProviderIdAndStatus(providerId, status)).thenReturn(payments);

        // When
        List<Payment> result = paymentService.getPaymentsByProviderAndStatus(providerId, status);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testPayment.getId(), result.get(0).getId());
        verify(paymentRepository).findByProviderIdAndStatus(providerId, status);
    }

    @Test
    void testGetPaymentsByCurrency_Success() {
        // Given
        String currency = "USD";
        List<Payment> payments = Arrays.asList(testPayment);
        when(paymentRepository.findByCurrency(currency)).thenReturn(payments);

        // When
        List<Payment> result = paymentService.getPaymentsByCurrency(currency);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testPayment.getId(), result.get(0).getId());
        verify(paymentRepository).findByCurrency(currency);
    }

    @Test
    void testUpdatePaymentStatus_Success() {
        // Given
        Long paymentId = 1L;
        PaymentStatus newStatus = PaymentStatus.APPROVED;
        when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(testPayment));
        when(paymentRepository.save(any(Payment.class))).thenReturn(testPayment);

        // When
        Payment result = paymentService.updatePaymentStatus(paymentId, newStatus);

        // Then
        assertNotNull(result);
        assertEquals(newStatus, result.getStatus());
        assertNotNull(result.getUpdated_at());
        verify(paymentRepository).findById(paymentId);
        verify(paymentRepository).save(any(Payment.class));
    }

    @Test
    void testUpdatePaymentStatus_Approved_SetsCapturedAt() {
        // Given
        Long paymentId = 1L;
        PaymentStatus newStatus = PaymentStatus.APPROVED;
        when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(testPayment));
        when(paymentRepository.save(any(Payment.class))).thenReturn(testPayment);

        // When
        Payment result = paymentService.updatePaymentStatus(paymentId, newStatus);

        // Then
        assertNotNull(result);
        assertEquals(newStatus, result.getStatus());
        assertNotNull(result.getCaptured_at());
        verify(paymentRepository).findById(paymentId);
        verify(paymentRepository).save(any(Payment.class));
    }

    @Test
    void testUpdatePaymentStatus_NotFound() {
        // Given
        Long paymentId = 999L;
        PaymentStatus newStatus = PaymentStatus.APPROVED;
        when(paymentRepository.findById(paymentId)).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            paymentService.updatePaymentStatus(paymentId, newStatus);
        });
        assertEquals("Pago no fue encontrado con id: 999", exception.getMessage());
    }

    @Test
    void testExistsById_Success() {
        // Given
        Long paymentId = 1L;
        when(paymentRepository.existsById(paymentId)).thenReturn(true);

        // When
        boolean result = paymentService.existsById(paymentId);

        // Then
        assertTrue(result);
        verify(paymentRepository).existsById(paymentId);
    }

    @Test
    void testGetTotalAmountByUserId_Success() {
        // Given
        Long userId = 100L;
        BigDecimal totalAmount = BigDecimal.valueOf(500.00);
        when(paymentRepository.getTotalAmountByUserId(userId)).thenReturn(totalAmount);

        // When
        BigDecimal result = paymentService.getTotalAmountByUserId(userId);

        // Then
        assertNotNull(result);
        assertEquals(totalAmount, result);
        verify(paymentRepository).getTotalAmountByUserId(userId);
    }

    @Test
    void testGetTotalAmountByProviderId_Success() {
        // Given
        Long providerId = 200L;
        BigDecimal totalAmount = BigDecimal.valueOf(1000.00);
        when(paymentRepository.getTotalAmountByProviderId(providerId)).thenReturn(totalAmount);

        // When
        BigDecimal result = paymentService.getTotalAmountByProviderId(providerId);

        // Then
        assertNotNull(result);
        assertEquals(totalAmount, result);
        verify(paymentRepository).getTotalAmountByProviderId(providerId);
    }

    @Test
    void testFindByUserNameContaining_Success() {
        // Given
        String userName = "test";
        List<Payment> payments = Arrays.asList(testPayment);
        when(paymentRepository.findByUserNameContaining(userName)).thenReturn(payments);

        // When
        List<Payment> result = paymentService.findByUserNameContaining(userName);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testPayment.getId(), result.get(0).getId());
        verify(paymentRepository).findByUserNameContaining(userName);
    }

    @Test
    void testFindByUserNameContaining_WithPageable_Success() {
        // Given
        String userName = "test";
        Pageable pageable = PageRequest.of(0, 10);
        Page<Payment> paymentPage = new PageImpl<>(Arrays.asList(testPayment));
        when(paymentRepository.findByUserNameContaining(userName, pageable)).thenReturn(paymentPage);

        // When
        Page<Payment> result = paymentService.findByUserNameContaining(userName, pageable);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals(testPayment.getId(), result.getContent().get(0).getId());
        verify(paymentRepository).findByUserNameContaining(userName, pageable);
    }

    @Test
    void testConfirmPayment_Success() {
        // Given
        Long paymentId = 1L;
        String paymentMethodType = "credit_card";
        String paymentMethodId = "1";
        boolean captureImmediately = true;
        when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(testPayment));
        when(paymentRepository.save(any(Payment.class))).thenReturn(testPayment);
        when(paymentAttemptService.createAttempt(anyLong(), any(), anyString(), anyString(), anyString(), any())).thenReturn(null);

        // When
        Payment result = paymentService.confirmPayment(paymentId, paymentMethodType, paymentMethodId, captureImmediately);

        // Then
        assertNotNull(result);
        verify(paymentRepository).findById(paymentId);
        verify(paymentEventService, atLeastOnce()).createEvent(anyLong(), any(), anyString(), anyString());
        verify(paymentRepository).save(any(Payment.class));
    }

    @Test
    void testConfirmPayment_NotFound() {
        // Given
        Long paymentId = 999L;
        String paymentMethodType = "credit_card";
        String paymentMethodId = "1";
        boolean captureImmediately = true;
        when(paymentRepository.findById(paymentId)).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            paymentService.confirmPayment(paymentId, paymentMethodType, paymentMethodId, captureImmediately);
        });
        assertEquals("Payment not found with id: 999", exception.getMessage());
    }

    @Test
    void testConfirmPayment_Expired() {
        // Given
        Long paymentId = 1L;
        String paymentMethodType = "credit_card";
        String paymentMethodId = "1";
        boolean captureImmediately = true;
        testPayment.setExpired_at(LocalDateTime.now().minusHours(1));
        when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(testPayment));

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            paymentService.confirmPayment(paymentId, paymentMethodType, paymentMethodId, captureImmediately);
        });
        assertEquals("Payment has expired", exception.getMessage());
    }

    @Test
    void testConfirmPayment_WrongStatus() {
        // Given
        Long paymentId = 1L;
        String paymentMethodType = "credit_card";
        String paymentMethodId = "1";
        boolean captureImmediately = true;
        testPayment.setStatus(PaymentStatus.APPROVED);
        when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(testPayment));

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            paymentService.confirmPayment(paymentId, paymentMethodType, paymentMethodId, captureImmediately);
        });
        assertEquals("Payment is not in pending_payment status", exception.getMessage());
    }

    @Test
    void testCancelPayment_Success() {
        // Given
        Long paymentId = 1L;
        String reason = "User cancelled";
        when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(testPayment));
        when(paymentRepository.save(any(Payment.class))).thenReturn(testPayment);

        // When
        Payment result = paymentService.cancelPayment(paymentId, reason);

        // Then
        assertNotNull(result);
        assertEquals(PaymentStatus.CANCELLED, result.getStatus());
        verify(paymentRepository).findById(paymentId);
        verify(paymentEventService).createEvent(anyLong(), any(), anyString(), anyString());
        verify(paymentRepository).save(any(Payment.class));
    }

    @Test
    void testCancelPayment_NotFound() {
        // Given
        Long paymentId = 999L;
        String reason = "User cancelled";
        when(paymentRepository.findById(paymentId)).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            paymentService.cancelPayment(paymentId, reason);
        });
        assertEquals("Payment not found with id: 999", exception.getMessage());
    }

    @Test
    void testExpirePayment_Success() {
        // Given
        Long paymentId = 1L;
        when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(testPayment));
        when(paymentRepository.save(any(Payment.class))).thenReturn(testPayment);

        // When
        Payment result = paymentService.expirePayment(paymentId);

        // Then
        assertNotNull(result);
        assertEquals(PaymentStatus.EXPIRED, result.getStatus());
        verify(paymentRepository).findById(paymentId);
        verify(paymentEventService).createEvent(anyLong(), any(), anyString(), anyString());
        verify(paymentRepository).save(any(Payment.class));
    }

    @Test
    void testExpirePayment_NotFound() {
        // Given
        Long paymentId = 999L;
        when(paymentRepository.findById(paymentId)).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            paymentService.expirePayment(paymentId);
        });
        assertEquals("Payment not found with id: 999", exception.getMessage());
    }

    @Test
    void testIsPaymentExpired_Expired() {
        // Given
        testPayment.setExpired_at(LocalDateTime.now().minusHours(1));

        // When
        boolean result = paymentService.isPaymentExpired(testPayment);

        // Then
        assertTrue(result);
    }

    @Test
    void testIsPaymentExpired_NotExpired() {
        // Given
        testPayment.setExpired_at(LocalDateTime.now().plusHours(1));

        // When
        boolean result = paymentService.isPaymentExpired(testPayment);

        // Then
        assertFalse(result);
    }

    @Test
    void testIsPaymentExpired_NoExpirationDate() {
        // Given
        testPayment.setExpired_at(null);

        // When
        boolean result = paymentService.isPaymentExpired(testPayment);

        // Then
        assertFalse(result);
    }

    @Test
    void testProcessPaymentWithRetry_Success() {
        // Given
        Long paymentId = 1L;
        int maxAttempts = 3;
        when(paymentAttemptService.hasExceededMaxAttempts(paymentId, maxAttempts)).thenReturn(false);
        when(paymentAttemptService.getAttemptCount(paymentId)).thenReturn(1);
        when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(testPayment));
        when(paymentRepository.save(any(Payment.class))).thenReturn(testPayment);
        when(paymentAttemptService.createAttempt(anyLong(), any(), anyString(), anyString(), anyString(), any())).thenReturn(null);

        // When
        Payment result = paymentService.processPaymentWithRetry(paymentId, maxAttempts);

        // Then
        assertNotNull(result);
        verify(paymentAttemptService).hasExceededMaxAttempts(paymentId, maxAttempts);
        verify(paymentEventService, times(3)).createEvent(anyLong(), any(), anyString(), anyString());
    }

    @Test
    void testProcessPaymentWithRetry_MaxAttemptsExceeded() {
        // Given
        Long paymentId = 1L;
        int maxAttempts = 3;
        when(paymentAttemptService.hasExceededMaxAttempts(paymentId, maxAttempts)).thenReturn(true);

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            paymentService.processPaymentWithRetry(paymentId, maxAttempts);
        });
        assertEquals("Maximum retry attempts exceeded for payment: 1", exception.getMessage());
    }

    @Test
    void testUpdatePaymentMethod_Success() {
        // Given
        Long paymentId = 1L;
        when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(testPayment));
        when(paymentRepository.save(any(Payment.class))).thenReturn(testPayment);

        // When
        Payment result = paymentService.updatePaymentMethod(paymentId, testPaymentMethod);

        // Then
        assertNotNull(result);
        verify(paymentRepository).findById(paymentId);
        verify(paymentEventService).createEvent(anyLong(), any(), anyString(), anyString());
        verify(paymentRepository).save(any(Payment.class));
    }

    @Test
    void testUpdatePaymentMethod_NotFound() {
        // Given
        Long paymentId = 999L;
        when(paymentRepository.findById(paymentId)).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            paymentService.updatePaymentMethod(paymentId, testPaymentMethod);
        });
        assertEquals("Payment not found with id: 999", exception.getMessage());
    }

    @Test
    void testUpdatePaymentMethod_WrongStatus() {
        // Given
        Long paymentId = 1L;
        testPayment.setStatus(PaymentStatus.APPROVED);
        when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(testPayment));

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            paymentService.updatePaymentMethod(paymentId, testPaymentMethod);
        });
        assertEquals("Cannot update payment method. Payment status must be PENDING_PAYMENT", exception.getMessage());
    }
}
