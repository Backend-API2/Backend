package backend_api.Backend.Service.Implementation;

import backend_api.Backend.Entity.payment.PaymentAttempt;
import backend_api.Backend.Entity.payment.PaymentStatus;
import backend_api.Backend.Repository.PaymentAttemptRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

/**
 * Unit tests for PaymentAttemptServiceImpl
 * Testing payment attempt management operations
 */
@ExtendWith(MockitoExtension.class)
class PaymentAttemptServiceImplTest {

    @Mock
    private PaymentAttemptRepository paymentAttemptRepository;

    @InjectMocks
    private PaymentAttemptServiceImpl paymentAttemptService;

    private PaymentAttempt testAttempt;
    private final Long paymentId = 1L;
    private final Long attemptId = 1L;

    @BeforeEach
    void setUp() {
        testAttempt = new PaymentAttempt();
        testAttempt.setId(attemptId);
        testAttempt.setPaymentId(paymentId);
        testAttempt.setAttemptNumber(1);
        testAttempt.setStatus(PaymentStatus.PENDING_PAYMENT);
        testAttempt.setResponseCode("200");
        testAttempt.setGatewayResponseCode("SUCCESS");
        testAttempt.setGatewayMessage("Success message");
        testAttempt.setFailureReason(null);
        testAttempt.setCreatedAt(LocalDateTime.now());
    }

    @Test
    void testCreateAttempt_BasicOverload() {
        // Given
        when(paymentAttemptRepository.countByPaymentId(paymentId)).thenReturn(0);
        when(paymentAttemptRepository.save(any(PaymentAttempt.class))).thenReturn(testAttempt);

        // When
        PaymentAttempt result = paymentAttemptService.createAttempt(
                paymentId, PaymentStatus.PENDING_PAYMENT, "200", "SUCCESS");

        // Then
        assertNotNull(result);
        assertEquals(paymentId, result.getPaymentId());
        assertEquals(PaymentStatus.PENDING_PAYMENT, result.getStatus());
        assertEquals("200", result.getResponseCode());
        assertEquals("SUCCESS", result.getGatewayResponseCode());
        verify(paymentAttemptRepository).countByPaymentId(paymentId);
        verify(paymentAttemptRepository).save(any(PaymentAttempt.class));
    }

    @Test
    void testCreateAttempt_FullOverload() {
        // Given
        when(paymentAttemptRepository.countByPaymentId(paymentId)).thenReturn(1);
        
        // Create a mock attempt with APPROVED status for this test
        PaymentAttempt approvedAttempt = new PaymentAttempt();
        approvedAttempt.setId(attemptId);
        approvedAttempt.setPaymentId(paymentId);
        approvedAttempt.setAttemptNumber(2);
        approvedAttempt.setStatus(PaymentStatus.APPROVED);
        approvedAttempt.setResponseCode("200");
        approvedAttempt.setGatewayResponseCode("SUCCESS");
        approvedAttempt.setGatewayMessage("Success message");
        approvedAttempt.setFailureReason(null);
        approvedAttempt.setCreatedAt(LocalDateTime.now());
        approvedAttempt.setCompletedAt(LocalDateTime.now());
        
        when(paymentAttemptRepository.save(any(PaymentAttempt.class))).thenReturn(approvedAttempt);

        // When
        PaymentAttempt result = paymentAttemptService.createAttempt(
                paymentId, PaymentStatus.APPROVED, "200", "SUCCESS", "Success message", null);

        // Then
        assertNotNull(result);
        assertEquals(paymentId, result.getPaymentId());
        assertEquals(PaymentStatus.APPROVED, result.getStatus());
        assertEquals("200", result.getResponseCode());
        assertEquals("SUCCESS", result.getGatewayResponseCode());
        assertEquals("Success message", result.getGatewayMessage());
        verify(paymentAttemptRepository).countByPaymentId(paymentId);
        verify(paymentAttemptRepository).save(any(PaymentAttempt.class));
    }

    @Test
    void testCreateAttempt_ApprovedStatus_SetsCompletedAt() {
        // Given
        when(paymentAttemptRepository.countByPaymentId(paymentId)).thenReturn(0);
        when(paymentAttemptRepository.save(any(PaymentAttempt.class))).thenAnswer(invocation -> {
            PaymentAttempt savedAttempt = invocation.getArgument(0);
            savedAttempt.setId(attemptId);
            return savedAttempt;
        });

        // When
        PaymentAttempt result = paymentAttemptService.createAttempt(
                paymentId, PaymentStatus.APPROVED, "200", "SUCCESS", "Success", null);

        // Then
        assertNotNull(result);
        assertEquals(PaymentStatus.APPROVED, result.getStatus());
        assertNotNull(result.getCompletedAt());
        verify(paymentAttemptRepository).save(any(PaymentAttempt.class));
    }

    @Test
    void testCreateAttempt_RejectedStatus_SetsCompletedAt() {
        // Given
        when(paymentAttemptRepository.countByPaymentId(paymentId)).thenReturn(0);
        when(paymentAttemptRepository.save(any(PaymentAttempt.class))).thenAnswer(invocation -> {
            PaymentAttempt savedAttempt = invocation.getArgument(0);
            savedAttempt.setId(attemptId);
            return savedAttempt;
        });

        // When
        PaymentAttempt result = paymentAttemptService.createAttempt(
                paymentId, PaymentStatus.REJECTED, "400", "FAILED", "Error", "Insufficient funds");

        // Then
        assertNotNull(result);
        assertEquals(PaymentStatus.REJECTED, result.getStatus());
        assertNotNull(result.getCompletedAt());
        assertEquals("Insufficient funds", result.getFailureReason());
        verify(paymentAttemptRepository).save(any(PaymentAttempt.class));
    }

    @Test
    void testCreateAttempt_PendingStatus_NoCompletedAt() {
        // Given
        when(paymentAttemptRepository.countByPaymentId(paymentId)).thenReturn(0);
        when(paymentAttemptRepository.save(any(PaymentAttempt.class))).thenAnswer(invocation -> {
            PaymentAttempt savedAttempt = invocation.getArgument(0);
            savedAttempt.setId(attemptId);
            return savedAttempt;
        });

        // When
        PaymentAttempt result = paymentAttemptService.createAttempt(
                paymentId, PaymentStatus.PENDING_PAYMENT, "200", "PENDING", null, null);

        // Then
        assertNotNull(result);
        assertEquals(PaymentStatus.PENDING_PAYMENT, result.getStatus());
        assertNull(result.getCompletedAt());
        verify(paymentAttemptRepository).save(any(PaymentAttempt.class));
    }

    @Test
    void testGetAttemptsByPaymentId() {
        // Given
        List<PaymentAttempt> attempts = Arrays.asList(testAttempt);
        when(paymentAttemptRepository.findByPaymentIdOrderByAttemptNumberDesc(paymentId))
                .thenReturn(attempts);

        // When
        List<PaymentAttempt> result = paymentAttemptService.getAttemptsByPaymentId(paymentId);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testAttempt, result.get(0));
        verify(paymentAttemptRepository).findByPaymentIdOrderByAttemptNumberDesc(paymentId);
    }

    @Test
    void testGetLastAttempt_Found() {
        // Given
        when(paymentAttemptRepository.findLastAttemptByPaymentId(paymentId))
                .thenReturn(Optional.of(testAttempt));

        // When
        Optional<PaymentAttempt> result = paymentAttemptService.getLastAttempt(paymentId);

        // Then
        assertTrue(result.isPresent());
        assertEquals(testAttempt, result.get());
        verify(paymentAttemptRepository).findLastAttemptByPaymentId(paymentId);
    }

    @Test
    void testGetLastAttempt_NotFound() {
        // Given
        when(paymentAttemptRepository.findLastAttemptByPaymentId(paymentId))
                .thenReturn(Optional.empty());

        // When
        Optional<PaymentAttempt> result = paymentAttemptService.getLastAttempt(paymentId);

        // Then
        assertFalse(result.isPresent());
        verify(paymentAttemptRepository).findLastAttemptByPaymentId(paymentId);
    }

    @Test
    void testGetAttemptCount_WithAttempts() {
        // Given
        when(paymentAttemptRepository.countByPaymentId(paymentId)).thenReturn(3);

        // When
        Integer result = paymentAttemptService.getAttemptCount(paymentId);

        // Then
        assertEquals(3, result);
        verify(paymentAttemptRepository).countByPaymentId(paymentId);
    }

    @Test
    void testGetAttemptCount_NoAttempts() {
        // Given
        when(paymentAttemptRepository.countByPaymentId(paymentId)).thenReturn(null);

        // When
        Integer result = paymentAttemptService.getAttemptCount(paymentId);

        // Then
        assertEquals(0, result);
        verify(paymentAttemptRepository).countByPaymentId(paymentId);
    }

    @Test
    void testGetSuccessfulAttempt_Found() {
        // Given
        when(paymentAttemptRepository.findSuccessfulAttemptByPaymentId(paymentId))
                .thenReturn(Optional.of(testAttempt));

        // When
        Optional<PaymentAttempt> result = paymentAttemptService.getSuccessfulAttempt(paymentId);

        // Then
        assertTrue(result.isPresent());
        assertEquals(testAttempt, result.get());
        verify(paymentAttemptRepository).findSuccessfulAttemptByPaymentId(paymentId);
    }

    @Test
    void testGetSuccessfulAttempt_NotFound() {
        // Given
        when(paymentAttemptRepository.findSuccessfulAttemptByPaymentId(paymentId))
                .thenReturn(Optional.empty());

        // When
        Optional<PaymentAttempt> result = paymentAttemptService.getSuccessfulAttempt(paymentId);

        // Then
        assertFalse(result.isPresent());
        verify(paymentAttemptRepository).findSuccessfulAttemptByPaymentId(paymentId);
    }

    @Test
    void testHasExceededMaxAttempts_Exceeded() {
        // Given
        when(paymentAttemptRepository.countByPaymentId(paymentId)).thenReturn(5);

        // When
        boolean result = paymentAttemptService.hasExceededMaxAttempts(paymentId, 3);

        // Then
        assertTrue(result);
        verify(paymentAttemptRepository).countByPaymentId(paymentId);
    }

    @Test
    void testHasExceededMaxAttempts_NotExceeded() {
        // Given
        when(paymentAttemptRepository.countByPaymentId(paymentId)).thenReturn(2);

        // When
        boolean result = paymentAttemptService.hasExceededMaxAttempts(paymentId, 3);

        // Then
        assertFalse(result);
        verify(paymentAttemptRepository).countByPaymentId(paymentId);
    }

    @Test
    void testHasExceededMaxAttempts_EqualToMax() {
        // Given
        when(paymentAttemptRepository.countByPaymentId(paymentId)).thenReturn(3);

        // When
        boolean result = paymentAttemptService.hasExceededMaxAttempts(paymentId, 3);

        // Then
        assertTrue(result);
        verify(paymentAttemptRepository).countByPaymentId(paymentId);
    }

    @Test
    void testUpdateAttempt_Success() {
        // Given
        PaymentAttempt updateData = new PaymentAttempt();
        updateData.setStatus(PaymentStatus.APPROVED);
        updateData.setResponseCode("200");
        updateData.setGatewayResponseCode("SUCCESS");
        updateData.setGatewayMessage("Updated message");
        updateData.setFailureReason(null);
        updateData.setGatewayTxnId("TXN123");

        when(paymentAttemptRepository.findById(attemptId)).thenReturn(Optional.of(testAttempt));
        when(paymentAttemptRepository.save(any(PaymentAttempt.class))).thenAnswer(invocation -> {
            PaymentAttempt savedAttempt = invocation.getArgument(0);
            savedAttempt.setCompletedAt(LocalDateTime.now());
            return savedAttempt;
        });

        // When
        PaymentAttempt result = paymentAttemptService.updateAttempt(attemptId, updateData);

        // Then
        assertNotNull(result);
        assertEquals(PaymentStatus.APPROVED, result.getStatus());
        assertEquals("200", result.getResponseCode());
        assertEquals("SUCCESS", result.getGatewayResponseCode());
        assertEquals("Updated message", result.getGatewayMessage());
        assertEquals("TXN123", result.getGatewayTxnId());
        assertNotNull(result.getCompletedAt());
        verify(paymentAttemptRepository).findById(attemptId);
        verify(paymentAttemptRepository).save(any(PaymentAttempt.class));
    }

    @Test
    void testUpdateAttempt_NotFound() {
        // Given
        PaymentAttempt updateData = new PaymentAttempt();
        when(paymentAttemptRepository.findById(anyLong())).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                paymentAttemptService.updateAttempt(attemptId, updateData));
        assertEquals("PaymentAttempt not found with id: " + attemptId, exception.getMessage());
        verify(paymentAttemptRepository).findById(attemptId);
        verify(paymentAttemptRepository, never()).save(any(PaymentAttempt.class));
    }
}
