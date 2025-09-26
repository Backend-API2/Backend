package backend_api.Backend.Service.Implementation;

import backend_api.Backend.DTO.refund.CreateRefundRequest;
import backend_api.Backend.Entity.payment.Payment;
import backend_api.Backend.Entity.payment.PaymentStatus;
import backend_api.Backend.Entity.refund.Refund;
import backend_api.Backend.Entity.refund.RefundStatus;
import backend_api.Backend.Repository.PaymentRepository;
import backend_api.Backend.Repository.RefundRepository;
import backend_api.Backend.Repository.UserRepository;
import backend_api.Backend.Service.Interface.BalanceService;
import backend_api.Backend.Service.Interface.PaymentEventService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RefundServiceImplTest {

    @Mock
    private RefundRepository refundRepository;

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private PaymentEventService paymentEventService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private BalanceService balanceService;

    @InjectMocks
    private RefundServiceImpl refundService;

    private Payment testPayment;
    private Refund testRefund;
    private CreateRefundRequest createRefundRequest;

    @BeforeEach
    void setUp() {
        testPayment = new Payment();
        testPayment.setId(1L);
        testPayment.setUser_id(100L);
        testPayment.setProvider_id(200L);
        testPayment.setAmount_total(BigDecimal.valueOf(100.00));
        testPayment.setStatus(PaymentStatus.COMPLETED);

        testRefund = new Refund();
        testRefund.setId(1L);
        testRefund.setPayment_id(1L);
        testRefund.setAmount(BigDecimal.valueOf(50.00));
        testRefund.setReason("Test refund");
        testRefund.setStatus(RefundStatus.PENDING);
        testRefund.setRequestedBy(100L);

        createRefundRequest = new CreateRefundRequest();
        createRefundRequest.setPaymentId(1L);
        createRefundRequest.setAmount(BigDecimal.valueOf(50.00));
        createRefundRequest.setReason("Test refund");
    }

    @Test
    void testCreateRefund_Success() {
        // Given
        Long requesterUserId = 100L;
        when(paymentRepository.findById(1L)).thenReturn(Optional.of(testPayment));
        when(refundRepository.existsActiveRefundForPayment(1L)).thenReturn(false);
        when(refundRepository.sumAmountByPaymentIdAndStatuses(eq(1L), anyList())).thenReturn(BigDecimal.ZERO);
        when(refundRepository.save(any(Refund.class))).thenReturn(testRefund);

        // When
        Refund result = refundService.createRefund(createRefundRequest, requesterUserId);

        // Then
        assertNotNull(result);
        assertEquals(testRefund.getId(), result.getId());
        assertEquals(RefundStatus.PENDING, result.getStatus());
        verify(paymentRepository).findById(1L);
        verify(refundRepository).existsActiveRefundForPayment(1L);
        verify(refundRepository).save(any(Refund.class));
        verify(paymentEventService).createEvent(eq(1L), any(), anyString(), anyString());
    }

    @Test
    void testCreateRefund_PaymentNotFound() {
        // Given
        Long requesterUserId = 100L;
        when(paymentRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
            refundService.createRefund(createRefundRequest, requesterUserId);
        });
        assertEquals("Pago no encontrado: 1", exception.getMessage());
    }

    @Test
    void testCreateRefund_UnauthorizedUser() {
        // Given
        Long requesterUserId = 999L; // Different user
        when(paymentRepository.findById(1L)).thenReturn(Optional.of(testPayment));

        // When & Then
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            refundService.createRefund(createRefundRequest, requesterUserId);
        });
        assertEquals("El pago no pertenece al usuario autenticado.", exception.getMessage());
    }

    @Test
    void testCreateRefund_ActiveRefundExists() {
        // Given
        Long requesterUserId = 100L;
        when(paymentRepository.findById(1L)).thenReturn(Optional.of(testPayment));
        when(refundRepository.existsActiveRefundForPayment(1L)).thenReturn(true);

        // When & Then
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            refundService.createRefund(createRefundRequest, requesterUserId);
        });
        assertEquals("Ya existe un reembolso activo para este pago. Solo se permite un reembolso por pago.", exception.getMessage());
    }

    @Test
    void testCreateRefund_PaymentNotRefundable_Cancelled() {
        // Given
        Long requesterUserId = 100L;
        testPayment.setStatus(PaymentStatus.CANCELLED);
        when(paymentRepository.findById(1L)).thenReturn(Optional.of(testPayment));
        when(refundRepository.existsActiveRefundForPayment(1L)).thenReturn(false);

        // When & Then
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            refundService.createRefund(createRefundRequest, requesterUserId);
        });
        assertTrue(exception.getMessage().contains("El pago no es reembolsable en su estado actual"));
    }

    @Test
    void testCreateRefund_PaymentNotRefundable_Rejected() {
        // Given
        Long requesterUserId = 100L;
        testPayment.setStatus(PaymentStatus.REJECTED);
        when(paymentRepository.findById(1L)).thenReturn(Optional.of(testPayment));
        when(refundRepository.existsActiveRefundForPayment(1L)).thenReturn(false);

        // When & Then
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            refundService.createRefund(createRefundRequest, requesterUserId);
        });
        assertTrue(exception.getMessage().contains("El pago no es reembolsable en su estado actual"));
    }

    @Test
    void testCreateRefund_AmountExceedsAvailable() {
        // Given
        Long requesterUserId = 100L;
        createRefundRequest.setAmount(BigDecimal.valueOf(150.00)); // More than available
        when(paymentRepository.findById(1L)).thenReturn(Optional.of(testPayment));
        when(refundRepository.existsActiveRefundForPayment(1L)).thenReturn(false);
        when(refundRepository.sumAmountByPaymentIdAndStatuses(eq(1L), anyList())).thenReturn(BigDecimal.ZERO);

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            refundService.createRefund(createRefundRequest, requesterUserId);
        });
        assertTrue(exception.getMessage().contains("El monto excede lo disponible para reembolso"));
    }

    @Test
    void testApproveRefund_Success() {
        // Given
        Long refundId = 1L;
        Long merchantUserId = 200L;
        String message = "Approved";
        when(refundRepository.findById(refundId)).thenReturn(Optional.of(testRefund));
        when(paymentRepository.findById(1L)).thenReturn(Optional.of(testPayment));
        when(refundRepository.sumAmountByPaymentIdAndStatuses(eq(1L), anyList())).thenReturn(BigDecimal.ZERO);
        when(refundRepository.save(any(Refund.class))).thenReturn(testRefund);
        when(paymentRepository.save(any(Payment.class))).thenReturn(testPayment);

        // When
        Refund result = refundService.approveRefund(refundId, merchantUserId, message);

        // Then
        assertNotNull(result);
        verify(refundRepository, atLeastOnce()).save(any(Refund.class));
        verify(paymentRepository).save(any(Payment.class));
        verify(balanceService).addBalance(100L, BigDecimal.valueOf(50.00));
        verify(paymentEventService).createEvent(eq(1L), any(), anyString(), anyString());
    }

    @Test
    void testApproveRefund_RefundNotFound() {
        // Given
        Long refundId = 999L;
        Long merchantUserId = 200L;
        String message = "Approved";
        when(refundRepository.findById(refundId)).thenReturn(Optional.empty());

        // When & Then
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
            refundService.approveRefund(refundId, merchantUserId, message);
        });
        assertEquals("Refund no encontrado: 999", exception.getMessage());
    }

    @Test
    void testApproveRefund_UnauthorizedMerchant() {
        // Given
        Long refundId = 1L;
        Long merchantUserId = 999L; // Different merchant
        String message = "Approved";
        when(refundRepository.findById(refundId)).thenReturn(Optional.of(testRefund));
        when(paymentRepository.findById(1L)).thenReturn(Optional.of(testPayment));

        // When & Then
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            refundService.approveRefund(refundId, merchantUserId, message);
        });
        assertEquals("No tienes permisos para aprobar este refund.", exception.getMessage());
    }

    @Test
    void testDeclineRefund_Success() {
        // Given
        Long refundId = 1L;
        Long merchantUserId = 200L;
        String message = "Declined";
        when(refundRepository.findById(refundId)).thenReturn(Optional.of(testRefund));
        when(paymentRepository.findById(1L)).thenReturn(Optional.of(testPayment));
        when(refundRepository.save(any(Refund.class))).thenReturn(testRefund);

        // When
        Refund result = refundService.declineRefund(refundId, merchantUserId, message);

        // Then
        assertNotNull(result);
        verify(refundRepository).save(any(Refund.class));
        verify(paymentEventService).createEvent(eq(1L), any(), anyString(), anyString());
    }

    @Test
    void testDeclineRefund_UnauthorizedMerchant() {
        // Given
        Long refundId = 1L;
        Long merchantUserId = 999L; // Different merchant
        String message = "Declined";
        when(refundRepository.findById(refundId)).thenReturn(Optional.of(testRefund));
        when(paymentRepository.findById(1L)).thenReturn(Optional.of(testPayment));

        // When & Then
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            refundService.declineRefund(refundId, merchantUserId, message);
        });
        assertEquals("No tienes permisos para rechazar este refund.", exception.getMessage());
    }

    @Test
    void testGetRefundById_Success() {
        // Given
        Long refundId = 1L;
        when(refundRepository.findById(refundId)).thenReturn(Optional.of(testRefund));

        // When
        Optional<Refund> result = refundService.getRefundById(refundId);

        // Then
        assertTrue(result.isPresent());
        assertEquals(testRefund.getId(), result.get().getId());
        verify(refundRepository).findById(refundId);
    }

    @Test
    void testGetRefundById_NotFound() {
        // Given
        Long refundId = 999L;
        when(refundRepository.findById(refundId)).thenReturn(Optional.empty());

        // When
        Optional<Refund> result = refundService.getRefundById(refundId);

        // Then
        assertFalse(result.isPresent());
        verify(refundRepository).findById(refundId);
    }

    @Test
    void testGetAllRefunds_Success() {
        // Given
        List<Refund> refunds = Arrays.asList(testRefund);
        when(refundRepository.findAll()).thenReturn(refunds);

        // When
        List<Refund> result = refundService.getAllRefunds();

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testRefund.getId(), result.get(0).getId());
        verify(refundRepository).findAll();
    }

    @Test
    void testUpdateRefundStatus_Success() {
        // Given
        Long refundId = 1L;
        RefundStatus newStatus = RefundStatus.APPROVED;
        when(refundRepository.findById(refundId)).thenReturn(Optional.of(testRefund));
        when(refundRepository.save(any(Refund.class))).thenReturn(testRefund);

        // When
        Refund result = refundService.updateRefundStatus(refundId, newStatus);

        // Then
        assertNotNull(result);
        verify(refundRepository).findById(refundId);
        verify(refundRepository).save(any(Refund.class));
    }

    @Test
    void testUpdateRefundStatus_NotFound() {
        // Given
        Long refundId = 999L;
        RefundStatus newStatus = RefundStatus.APPROVED;
        when(refundRepository.findById(refundId)).thenReturn(Optional.empty());

        // When & Then
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
            refundService.updateRefundStatus(refundId, newStatus);
        });
        assertEquals("Refund no encontrado: 999", exception.getMessage());
    }

    @Test
    void testGetRefundsByPaymentId_Success() {
        // Given
        Long paymentId = 1L;
        List<Refund> refunds = Arrays.asList(testRefund);
        when(refundRepository.findByPayment_id(paymentId)).thenReturn(refunds);

        // When
        List<Refund> result = refundService.getRefundsByPaymentId(paymentId);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testRefund.getId(), result.get(0).getId());
        verify(refundRepository).findByPayment_id(paymentId);
    }

    @Test
    void testGetRefundsByStatus_Success() {
        // Given
        RefundStatus status = RefundStatus.PENDING;
        List<Refund> refunds = Arrays.asList(testRefund);
        when(refundRepository.findByStatus(status)).thenReturn(refunds);

        // When
        List<Refund> result = refundService.getRefundsByStatus(status);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testRefund.getId(), result.get(0).getId());
        verify(refundRepository).findByStatus(status);
    }

    @Test
    void testCreateRefund_PaymentNotRefundable_Pending() {
        // Given
        Long requesterUserId = 100L;
        testPayment.setStatus(PaymentStatus.PENDING_PAYMENT);
        when(paymentRepository.findById(1L)).thenReturn(Optional.of(testPayment));
        when(refundRepository.existsActiveRefundForPayment(1L)).thenReturn(false);

        // When & Then
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            refundService.createRefund(createRefundRequest, requesterUserId);
        });
        assertTrue(exception.getMessage().contains("El pago no es reembolsable en su estado actual"));
    }

    @Test
    void testCreateRefund_PaymentNotRefundable_Expired() {
        // Given
        Long requesterUserId = 100L;
        testPayment.setStatus(PaymentStatus.EXPIRED);
        when(paymentRepository.findById(1L)).thenReturn(Optional.of(testPayment));
        when(refundRepository.existsActiveRefundForPayment(1L)).thenReturn(false);

        // When & Then
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            refundService.createRefund(createRefundRequest, requesterUserId);
        });
        assertTrue(exception.getMessage().contains("El pago no es reembolsable en su estado actual"));
    }

    @Test
    void testCreateRefund_PaymentNotRefundable_Failed() {
        // Given
        Long requesterUserId = 100L;
        testPayment.setStatus(PaymentStatus.REJECTED);
        when(paymentRepository.findById(1L)).thenReturn(Optional.of(testPayment));
        when(refundRepository.existsActiveRefundForPayment(1L)).thenReturn(false);

        // When & Then
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            refundService.createRefund(createRefundRequest, requesterUserId);
        });
        assertTrue(exception.getMessage().contains("El pago no es reembolsable en su estado actual"));
    }

    @Test
    void testCreateRefund_AmountExceedsAvailableWithExistingRefunds() {
        // Given
        Long requesterUserId = 100L;
        createRefundRequest.setAmount(BigDecimal.valueOf(60.00)); // More than available after existing refunds
        when(paymentRepository.findById(1L)).thenReturn(Optional.of(testPayment));
        when(refundRepository.existsActiveRefundForPayment(1L)).thenReturn(false);
        when(refundRepository.sumAmountByPaymentIdAndStatuses(eq(1L), anyList())).thenReturn(BigDecimal.valueOf(50.00)); // Existing refunds

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            refundService.createRefund(createRefundRequest, requesterUserId);
        });
        assertTrue(exception.getMessage().contains("El monto excede lo disponible para reembolso"));
    }

    @Test
    void testApproveRefund_RefundNotPending() {
        // Given
        Long refundId = 1L;
        Long merchantUserId = 200L;
        String message = "Approved";
        testRefund.setStatus(RefundStatus.APPROVED); // Already approved
        when(refundRepository.findById(refundId)).thenReturn(Optional.of(testRefund));

        // When & Then
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
            refundService.approveRefund(refundId, merchantUserId, message);
        });
        assertEquals("Pago no encontrado para el refund: 1", exception.getMessage());
    }

    @Test
    void testApproveRefund_RefundDeclined() {
        // Given
        Long refundId = 1L;
        Long merchantUserId = 200L;
        String message = "Approved";
        testRefund.setStatus(RefundStatus.DECLINED); // Already declined
        when(refundRepository.findById(refundId)).thenReturn(Optional.of(testRefund));

        // When & Then
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
            refundService.approveRefund(refundId, merchantUserId, message);
        });
        assertEquals("Pago no encontrado para el refund: 1", exception.getMessage());
    }

    @Test
    void testApproveRefund_PaymentNotFound() {
        // Given
        Long refundId = 1L;
        Long merchantUserId = 200L;
        String message = "Approved";
        when(refundRepository.findById(refundId)).thenReturn(Optional.of(testRefund));
        when(paymentRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
            refundService.approveRefund(refundId, merchantUserId, message);
        });
        assertEquals("Pago no encontrado para el refund: 1", exception.getMessage());
    }

    @Test
    void testApproveRefund_AmountExceedsAvailable() {
        // Given
        Long refundId = 1L;
        Long merchantUserId = 200L;
        String message = "Approved";
        testRefund.setAmount(BigDecimal.valueOf(150.00)); // More than payment amount
        when(refundRepository.findById(refundId)).thenReturn(Optional.of(testRefund));
        when(paymentRepository.findById(1L)).thenReturn(Optional.of(testPayment));
        when(refundRepository.sumAmountByPaymentIdAndStatuses(eq(1L), anyList())).thenReturn(BigDecimal.ZERO);

        // When
        Refund result = refundService.approveRefund(refundId, merchantUserId, message);

        // Then
        assertNotNull(result);
        verify(refundRepository, times(2)).save(testRefund);
    }

    @Test
    void testDeclineRefund_RefundNotFound() {
        // Given
        Long refundId = 999L;
        Long merchantUserId = 200L;
        String message = "Declined";
        when(refundRepository.findById(refundId)).thenReturn(Optional.empty());

        // When & Then
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
            refundService.declineRefund(refundId, merchantUserId, message);
        });
        assertEquals("Refund no encontrado: 999", exception.getMessage());
    }

    @Test
    void testDeclineRefund_RefundNotPending() {
        // Given
        Long refundId = 1L;
        Long merchantUserId = 200L;
        String message = "Declined";
        testRefund.setStatus(RefundStatus.APPROVED); // Already approved
        when(refundRepository.findById(refundId)).thenReturn(Optional.of(testRefund));

        // When & Then
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
            refundService.declineRefund(refundId, merchantUserId, message);
        });
        assertEquals("Pago no encontrado para el refund: 1", exception.getMessage());
    }

    @Test
    void testDeclineRefund_RefundDeclined() {
        // Given
        Long refundId = 1L;
        Long merchantUserId = 200L;
        String message = "Declined";
        testRefund.setStatus(RefundStatus.DECLINED); // Already declined
        when(refundRepository.findById(refundId)).thenReturn(Optional.of(testRefund));

        // When & Then
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
            refundService.declineRefund(refundId, merchantUserId, message);
        });
        assertEquals("Pago no encontrado para el refund: 1", exception.getMessage());
    }

    @Test
    void testDeclineRefund_PaymentNotFound() {
        // Given
        Long refundId = 1L;
        Long merchantUserId = 200L;
        String message = "Declined";
        when(refundRepository.findById(refundId)).thenReturn(Optional.of(testRefund));
        when(paymentRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
            refundService.declineRefund(refundId, merchantUserId, message);
        });
        assertEquals("Pago no encontrado para el refund: 1", exception.getMessage());
    }

    @Test
    void testGetAllRefunds_EmptyResult() {
        // Given
        when(refundRepository.findAll()).thenReturn(Arrays.asList());

        // When
        List<Refund> result = refundService.getAllRefunds();

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(refundRepository).findAll();
    }

    @Test
    void testGetRefundsByPaymentId_EmptyResult() {
        // Given
        Long paymentId = 999L;
        when(refundRepository.findByPayment_id(paymentId)).thenReturn(Arrays.asList());

        // When
        List<Refund> result = refundService.getRefundsByPaymentId(paymentId);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(refundRepository).findByPayment_id(paymentId);
    }

    @Test
    void testGetRefundsByStatus_EmptyResult() {
        // Given
        RefundStatus status = RefundStatus.APPROVED;
        when(refundRepository.findByStatus(status)).thenReturn(Arrays.asList());

        // When
        List<Refund> result = refundService.getRefundsByStatus(status);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(refundRepository).findByStatus(status);
    }

    @Test
    void testCreateRefund_WithExistingRefunds() {
        // Given
        Long requesterUserId = 100L;
        createRefundRequest.setAmount(BigDecimal.valueOf(30.00)); // Less than available
        when(paymentRepository.findById(1L)).thenReturn(Optional.of(testPayment));
        when(refundRepository.existsActiveRefundForPayment(1L)).thenReturn(false);
        when(refundRepository.sumAmountByPaymentIdAndStatuses(eq(1L), anyList())).thenReturn(BigDecimal.valueOf(20.00)); // Existing refunds
        when(refundRepository.save(any(Refund.class))).thenReturn(testRefund);

        // When
        Refund result = refundService.createRefund(createRefundRequest, requesterUserId);

        // Then
        assertNotNull(result);
        assertEquals(testRefund.getId(), result.getId());
        assertEquals(RefundStatus.PENDING, result.getStatus());
        verify(paymentRepository).findById(1L);
        verify(refundRepository).existsActiveRefundForPayment(1L);
        verify(refundRepository).save(any(Refund.class));
        verify(paymentEventService).createEvent(eq(1L), any(), anyString(), anyString());
    }

    @Test
    void testApproveRefund_WithExistingRefunds() {
        // Given
        Long refundId = 1L;
        Long merchantUserId = 200L;
        String message = "Approved";
        testRefund.setAmount(BigDecimal.valueOf(30.00)); // Less than available
        when(refundRepository.findById(refundId)).thenReturn(Optional.of(testRefund));
        when(paymentRepository.findById(1L)).thenReturn(Optional.of(testPayment));
        when(refundRepository.sumAmountByPaymentIdAndStatuses(eq(1L), anyList())).thenReturn(BigDecimal.valueOf(20.00)); // Existing refunds
        when(refundRepository.save(any(Refund.class))).thenReturn(testRefund);
        when(paymentRepository.save(any(Payment.class))).thenReturn(testPayment);

        // When
        Refund result = refundService.approveRefund(refundId, merchantUserId, message);

        // Then
        assertNotNull(result);
        verify(refundRepository, atLeastOnce()).save(any(Refund.class));
        verify(paymentRepository).save(any(Payment.class));
        verify(balanceService).addBalance(100L, BigDecimal.valueOf(30.00));
        verify(paymentEventService).createEvent(eq(1L), any(), anyString(), anyString());
    }

    @Test
    void testCreateRefund_ExactAmount() {
        // Given
        Long requesterUserId = 100L;
        createRefundRequest.setAmount(BigDecimal.valueOf(100.00)); // Exact payment amount
        when(paymentRepository.findById(1L)).thenReturn(Optional.of(testPayment));
        when(refundRepository.existsActiveRefundForPayment(1L)).thenReturn(false);
        when(refundRepository.sumAmountByPaymentIdAndStatuses(eq(1L), anyList())).thenReturn(BigDecimal.ZERO);
        when(refundRepository.save(any(Refund.class))).thenReturn(testRefund);

        // When
        Refund result = refundService.createRefund(createRefundRequest, requesterUserId);

        // Then
        assertNotNull(result);
        assertEquals(testRefund.getId(), result.getId());
        assertEquals(RefundStatus.PENDING, result.getStatus());
        verify(paymentRepository).findById(1L);
        verify(refundRepository).existsActiveRefundForPayment(1L);
        verify(refundRepository).save(any(Refund.class));
        verify(paymentEventService).createEvent(eq(1L), any(), anyString(), anyString());
    }

    @Test
    void testApproveRefund_ExactAmount() {
        // Given
        Long refundId = 1L;
        Long merchantUserId = 200L;
        String message = "Approved";
        testRefund.setAmount(BigDecimal.valueOf(100.00)); // Exact payment amount
        when(refundRepository.findById(refundId)).thenReturn(Optional.of(testRefund));
        when(paymentRepository.findById(1L)).thenReturn(Optional.of(testPayment));
        when(refundRepository.sumAmountByPaymentIdAndStatuses(eq(1L), anyList())).thenReturn(BigDecimal.ZERO);
        when(refundRepository.save(any(Refund.class))).thenReturn(testRefund);
        when(paymentRepository.save(any(Payment.class))).thenReturn(testPayment);

        // When
        Refund result = refundService.approveRefund(refundId, merchantUserId, message);

        // Then
        assertNotNull(result);
        verify(refundRepository, atLeastOnce()).save(any(Refund.class));
        verify(paymentRepository).save(any(Payment.class));
        verify(balanceService).addBalance(100L, BigDecimal.valueOf(100.00));
        verify(paymentEventService).createEvent(eq(1L), any(), anyString(), anyString());
    }
}
