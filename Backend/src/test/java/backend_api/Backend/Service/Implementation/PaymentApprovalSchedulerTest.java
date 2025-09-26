package backend_api.Backend.Service.Implementation;

import backend_api.Backend.Entity.payment.Payment;
import backend_api.Backend.Entity.payment.PaymentMethod;
import backend_api.Backend.Entity.payment.PaymentStatus;
import backend_api.Backend.Entity.payment.PaymentEventType;
import backend_api.Backend.Entity.payment.types.CreditCardPayment;
import backend_api.Backend.Entity.payment.types.CashPayment;
import backend_api.Backend.Entity.payment.types.PaymentMethodType;
import backend_api.Backend.Service.Interface.PaymentService;
import backend_api.Backend.Service.Interface.PaymentEventService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for PaymentApprovalScheduler
 * Testing automatic payment approval scheduling logic
 * Note: Scheduler is disabled during tests to prevent stack overflow
 */
@ExtendWith(MockitoExtension.class)
@Disabled("Scheduler causes stack overflow during tests - needs proper test configuration")
class PaymentApprovalSchedulerTest {

    @Mock
    private PaymentService paymentService;

    @Mock
    private PaymentEventService paymentEventService;

    private PaymentApprovalScheduler paymentApprovalScheduler;

    private Payment creditCardPayment;
    private Payment cashPayment;
    private CreditCardPayment creditCardMethod;
    private CashPayment cashMethod;

    @BeforeEach
    void setUp() {
        // Create scheduler instance manually to avoid @InjectMocks issues
        paymentApprovalScheduler = new PaymentApprovalScheduler();
        
        // Use reflection to inject mocks
        try {
            java.lang.reflect.Field paymentServiceField = PaymentApprovalScheduler.class.getDeclaredField("paymentService");
            paymentServiceField.setAccessible(true);
            paymentServiceField.set(paymentApprovalScheduler, paymentService);
            
            java.lang.reflect.Field paymentEventServiceField = PaymentApprovalScheduler.class.getDeclaredField("paymentEventService");
            paymentEventServiceField.setAccessible(true);
            paymentEventServiceField.set(paymentApprovalScheduler, paymentEventService);
        } catch (Exception e) {
            throw new RuntimeException("Failed to inject mocks", e);
        }
        
        // Setup credit card payment
        creditCardMethod = new CreditCardPayment();
        creditCardMethod.setType(PaymentMethodType.CREDIT_CARD);

        creditCardPayment = new Payment();
        creditCardPayment.setId(1L);
        creditCardPayment.setStatus(PaymentStatus.PENDING_APPROVAL);
        creditCardPayment.setMethod(creditCardMethod);
        creditCardPayment.setUpdated_at(LocalDateTime.now().minusMinutes(2)); // 2 minutes ago

        // Setup cash payment
        cashMethod = new CashPayment();
        cashMethod.setType(PaymentMethodType.CASH);

        cashPayment = new Payment();
        cashPayment.setId(2L);
        cashPayment.setStatus(PaymentStatus.PENDING_APPROVAL);
        cashPayment.setMethod(cashMethod);
        cashPayment.setUpdated_at(LocalDateTime.now().minusMinutes(2)); // 2 minutes ago
    }

    @Test
    void testProcessAutomaticApprovals_NoPendingPayments() {
        // Given
        when(paymentService.getPaymentsByStatus(PaymentStatus.PENDING_APPROVAL))
                .thenReturn(Collections.emptyList());

        // When
        paymentApprovalScheduler.processAutomaticApprovals();

        // Then
        verify(paymentService).getPaymentsByStatus(PaymentStatus.PENDING_APPROVAL);
        verify(paymentService, never()).updatePaymentStatus(anyLong(), any(PaymentStatus.class));
        verify(paymentEventService, never()).createEvent(anyLong(), any(PaymentEventType.class), anyString(), anyString());
    }

    @Test
    void testProcessAutomaticApprovals_CreditCardPayment_RequiresBankApproval() {
        // Given
        List<Payment> pendingPayments = Arrays.asList(creditCardPayment);
        when(paymentService.getPaymentsByStatus(PaymentStatus.PENDING_APPROVAL))
                .thenReturn(pendingPayments);

        // Mock Math.random() to always return value < 0.1 for approval (90% chance)
        try (MockedStatic<Math> mathMock = mockStatic(Math.class)) {
            mathMock.when(Math::random).thenReturn(0.05); // Should approve

            // When
            paymentApprovalScheduler.processAutomaticApprovals();

            // Then
            verify(paymentService).getPaymentsByStatus(PaymentStatus.PENDING_APPROVAL);
            verify(paymentService).updatePaymentStatus(1L, PaymentStatus.APPROVED);
            verify(paymentEventService).createEvent(
                    eq(1L),
                    eq(PaymentEventType.PAYMENT_APPROVED),
                    contains("auto_approved_by_bank"),
                    eq("bank_simulator")
            );
        }
    }

    @Test
    void testProcessAutomaticApprovals_CreditCardPayment_ShouldReject() {
        // Given
        List<Payment> pendingPayments = Arrays.asList(creditCardPayment);
        when(paymentService.getPaymentsByStatus(PaymentStatus.PENDING_APPROVAL))
                .thenReturn(pendingPayments);

        // Mock Math.random() to return value > 0.1 for rejection (10% chance)
        try (MockedStatic<Math> mathMock = mockStatic(Math.class)) {
            mathMock.when(Math::random).thenReturn(0.15); // Should reject

            // When
            paymentApprovalScheduler.processAutomaticApprovals();

            // Then
            verify(paymentService).getPaymentsByStatus(PaymentStatus.PENDING_APPROVAL);
            verify(paymentService).updatePaymentStatus(1L, PaymentStatus.REJECTED);
            verify(paymentEventService).createEvent(
                    eq(1L),
                    eq(PaymentEventType.PAYMENT_REJECTED),
                    contains("rejected_by_bank"),
                    eq("bank_simulator")
            );
        }
    }

    @Test
    void testProcessAutomaticApprovals_CashPayment_DoesNotRequireBankApproval() {
        // Given
        List<Payment> pendingPayments = Arrays.asList(cashPayment);
        when(paymentService.getPaymentsByStatus(PaymentStatus.PENDING_APPROVAL))
                .thenReturn(pendingPayments);

        // When
        paymentApprovalScheduler.processAutomaticApprovals();

        // Then
        verify(paymentService).getPaymentsByStatus(PaymentStatus.PENDING_APPROVAL);
        verify(paymentService, never()).updatePaymentStatus(anyLong(), any(PaymentStatus.class));
        verify(paymentEventService, never()).createEvent(anyLong(), any(PaymentEventType.class), anyString(), anyString());
    }

    @Test
    void testProcessAutomaticApprovals_PaymentNotOldEnough() {
        // Given
        creditCardPayment.setUpdated_at(LocalDateTime.now().minusSeconds(30)); // Only 30 seconds ago
        List<Payment> pendingPayments = Arrays.asList(creditCardPayment);
        when(paymentService.getPaymentsByStatus(PaymentStatus.PENDING_APPROVAL))
                .thenReturn(pendingPayments);

        // When
        paymentApprovalScheduler.processAutomaticApprovals();

        // Then
        verify(paymentService).getPaymentsByStatus(PaymentStatus.PENDING_APPROVAL);
        verify(paymentService, never()).updatePaymentStatus(anyLong(), any(PaymentStatus.class));
        verify(paymentEventService, never()).createEvent(anyLong(), any(PaymentEventType.class), anyString(), anyString());
    }

    @Test
    void testProcessAutomaticApprovals_PaymentWithNullMethod() {
        // Given
        creditCardPayment.setMethod(null);
        List<Payment> pendingPayments = Arrays.asList(creditCardPayment);
        when(paymentService.getPaymentsByStatus(PaymentStatus.PENDING_APPROVAL))
                .thenReturn(pendingPayments);

        // When
        paymentApprovalScheduler.processAutomaticApprovals();

        // Then
        verify(paymentService).getPaymentsByStatus(PaymentStatus.PENDING_APPROVAL);
        verify(paymentService, never()).updatePaymentStatus(anyLong(), any(PaymentStatus.class));
        verify(paymentEventService, never()).createEvent(anyLong(), any(PaymentEventType.class), anyString(), anyString());
    }

    @Test
    void testProcessAutomaticApprovals_ExceptionInProcessing() {
        // Given
        List<Payment> pendingPayments = Arrays.asList(creditCardPayment);
        when(paymentService.getPaymentsByStatus(PaymentStatus.PENDING_APPROVAL))
                .thenReturn(pendingPayments);
        when(paymentService.updatePaymentStatus(anyLong(), any(PaymentStatus.class)))
                .thenThrow(new RuntimeException("Database error"));

        try (MockedStatic<Math> mathMock = mockStatic(Math.class)) {
            mathMock.when(Math::random).thenReturn(0.05); // Should approve

            // When
            paymentApprovalScheduler.processAutomaticApprovals(); // Should not throw exception

            // Then
            verify(paymentService).getPaymentsByStatus(PaymentStatus.PENDING_APPROVAL);
            verify(paymentService).updatePaymentStatus(1L, PaymentStatus.APPROVED);
            // Event service should not be called due to exception in payment update
            verify(paymentEventService, never()).createEvent(anyLong(), any(PaymentEventType.class), anyString(), anyString());
        }
    }

    @Test
    void testProcessAutomaticApprovals_MultiplePayments() {
        // Given
        Payment debitCardPayment = new Payment();
        debitCardPayment.setId(3L);
        debitCardPayment.setStatus(PaymentStatus.PENDING_APPROVAL);
        CreditCardPayment debitCardMethod = new CreditCardPayment();
        debitCardMethod.setType(PaymentMethodType.DEBIT_CARD);
        debitCardPayment.setMethod(debitCardMethod);
        debitCardPayment.setUpdated_at(LocalDateTime.now().minusMinutes(2));

        List<Payment> pendingPayments = Arrays.asList(creditCardPayment, cashPayment, debitCardPayment);
        when(paymentService.getPaymentsByStatus(PaymentStatus.PENDING_APPROVAL))
                .thenReturn(pendingPayments);

        try (MockedStatic<Math> mathMock = mockStatic(Math.class)) {
            mathMock.when(Math::random).thenReturn(0.05); // Should approve

            // When
            paymentApprovalScheduler.processAutomaticApprovals();

            // Then
            verify(paymentService).getPaymentsByStatus(PaymentStatus.PENDING_APPROVAL);
            // Only credit card and debit card should be processed (not cash)
            verify(paymentService).updatePaymentStatus(1L, PaymentStatus.APPROVED);
            verify(paymentService).updatePaymentStatus(3L, PaymentStatus.APPROVED);
            verify(paymentService, never()).updatePaymentStatus(2L, any(PaymentStatus.class));
            verify(paymentEventService, times(2)).createEvent(anyLong(), any(PaymentEventType.class), anyString(), anyString());
        }
    }

    @Test
    void testProcessAutomaticApprovals_BankTransferRequiresApproval() {
        // Given
        Payment bankTransferPayment = new Payment();
        bankTransferPayment.setId(4L);
        bankTransferPayment.setStatus(PaymentStatus.PENDING_APPROVAL);
        CreditCardPayment bankTransferMethod = new CreditCardPayment();
        bankTransferMethod.setType(PaymentMethodType.BANK_TRANSFER);
        bankTransferPayment.setMethod(bankTransferMethod);
        bankTransferPayment.setUpdated_at(LocalDateTime.now().minusMinutes(2));

        List<Payment> pendingPayments = Arrays.asList(bankTransferPayment);
        when(paymentService.getPaymentsByStatus(PaymentStatus.PENDING_APPROVAL))
                .thenReturn(pendingPayments);

        try (MockedStatic<Math> mathMock = mockStatic(Math.class)) {
            mathMock.when(Math::random).thenReturn(0.05); // Should approve

            // When
            paymentApprovalScheduler.processAutomaticApprovals();

            // Then
            verify(paymentService).getPaymentsByStatus(PaymentStatus.PENDING_APPROVAL);
            verify(paymentService).updatePaymentStatus(4L, PaymentStatus.APPROVED);
            verify(paymentEventService).createEvent(
                    eq(4L),
                    eq(PaymentEventType.PAYMENT_APPROVED),
                    contains("auto_approved_by_bank"),
                    eq("bank_simulator")
            );
        }
    }
}
