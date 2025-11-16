package backend_api.Backend.Controller;

import backend_api.Backend.Auth.JwtUtil;
import backend_api.Backend.DTO.payment.*;
import backend_api.Backend.Entity.payment.*;
import backend_api.Backend.Entity.payment.types.PaymentMethodType;
import backend_api.Backend.Entity.payment.types.CreditCardPayment;
import backend_api.Backend.Entity.payment.types.CashPayment;
import backend_api.Backend.Entity.user.User;
import backend_api.Backend.Entity.user.UserRole;
import backend_api.Backend.Repository.UserRepository;
import backend_api.Backend.Repository.UserDataRepository;
import backend_api.Backend.Service.Interface.*;
import backend_api.Backend.Service.Common.AuthenticationService;
import backend_api.Backend.Service.Common.ResponseMapperService;
import backend_api.Backend.Service.Common.EntityValidationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import jakarta.persistence.EntityNotFoundException;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for PaymentController
 * 
 * Tests all payment endpoints including:
 * - Payment creation with balance validation
 * - Payment method selection
 * - Payment confirmation with different payment types
 * - Payment cancellation
 * - Payment retrieval and search
 * - Balance operations
 * - Error handling and validation
 */
@ExtendWith(MockitoExtension.class)
class PaymentControllerTest {

    @Mock
    private PaymentService paymentService;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private UserRepository userRepository;
    
    @Mock
    private UserDataRepository userDataRepository;

    @Mock
    private PaymentEventService paymentEventService;

    @Mock
    private PaymentAttemptService paymentAttemptService;

    @Mock
    private PaymentMethodService paymentMethodService;

    @Mock
    private BalanceService balanceService;

    @Mock
    private AuthenticationService authenticationService;

    @Mock
    private ResponseMapperService responseMapperService;

    @Mock
    private EntityValidationService entityValidationService;

    @InjectMocks
    private PaymentController paymentController;

    private User testUser;
    private User merchantUser;
    private Payment testPayment;
    private CreatePaymentRequest createPaymentRequest;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        
        // Setup test user
        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("user@example.com");
        testUser.setName("Test User");
        testUser.setRole(UserRole.USER);
        testUser.setSaldo_disponible(BigDecimal.valueOf(1000.00));

        // Setup merchant user
        merchantUser = new User();
        merchantUser.setId(2L);
        merchantUser.setEmail("merchant@example.com");
        merchantUser.setName("Test Merchant");
        merchantUser.setRole(UserRole.MERCHANT);

        // Setup test payment
        testPayment = new Payment();
        testPayment.setId(1L);
        testPayment.setUser_id(1L);
        testPayment.setProvider_id(2L);
        testPayment.setAmount_subtotal(BigDecimal.valueOf(100.00));
        testPayment.setTaxes(BigDecimal.valueOf(10.00));
        testPayment.setFees(BigDecimal.valueOf(5.00));
        testPayment.setAmount_total(BigDecimal.valueOf(115.00));
        testPayment.setCurrency("USD");
        testPayment.setStatus(PaymentStatus.PENDING_PAYMENT);
        testPayment.setCreated_at(LocalDateTime.now());
        testPayment.setUpdated_at(LocalDateTime.now());

        // Setup create payment request
        createPaymentRequest = new CreatePaymentRequest();
        createPaymentRequest.setAmount_subtotal(BigDecimal.valueOf(100.00));
        createPaymentRequest.setTaxes(BigDecimal.valueOf(10.00));
        createPaymentRequest.setFees(BigDecimal.valueOf(5.00));
        createPaymentRequest.setCurrency("USD");
        createPaymentRequest.setProvider_id(2L);
        createPaymentRequest.setMetadata("Test payment");

        // Setup default mocks (using lenient to avoid unnecessary stubbing errors)
        lenient().when(jwtUtil.getSubject("valid-token")).thenReturn("user@example.com");
        lenient().when(jwtUtil.getSubject("invalid-token")).thenReturn(null);
        lenient().when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(testUser));
        lenient().when(userRepository.findByEmail("merchant@example.com")).thenReturn(Optional.of(merchantUser));
        lenient().when(userRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());
        
        // Setup AuthenticationService mock
        lenient().when(authenticationService.getUserFromToken(anyString())).thenReturn(testUser);
        lenient().when(authenticationService.getUserFromToken("invalid-token")).thenReturn(null);
        lenient().when(authenticationService.getUserFromToken("merchant-token")).thenReturn(merchantUser);
        
        // Setup ResponseMapperService mock
        lenient().when(responseMapperService.mapPaymentsToResponses(anyList(), anyString())).thenReturn(new ArrayList<>());
        lenient().when(responseMapperService.mapPaymentToResponse(any(Payment.class), anyString())).thenReturn(new PaymentResponse());
    }

    // ========== CREATE PAYMENT TESTS ==========

    @Test
    void testCreatePayment_Success() {
        // Given
        String authHeader = "Bearer valid-token";
        when(authenticationService.getUserFromToken(authHeader)).thenReturn(testUser);
        when(balanceService.hasSufficientBalance(1L, BigDecimal.valueOf(115.00))).thenReturn(true);
        when(paymentService.createPayment(any(Payment.class))).thenReturn(testPayment);

        // When
        ResponseEntity<PaymentResponse> response = paymentController.createPayment(createPaymentRequest, authHeader);

        // Then
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(testPayment.getId(), response.getBody().getId());
        assertEquals(testPayment.getAmount_total(), response.getBody().getAmount_total());

        verify(authenticationService).getUserFromToken(authHeader);
        verify(paymentService).createPayment(any(Payment.class));
        verify(paymentEventService).createEvent(eq(testPayment.getId()), eq(PaymentEventType.PAYMENT_PENDING), anyString(), anyString());
    }

    @Test
    void testCreatePayment_InvalidToken() {
        // Given
        String authHeader = "Bearer invalid-token";
        when(authenticationService.getUserFromToken(authHeader)).thenReturn(null);

        // When
        ResponseEntity<PaymentResponse> response = paymentController.createPayment(createPaymentRequest, authHeader);

        // Then
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNull(response.getBody());
        verify(authenticationService).getUserFromToken(authHeader);
        verify(paymentService, never()).createPayment(any(Payment.class));
    }

    @Test
    void testCreatePayment_UserNotFound() {
        // Given
        String authHeader = "Bearer valid-token";
        when(authenticationService.getUserFromToken(authHeader)).thenThrow(new SecurityException("User not found"));

        // When
        ResponseEntity<PaymentResponse> response = paymentController.createPayment(createPaymentRequest, authHeader);

        // Then
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNull(response.getBody());
        verify(authenticationService).getUserFromToken(authHeader);
        verify(paymentService, never()).createPayment(any(Payment.class));
    }

    @Test
    void testCreatePayment_InsufficientBalance() {
        // Given
        String authHeader = "Bearer valid-token";
        when(authenticationService.getUserFromToken(authHeader)).thenReturn(testUser);
        when(balanceService.hasSufficientBalance(1L, BigDecimal.valueOf(115.00))).thenReturn(false);
        when(balanceService.getCurrentBalance(1L)).thenReturn(BigDecimal.valueOf(50.00));

        // When
        ResponseEntity<PaymentResponse> response = paymentController.createPayment(createPaymentRequest, authHeader);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNull(response.getBody());
        assertTrue(response.getHeaders().containsKey("Error-Message"));
        verify(authenticationService).getUserFromToken(authHeader);
        verify(paymentService, never()).createPayment(any(Payment.class));
    }

    @Test
    void testCreatePayment_MerchantUser() {
        // Given
        String authHeader = "Bearer valid-token";
        when(authenticationService.getUserFromToken(authHeader)).thenReturn(merchantUser);
        when(paymentService.createPayment(any(Payment.class))).thenReturn(testPayment);

        // When
        ResponseEntity<PaymentResponse> response = paymentController.createPayment(createPaymentRequest, authHeader);

        // Then
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        verify(authenticationService).getUserFromToken(authHeader);
        verify(balanceService, never()).hasSufficientBalance(anyLong(), any(BigDecimal.class));
        verify(paymentService).createPayment(any(Payment.class));
    }

    @Test
    void testCreatePayment_Exception() {
        // Given
        String authHeader = "Bearer valid-token";
        when(authenticationService.getUserFromToken(authHeader)).thenThrow(new RuntimeException("Authentication error"));

        // When
        ResponseEntity<PaymentResponse> response = paymentController.createPayment(createPaymentRequest, authHeader);

        // Then
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNull(response.getBody());
        verify(authenticationService).getUserFromToken(authHeader);
    }

    // ========== GET PAYMENT TIMELINE TESTS ==========

    @Test
    void testGetPaymentTimeline_Success() {
        // Given
        Long paymentId = 1L;
        String authHeader = "Bearer test-token";
        
        PaymentEvent event1 = new PaymentEvent();
        event1.setId(1L);
        event1.setPaymentId(paymentId);
        event1.setType(PaymentEventType.PAYMENT_PENDING);
        event1.setPayload("{}");
        event1.setActor("system");
        event1.setCreatedAt(LocalDateTime.now());
        
        PaymentEvent event2 = new PaymentEvent();
        event2.setId(2L);
        event2.setPaymentId(paymentId);
        event2.setType(PaymentEventType.PAYMENT_APPROVED);
        event2.setPayload("{}");
        event2.setActor("system");
        event2.setCreatedAt(LocalDateTime.now());
        
        List<PaymentEvent> timeline = Arrays.asList(event1, event2);
        
        when(authenticationService.getUserFromToken(authHeader)).thenReturn(testUser);
        doNothing().when(entityValidationService).validatePaymentOwnership(paymentId, testUser.getId(), testUser.getRole().name());
        when(paymentEventService.getPaymentTimeline(paymentId)).thenReturn(timeline);

        // When
        ResponseEntity<List<PaymentEvent>> response = paymentController.getPaymentTimeline(paymentId, authHeader);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());
        verify(authenticationService).getUserFromToken(authHeader);
        verify(entityValidationService).validatePaymentOwnership(paymentId, testUser.getId(), testUser.getRole().name());
        verify(paymentEventService).getPaymentTimeline(paymentId);
    }

    @Test
    void testGetPaymentTimeline_Exception() {
        // Given
        Long paymentId = 1L;
        String authHeader = "Bearer test-token";
        
        when(authenticationService.getUserFromToken(authHeader)).thenReturn(testUser);
        doNothing().when(entityValidationService).validatePaymentOwnership(paymentId, testUser.getId(), testUser.getRole().name());
        when(paymentEventService.getPaymentTimeline(paymentId)).thenThrow(new RuntimeException("Service error"));

        // When
        ResponseEntity<List<PaymentEvent>> response = paymentController.getPaymentTimeline(paymentId, authHeader);

        // Then
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNull(response.getBody());
        verify(authenticationService).getUserFromToken(authHeader);
        verify(entityValidationService).validatePaymentOwnership(paymentId, testUser.getId(), testUser.getRole().name());
    }

    // ========== SELECT PAYMENT METHOD TESTS ==========

    @Test
    void testSelectPaymentMethod_Success() {
        // Given
        Long paymentId = 1L;
        SelectPaymentMethodRequest request = new SelectPaymentMethodRequest();
        request.setPaymentMethodType("CREDIT_CARD");
        request.setCardNumber("4111111111111111");
        request.setExpirationMonth(12);
        request.setExpirationYear(2025);
        request.setCvv("123");

        CreditCardPayment paymentMethod = new CreditCardPayment();
        paymentMethod.setType(PaymentMethodType.CREDIT_CARD);

        Payment updatedPayment = new Payment();
        updatedPayment.setId(paymentId);
        updatedPayment.setStatus(PaymentStatus.PENDING_PAYMENT);
        updatedPayment.setMethod(paymentMethod);

        when(entityValidationService.getPaymentOrThrow(paymentId)).thenReturn(testPayment);
        when(paymentMethodService.createPaymentMethod(request)).thenReturn(paymentMethod);
        when(paymentService.updatePaymentMethod(paymentId, paymentMethod)).thenReturn(updatedPayment);
        // Mock para el refresh del pago después de actualizar
        when(paymentService.getPaymentById(paymentId)).thenReturn(Optional.of(updatedPayment));
        // Mock para responseMapperService
        PaymentResponse mockResponse = PaymentResponse.fromEntity(updatedPayment);
        when(responseMapperService.mapPaymentToResponse(any(Payment.class), anyString())).thenReturn(mockResponse);

        // When
        ResponseEntity<PaymentResponse> response = paymentController.selectPaymentMethod(paymentId, request);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        verify(paymentMethodService).createPaymentMethod(request);
        verify(paymentService).updatePaymentMethod(paymentId, paymentMethod);
        verify(paymentService).getPaymentById(paymentId);
        verify(responseMapperService).mapPaymentToResponse(any(Payment.class), eq("ADMIN"));
    }

    @Test
    void testSelectPaymentMethod_PaymentNotFound() {
        // Given
        Long paymentId = 1L;
        SelectPaymentMethodRequest request = new SelectPaymentMethodRequest();
        when(entityValidationService.getPaymentOrThrow(paymentId)).thenThrow(new EntityNotFoundException("Payment not found"));

        // When
        ResponseEntity<PaymentResponse> response = paymentController.selectPaymentMethod(paymentId, request);

        // Then
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
        verify(paymentMethodService, never()).createPaymentMethod(any());
    }

    @Test
    void testSelectPaymentMethod_InvalidStatus() {
        // Given
        Long paymentId = 1L;
        SelectPaymentMethodRequest request = new SelectPaymentMethodRequest();
        testPayment.setStatus(PaymentStatus.APPROVED);
        when(entityValidationService.getPaymentOrThrow(paymentId)).thenReturn(testPayment);

        // When
        ResponseEntity<PaymentResponse> response = paymentController.selectPaymentMethod(paymentId, request);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNull(response.getBody());
        verify(paymentMethodService, never()).createPaymentMethod(any());
    }

    // ========== CONFIRM PAYMENT TESTS ==========

    @Test
    void testConfirmPayment_Success_CreditCard() {
        // Given
        Long paymentId = 1L;
        CreditCardPayment paymentMethod = new CreditCardPayment();
        paymentMethod.setType(PaymentMethodType.CREDIT_CARD);
        testPayment.setMethod(paymentMethod);

        Payment updatedPayment = new Payment();
        updatedPayment.setId(paymentId);
        updatedPayment.setStatus(PaymentStatus.PENDING_APPROVAL);

        when(entityValidationService.getPaymentOrThrow(paymentId)).thenReturn(testPayment);
        when(paymentService.updatePaymentStatus(paymentId, PaymentStatus.PENDING_APPROVAL)).thenReturn(updatedPayment);
        // Mock para el refresh del pago después de actualizar
        when(paymentService.getPaymentById(paymentId)).thenReturn(Optional.of(updatedPayment));
        // Mock para responseMapperService
        PaymentResponse mockResponse = PaymentResponse.fromEntity(updatedPayment);
        when(responseMapperService.mapPaymentToResponse(any(Payment.class), anyString())).thenReturn(mockResponse);

        // When
        ResponseEntity<PaymentResponse> response = paymentController.confirmPayment(paymentId);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        verify(paymentService).updatePaymentStatus(paymentId, PaymentStatus.PENDING_APPROVAL);
        verify(paymentService).getPaymentById(paymentId);
        verify(responseMapperService).mapPaymentToResponse(any(Payment.class), eq("ADMIN"));
        verify(paymentEventService).createEvent(eq(paymentId), eq(PaymentEventType.PAYMENT_PENDING), anyString(), eq("system"));
    }

    @Test
    void testConfirmPayment_Success_DirectPayment() {
        // Given
        Long paymentId = 1L;
        CashPayment paymentMethod = new CashPayment();
        paymentMethod.setType(PaymentMethodType.CASH);
        testPayment.setMethod(paymentMethod);

        Payment updatedPayment = new Payment();
        updatedPayment.setId(paymentId);
        updatedPayment.setStatus(PaymentStatus.APPROVED);

        when(entityValidationService.getPaymentOrThrow(paymentId)).thenReturn(testPayment);
        // Mock userDataRepository para que retorne empty (usará fallback a users)
        when(userDataRepository.findByUserId(1L)).thenReturn(Optional.empty());
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(balanceService.deductBalance(1L, BigDecimal.valueOf(115.00))).thenReturn(testUser);
        when(paymentService.updatePaymentStatus(paymentId, PaymentStatus.APPROVED)).thenReturn(updatedPayment);
        // Mock para el refresh del pago después de actualizar
        when(paymentService.getPaymentById(paymentId)).thenReturn(Optional.of(updatedPayment));
        // Mock para responseMapperService
        PaymentResponse mockResponse = PaymentResponse.fromEntity(updatedPayment);
        when(responseMapperService.mapPaymentToResponse(any(Payment.class), anyString())).thenReturn(mockResponse);

        // When
        ResponseEntity<PaymentResponse> response = paymentController.confirmPayment(paymentId);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        verify(userDataRepository).findByUserId(1L);
        verify(userRepository).findById(1L);
        verify(balanceService).deductBalance(1L, BigDecimal.valueOf(115.00));
        verify(paymentService).updatePaymentStatus(paymentId, PaymentStatus.APPROVED);
        verify(paymentService).getPaymentById(paymentId);
        verify(responseMapperService).mapPaymentToResponse(any(Payment.class), eq("ADMIN"));
        verify(paymentEventService).createEvent(eq(paymentId), eq(PaymentEventType.PAYMENT_APPROVED), anyString(), eq("system"));
    }

    @Test
    void testConfirmPayment_InsufficientBalance() {
        // Given
        Long paymentId = 1L;
        CashPayment paymentMethod = new CashPayment();
        paymentMethod.setType(PaymentMethodType.CASH);
        testPayment.setMethod(paymentMethod);

        when(entityValidationService.getPaymentOrThrow(paymentId)).thenReturn(testPayment);
        // Mock userDataRepository para que retorne empty (usará fallback a users)
        when(userDataRepository.findByUserId(1L)).thenReturn(Optional.empty());
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(balanceService.deductBalance(1L, BigDecimal.valueOf(115.00))).thenThrow(new IllegalStateException("Saldo insuficiente"));

        // When
        ResponseEntity<PaymentResponse> response = paymentController.confirmPayment(paymentId);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNull(response.getBody());
        assertTrue(response.getHeaders().containsKey("Error-Message"));
        verify(userDataRepository).findByUserId(1L);
        verify(userRepository).findById(1L);
        verify(balanceService).deductBalance(1L, BigDecimal.valueOf(115.00));
        verify(paymentService).createPayment(any(Payment.class)); // Should update payment status to rejected
        verify(paymentEventService).createEvent(eq(paymentId), eq(PaymentEventType.PAYMENT_REJECTED), anyString(), eq("system"));
    }

    @Test
    void testConfirmPayment_NoPaymentMethod() {
        // Given
        Long paymentId = 1L;
        testPayment.setMethod(null);
        when(entityValidationService.getPaymentOrThrow(paymentId)).thenReturn(testPayment);

        // When
        ResponseEntity<PaymentResponse> response = paymentController.confirmPayment(paymentId);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNull(response.getBody());
        assertTrue(response.getHeaders().containsKey("Error-Message"));
        verify(paymentService, never()).updatePaymentStatus(anyLong(), any());
    }

    @Test
    void testConfirmPayment_InvalidStatus() {
        // Given
        Long paymentId = 1L;
        // Usar un estado realmente inválido (REJECTED) ya que ahora permitimos APPROVED
        testPayment.setStatus(PaymentStatus.REJECTED);
        when(entityValidationService.getPaymentOrThrow(paymentId)).thenReturn(testPayment);

        // When
        ResponseEntity<PaymentResponse> response = paymentController.confirmPayment(paymentId);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNull(response.getBody());
        assertTrue(response.getHeaders().containsKey("Error-Message"));
        verify(paymentService, never()).updatePaymentStatus(anyLong(), any());
    }

    // ========== CANCEL PAYMENT TESTS ==========

    @Test
    void testCancelPayment_Success() {
        // Given
        Long paymentId = 1L;
        String reason = "user_requested";
        Payment cancelledPayment = new Payment();
        cancelledPayment.setId(paymentId);
        cancelledPayment.setStatus(PaymentStatus.CANCELLED);

        when(paymentService.cancelPayment(paymentId, reason)).thenReturn(cancelledPayment);

        // When
        ResponseEntity<PaymentResponse> response = paymentController.cancelPayment(paymentId, reason);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(PaymentStatus.CANCELLED, response.getBody().getStatus());
        verify(paymentService).cancelPayment(paymentId, reason);
    }

    @Test
    void testCancelPayment_DefaultReason() {
        // Given
        Long paymentId = 1L;
        Payment cancelledPayment = new Payment();
        cancelledPayment.setId(paymentId);
        cancelledPayment.setStatus(PaymentStatus.CANCELLED);

        when(paymentService.cancelPayment(paymentId, null)).thenThrow(new RuntimeException("Payment not found"));

        // When
        ResponseEntity<PaymentResponse> response = paymentController.cancelPayment(paymentId, null);

        // Then
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        verify(paymentService).cancelPayment(paymentId, null);
    }

    @Test
    void testCancelPayment_Exception() {
        // Given
        Long paymentId = 1L;
        when(paymentService.cancelPayment(paymentId, "user_requested")).thenThrow(new RuntimeException("Service error"));

        // When
        ResponseEntity<PaymentResponse> response = paymentController.cancelPayment(paymentId, "user_requested");

        // Then
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNull(response.getBody());
    }

    // ========== PAYMENT EXISTS TESTS ==========

    @Test
    void testPaymentExists_True() {
        // Given
        Long paymentId = 1L;
        when(paymentService.existsById(paymentId)).thenReturn(true);

        // When
        ResponseEntity<Boolean> response = paymentController.paymentExists(paymentId);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody());
        verify(paymentService).existsById(paymentId);
    }

    @Test
    void testPaymentExists_False() {
        // Given
        Long paymentId = 1L;
        when(paymentService.existsById(paymentId)).thenReturn(false);

        // When
        ResponseEntity<Boolean> response = paymentController.paymentExists(paymentId);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertFalse(response.getBody());
        verify(paymentService).existsById(paymentId);
    }

    @Test
    void testPaymentExists_Exception() {
        // Given
        Long paymentId = 1L;
        when(paymentService.existsById(paymentId)).thenThrow(new RuntimeException("Service error"));

        // When
        ResponseEntity<Boolean> response = paymentController.paymentExists(paymentId);

        // Then
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNull(response.getBody());
    }

    // ========== GET PAYMENT ATTEMPTS TESTS ==========

    @Test
    void testGetPaymentAttempts_Success() {
        // Given
        Long paymentId = 1L;
        PaymentAttempt attempt1 = new PaymentAttempt();
        attempt1.setId(1L);
        attempt1.setPaymentId(paymentId);
        attempt1.setStatus(PaymentStatus.APPROVED);
        attempt1.setResponseCode("success");
        attempt1.setCreatedAt(LocalDateTime.now());
        
        PaymentAttempt attempt2 = new PaymentAttempt();
        attempt2.setId(2L);
        attempt2.setPaymentId(paymentId);
        attempt2.setStatus(PaymentStatus.REJECTED);
        attempt2.setResponseCode("failed");
        attempt2.setCreatedAt(LocalDateTime.now());
        
        List<PaymentAttempt> attempts = Arrays.asList(attempt1, attempt2);
        when(paymentAttemptService.getAttemptsByPaymentId(paymentId)).thenReturn(attempts);

        // When
        ResponseEntity<List<PaymentAttempt>> response = paymentController.getPaymentAttempts(paymentId);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());
        verify(paymentAttemptService).getAttemptsByPaymentId(paymentId);
    }

    @Test
    void testGetPaymentAttempts_Exception() {
        // Given
        Long paymentId = 1L;
        when(paymentAttemptService.getAttemptsByPaymentId(paymentId)).thenThrow(new RuntimeException("Service error"));

        // When
        ResponseEntity<List<PaymentAttempt>> response = paymentController.getPaymentAttempts(paymentId);

        // Then
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNull(response.getBody());
    }

    // ========== RETRY PAYMENT BY BALANCE TESTS ==========

    @Test
    void testRetryPaymentByBalance_Success() {
        // Given
        Long paymentId = 1L;
        String authHeader = "Bearer valid-token";
        testPayment.setStatus(PaymentStatus.REJECTED);
        testPayment.setRejected_by_balance(true);
        testPayment.setRetry_attempts(1);

        SelectPaymentMethodRequest request = new SelectPaymentMethodRequest();
        request.setPaymentMethodType("CREDIT_CARD");
        request.setCardNumber("4111111111111111");
        request.setCardHolderName("Test User");
        request.setExpirationMonth(12);
        request.setExpirationYear(2025);
        request.setCvv("123");

        CreditCardPayment creditCardPayment = mock(CreditCardPayment.class);
        when(creditCardPayment.getType()).thenReturn(PaymentMethodType.CREDIT_CARD);

        Payment updatedPayment = new Payment();
        updatedPayment.setId(paymentId);
        updatedPayment.setStatus(PaymentStatus.PENDING_APPROVAL); // Para tarjetas va a PENDING_APPROVAL después de updatePaymentMethod
        updatedPayment.setRetry_attempts(2);
        updatedPayment.setMethod(creditCardPayment);
        updatedPayment.setUser_id(testUser.getId());
        updatedPayment.setAmount_total(testPayment.getAmount_total());

        Payment finalPayment = new Payment();
        finalPayment.setId(paymentId);
        finalPayment.setStatus(PaymentStatus.PENDING_PAYMENT); // Después de createPayment
        finalPayment.setRetry_attempts(2);
        finalPayment.setMethod(creditCardPayment);
        finalPayment.setUser_id(testUser.getId());
        finalPayment.setAmount_total(testPayment.getAmount_total());

        when(authenticationService.getUserFromToken(authHeader)).thenReturn(testUser);
        when(entityValidationService.getPaymentOrThrow(paymentId)).thenReturn(testPayment);
        when(balanceService.canRetryPayment(paymentId)).thenReturn(true);
        when(paymentService.updatePaymentStatus(paymentId, PaymentStatus.PENDING_PAYMENT)).thenReturn(testPayment);
        when(paymentMethodService.createPaymentMethod(any(SelectPaymentMethodRequest.class))).thenReturn(creditCardPayment);
        when(paymentService.updatePaymentMethod(anyLong(), any())).thenReturn(updatedPayment);
        // Primera llamada después de updatePaymentMethod (retorna PENDING_APPROVAL)
        // Segunda llamada después de createPayment (retorna PENDING_PAYMENT)
        when(paymentService.getPaymentById(paymentId))
            .thenReturn(Optional.of(updatedPayment))  // Primera llamada
            .thenReturn(Optional.of(finalPayment));    // Segunda llamada
        when(responseMapperService.mapPaymentToResponse(any(Payment.class), anyString())).thenReturn(PaymentResponse.fromEntity(finalPayment));
        when(paymentService.createPayment(any(Payment.class))).thenReturn(finalPayment);

        // When
        ResponseEntity<PaymentResponse> response = paymentController.retryPaymentByBalance(paymentId, authHeader, request);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        verify(balanceService).canRetryPayment(paymentId);
        verify(paymentService).updatePaymentStatus(paymentId, PaymentStatus.PENDING_PAYMENT);
        verify(paymentMethodService).createPaymentMethod(any(SelectPaymentMethodRequest.class));
        verify(paymentService).updatePaymentMethod(anyLong(), any());
    }

    @Test
    void testRetryPaymentByBalance_Unauthorized() {
        // Given
        Long paymentId = 1L;
        String authHeader = "Bearer invalid-token";
        SelectPaymentMethodRequest request = new SelectPaymentMethodRequest();
        request.setPaymentMethodType("CREDIT_CARD");
        when(authenticationService.getUserFromToken(authHeader)).thenReturn(null);

        // When
        ResponseEntity<PaymentResponse> response = paymentController.retryPaymentByBalance(paymentId, authHeader, request);

        // Then
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNull(response.getBody());
        verify(balanceService, never()).canRetryPayment(anyLong());
    }

    @Test
    void testRetryPaymentByBalance_Forbidden() {
        // Given
        Long paymentId = 1L;
        String authHeader = "Bearer valid-token";
        SelectPaymentMethodRequest request = new SelectPaymentMethodRequest();
        request.setPaymentMethodType("CREDIT_CARD");
        testPayment.setUser_id(999L); // Different user

        when(authenticationService.getUserFromToken(authHeader)).thenReturn(testUser);
        when(entityValidationService.getPaymentOrThrow(paymentId)).thenReturn(testPayment);

        // When
        ResponseEntity<PaymentResponse> response = paymentController.retryPaymentByBalance(paymentId, authHeader, request);

        // Then
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertNull(response.getBody());
        verify(balanceService, never()).canRetryPayment(anyLong());
    }

    @Test
    void testRetryPaymentByBalance_CannotRetry() {
        // Given
        Long paymentId = 1L;
        String authHeader = "Bearer valid-token";
        SelectPaymentMethodRequest request = new SelectPaymentMethodRequest();
        request.setPaymentMethodType("CREDIT_CARD");
        when(authenticationService.getUserFromToken(authHeader)).thenReturn(testUser);
        when(entityValidationService.getPaymentOrThrow(paymentId)).thenReturn(testPayment);
        when(balanceService.canRetryPayment(paymentId)).thenReturn(false);

        // When
        ResponseEntity<PaymentResponse> response = paymentController.retryPaymentByBalance(paymentId, authHeader, request);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNull(response.getBody());
        assertTrue(response.getHeaders().containsKey("Error-Message"));
        verify(balanceService).canRetryPayment(paymentId);
        verify(balanceService, never()).hasSufficientBalance(anyLong(), any(BigDecimal.class));
    }

    @Test
    void testRetryPaymentByBalance_InsufficientBalance() {
        // Given
        Long paymentId = 1L;
        String authHeader = "Bearer valid-token";
        SelectPaymentMethodRequest request = new SelectPaymentMethodRequest();
        request.setPaymentMethodType("MERCADO_PAGO");
        request.setMercadoPagoUserId("123456");
        request.setAccessToken("token123");
        
        testPayment.setStatus(PaymentStatus.REJECTED);
        testPayment.setRejected_by_balance(true);
        
        when(authenticationService.getUserFromToken(authHeader)).thenReturn(testUser);
        when(entityValidationService.getPaymentOrThrow(paymentId)).thenReturn(testPayment);
        when(balanceService.canRetryPayment(paymentId)).thenReturn(true);
        when(paymentMethodService.createPaymentMethod(any(SelectPaymentMethodRequest.class))).thenReturn(mock(backend_api.Backend.Entity.payment.types.MercadoPagoPayment.class));
        when(paymentService.updatePaymentMethod(anyLong(), any())).thenReturn(testPayment);
        when(paymentService.getPaymentById(paymentId)).thenReturn(Optional.of(testPayment));
        when(balanceService.hasSufficientBalance(1L, BigDecimal.valueOf(115.00))).thenReturn(false);
        when(balanceService.getCurrentBalance(1L)).thenReturn(BigDecimal.valueOf(50.00));

        // When
        ResponseEntity<PaymentResponse> response = paymentController.retryPaymentByBalance(paymentId, authHeader, request);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNull(response.getBody());
        assertTrue(response.getHeaders().containsKey("Error-Message"));
        verify(balanceService).hasSufficientBalance(1L, BigDecimal.valueOf(115.00));
        verify(paymentService, never()).createPayment(any(Payment.class));
    }

    // ========== GET PAYMENT BY ID TESTS ==========

    @Test
    void testGetPaymentById_Success_User() {
        // Given
        Long paymentId = 1L;
        String authHeader = "Bearer valid-token";
        when(authenticationService.getUserFromToken(authHeader)).thenReturn(testUser);
        when(entityValidationService.getPaymentOrThrow(paymentId)).thenReturn(testPayment);
        
        PaymentResponse expectedResponse = new PaymentResponse();
        expectedResponse.setId(testPayment.getId());
        when(responseMapperService.mapPaymentToResponse(testPayment, "USER")).thenReturn(expectedResponse);

        // When
        ResponseEntity<PaymentResponse> response = paymentController.getPaymentById(paymentId, authHeader);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(testPayment.getId(), response.getBody().getId());
    }

    @Test
    void testGetPaymentById_Success_Merchant() {
        // Given
        Long paymentId = 1L;
        String authHeader = "Bearer valid-token";
        testPayment.setProvider_id(2L); // Merchant's ID
        when(authenticationService.getUserFromToken(authHeader)).thenReturn(merchantUser);
        when(entityValidationService.getPaymentOrThrow(paymentId)).thenReturn(testPayment);
        
        PaymentResponse expectedResponse = new PaymentResponse();
        expectedResponse.setId(testPayment.getId());
        when(responseMapperService.mapPaymentToResponse(testPayment, "MERCHANT")).thenReturn(expectedResponse);

        // When
        ResponseEntity<PaymentResponse> response = paymentController.getPaymentById(paymentId, authHeader);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(testPayment.getId(), response.getBody().getId());
    }

    @Test
    void testGetPaymentById_Forbidden() {
        // Given
        Long paymentId = 1L;
        String authHeader = "Bearer valid-token";
        testPayment.setUser_id(999L); // Different user
        testPayment.setProvider_id(999L); // Different provider
        when(authenticationService.getUserFromToken(authHeader)).thenReturn(testUser);
        when(entityValidationService.getPaymentOrThrow(paymentId)).thenReturn(testPayment);
        doThrow(new SecurityException("Access denied")).when(entityValidationService).validatePaymentOwnership(paymentId, testUser.getId(), "USER");

        // When
        ResponseEntity<PaymentResponse> response = paymentController.getPaymentById(paymentId, authHeader);

        // Then
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    void testGetPaymentById_Unauthorized() {
        // Given
        Long paymentId = 1L;
        String authHeader = "Bearer invalid-token";
        when(authenticationService.getUserFromToken(authHeader)).thenReturn(null);

        // When
        ResponseEntity<PaymentResponse> response = paymentController.getPaymentById(paymentId, authHeader);

        // Then
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNull(response.getBody());
    }

    // ========== GET MY PAYMENTS TESTS ==========

    @Test
    void testGetMyPayments_Success_User() {
        // Given
        String authHeader = "Bearer valid-token";
        List<Payment> payments = Arrays.asList(testPayment);
        when(authenticationService.getUserFromToken(authHeader)).thenReturn(testUser);
        when(paymentService.getPaymentsByUserId(1L, 0, 50)).thenReturn(payments);
        
        PaymentResponse expectedResponse = new PaymentResponse();
        expectedResponse.setId(testPayment.getId());
        when(responseMapperService.mapPaymentsToResponses(payments, "USER")).thenReturn(Arrays.asList(expectedResponse));

        // When
        ResponseEntity<List<PaymentResponse>> response = paymentController.getMyPayments(authHeader, 0, 50);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        verify(paymentService).getPaymentsByUserId(1L, 0, 50);
    }

    @Test
    void testGetMyPayments_Success_Merchant() {
        // Given
        String authHeader = "Bearer valid-token";
        List<Payment> payments = Arrays.asList(testPayment);
        when(authenticationService.getUserFromToken(authHeader)).thenReturn(merchantUser);
        when(paymentService.getPaymentsByProviderId(2L, 0, 50)).thenReturn(payments);
        
        PaymentResponse expectedResponse = new PaymentResponse();
        expectedResponse.setId(testPayment.getId());
        when(responseMapperService.mapPaymentsToResponses(payments, "MERCHANT")).thenReturn(Arrays.asList(expectedResponse));

        // When
        ResponseEntity<List<PaymentResponse>> response = paymentController.getMyPayments(authHeader, 0, 50);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        verify(paymentService).getPaymentsByProviderId(2L, 0, 50);
    }

    @Test
    void testGetMyPayments_Unauthorized() {
        // Given
        String authHeader = "Bearer invalid-token";
        when(authenticationService.getUserFromToken(authHeader)).thenReturn(null);

        // When
        ResponseEntity<List<PaymentResponse>> response = paymentController.getMyPayments(authHeader, 0, 10);

        // Then
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNull(response.getBody());
        verify(paymentService, never()).getPaymentsByUserId(anyLong());
        verify(paymentService, never()).getPaymentsByProviderId(anyLong());
    }

    // ========== GET MY PAYMENTS BY STATUS TESTS ==========

    @Test
    void testGetMyPaymentsByStatus_Success_User() {
        // Given
        String authHeader = "Bearer valid-token";
        PaymentStatus status = PaymentStatus.APPROVED;
        List<Payment> payments = Arrays.asList(testPayment);
        when(authenticationService.getUserFromToken(authHeader)).thenReturn(testUser);
        when(paymentService.getPaymentsByUserAndStatus(1L, status)).thenReturn(payments);
        
        PaymentResponse expectedResponse = new PaymentResponse();
        expectedResponse.setId(testPayment.getId());
        when(responseMapperService.mapPaymentsToResponses(payments, "USER")).thenReturn(Arrays.asList(expectedResponse));

        // When
        ResponseEntity<List<PaymentResponse>> response = paymentController.getMyPaymentsByStatus(authHeader, status);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        verify(paymentService).getPaymentsByUserAndStatus(1L, status);
    }

    @Test
    void testGetMyPaymentsByStatus_Success_Merchant() {
        // Given
        String authHeader = "Bearer valid-token";
        PaymentStatus status = PaymentStatus.APPROVED;
        List<Payment> payments = Arrays.asList(testPayment);
        when(authenticationService.getUserFromToken(authHeader)).thenReturn(merchantUser);
        when(paymentService.getPaymentsByProviderAndStatus(2L, status)).thenReturn(payments);
        
        PaymentResponse expectedResponse = new PaymentResponse();
        expectedResponse.setId(testPayment.getId());
        when(responseMapperService.mapPaymentsToResponses(payments, "MERCHANT")).thenReturn(Arrays.asList(expectedResponse));

        // When
        ResponseEntity<List<PaymentResponse>> response = paymentController.getMyPaymentsByStatus(authHeader, status);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        verify(paymentService).getPaymentsByProviderAndStatus(2L, status);
    }

    // ========== GET MY TOTAL AMOUNT TESTS ==========

    @Test
    void testGetMyTotalAmount_Success_User() {
        // Given
        String authHeader = "Bearer valid-token";
        BigDecimal total = BigDecimal.valueOf(1000.00);
        when(authenticationService.getUserFromToken(authHeader)).thenReturn(testUser);
        when(paymentService.getTotalAmountByUserId(1L)).thenReturn(total);

        // When
        ResponseEntity<BigDecimal> response = paymentController.getMyTotalAmount(authHeader);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(total, response.getBody());
        verify(paymentService).getTotalAmountByUserId(1L);
    }

    @Test
    void testGetMyTotalAmount_Success_Merchant() {
        // Given
        String authHeader = "Bearer valid-token";
        BigDecimal total = BigDecimal.valueOf(5000.00);
        when(authenticationService.getUserFromToken(authHeader)).thenReturn(merchantUser);
        when(paymentService.getTotalAmountByProviderId(2L)).thenReturn(total);

        // When
        ResponseEntity<BigDecimal> response = paymentController.getMyTotalAmount(authHeader);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(total, response.getBody());
        verify(paymentService).getTotalAmountByProviderId(2L);
    }

    // ========== GET MY BALANCE TESTS ==========

    @Test
    void testGetMyBalance_Success_User() {
        // Given
        String authHeader = "Bearer valid-token";
        BigDecimal balance = BigDecimal.valueOf(1000.00);
        when(authenticationService.getUserFromToken(authHeader)).thenReturn(testUser);
        when(balanceService.getCurrentBalance(1L)).thenReturn(balance);

        // When
        ResponseEntity<BigDecimal> response = paymentController.getMyBalance(authHeader);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(balance, response.getBody());
        verify(balanceService).getCurrentBalance(1L);
    }

    @Test
    void testGetMyBalance_MerchantNotAllowed() {
        // Given
        String authHeader = "Bearer valid-token";
        when(authenticationService.getUserFromToken(authHeader)).thenReturn(merchantUser);

        // When
        ResponseEntity<BigDecimal> response = paymentController.getMyBalance(authHeader);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNull(response.getBody());
        assertTrue(response.getHeaders().containsKey("Error-Message"));
        verify(balanceService, never()).getCurrentBalance(anyLong());
    }

    // ========== SEARCH MY PAYMENTS TESTS ==========

    @Test
    void testSearchMyPayments_Success_User() {
        // Given
        String authHeader = "Bearer valid-token";
        PaymentSearchRequest request = new PaymentSearchRequest();
        request.setStatus(PaymentStatus.APPROVED);
        request.setCurrency("USD");
        request.setMinAmount(BigDecimal.valueOf(50.00));
        request.setMaxAmount(BigDecimal.valueOf(200.00));

        List<Payment> payments = Arrays.asList(testPayment);
        when(authenticationService.getUserFromToken(authHeader)).thenReturn(testUser);
        when(paymentService.getPaymentsByUserId(1L)).thenReturn(payments);

        // When
        ResponseEntity<PagedPaymentResponse> response = paymentController.searchMyPayments(authHeader, request, 0, 10);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertNotNull(response.getBody().getContent());
        verify(paymentService).getPaymentsByUserId(1L);
    }

    @Test
    void testSearchMyPayments_Success_Merchant() {
        // Given
        String authHeader = "Bearer valid-token";
        PaymentSearchRequest request = new PaymentSearchRequest();
        List<Payment> payments = Arrays.asList(testPayment);
        when(authenticationService.getUserFromToken(authHeader)).thenReturn(merchantUser);
        when(paymentService.getPaymentsByProviderId(2L)).thenReturn(payments);

        // When
        ResponseEntity<PagedPaymentResponse> response = paymentController.searchMyPayments(authHeader, request, 0, 10);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        verify(paymentService).getPaymentsByProviderId(2L);
    }

    @Test
    void testSearchMyPayments_Unauthorized() {
        // Given
        String authHeader = "Bearer invalid-token";
        PaymentSearchRequest request = new PaymentSearchRequest();
        when(authenticationService.getUserFromToken(authHeader)).thenReturn(null);

        // When
        ResponseEntity<PagedPaymentResponse> response = paymentController.searchMyPayments(authHeader, request, 0, 10);

        // Then
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNull(response.getBody());
        verify(paymentService, never()).getPaymentsByUserId(anyLong());
        verify(paymentService, never()).getPaymentsByProviderId(anyLong());
    }
}
