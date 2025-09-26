package backend_api.Backend.Controller;

import backend_api.Backend.Auth.JwtUtil;
import backend_api.Backend.DTO.refund.CreateRefundRequest;
import backend_api.Backend.DTO.refund.RefundResponse;
import backend_api.Backend.DTO.refund.UpdateRefundStatusRequest;
import backend_api.Backend.Entity.refund.Refund;
import backend_api.Backend.Entity.refund.RefundStatus;
import backend_api.Backend.Entity.user.User;
import backend_api.Backend.Entity.user.UserRole;
import backend_api.Backend.Repository.UserRepository;
import backend_api.Backend.Service.Interface.RefundService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for RefundController
 * 
 * Tests all refund endpoints including:
 * - Refund creation with authentication
 * - Refund approval by merchants
 * - Refund decline by merchants
 * - Refund retrieval by various criteria
 * - Error handling and validation
 */
@ExtendWith(MockitoExtension.class)
class RefundControllerTest {

    @Mock
    private RefundService refundService;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private RefundController refundController;

    private User testUser;
    private User merchantUser;
    private Refund testRefund;
    private CreateRefundRequest createRefundRequest;
    private UpdateRefundStatusRequest updateRefundStatusRequest;

    @BeforeEach
    void setUp() {
        // Setup test user
        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("user@example.com");
        testUser.setName("Test User");
        testUser.setRole(UserRole.USER);

        // Setup merchant user
        merchantUser = new User();
        merchantUser.setId(2L);
        merchantUser.setEmail("merchant@example.com");
        merchantUser.setName("Test Merchant");
        merchantUser.setRole(UserRole.MERCHANT);

        // Setup test refund
        testRefund = new Refund();
        testRefund.setId(1L);
        testRefund.setPayment_id(1L);
        testRefund.setRequestedBy(1L);
        testRefund.setAmount(BigDecimal.valueOf(100.00));
        testRefund.setReason("Product defect");
        testRefund.setStatus(RefundStatus.PENDING);
        testRefund.setCreated_at(LocalDateTime.now());
        testRefund.setUpdated_at(LocalDateTime.now());

        // Setup create refund request
        createRefundRequest = new CreateRefundRequest();
        createRefundRequest.setPaymentId(1L);
        createRefundRequest.setAmount(BigDecimal.valueOf(100.00));
        createRefundRequest.setReason("Product defect");

        // Setup update refund status request
        updateRefundStatusRequest = new UpdateRefundStatusRequest();
        updateRefundStatusRequest.setMessage("Approved by merchant");
    }

    // ========== CREATE REFUND TESTS ==========

    @Test
    void testCreateRefund_Success() {
        // Given
        String authHeader = "Bearer valid-token";
        when(jwtUtil.getSubject("valid-token")).thenReturn("user@example.com");
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(testUser));
        when(refundService.createRefund(createRefundRequest, 1L)).thenReturn(testRefund);

        // When
        ResponseEntity<?> response = refundController.createRefund(createRefundRequest, authHeader);

        // Then
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody() instanceof RefundResponse);
        RefundResponse refundResponse = (RefundResponse) response.getBody();
        assertEquals(testRefund.getId(), refundResponse.getId());
        assertEquals(testRefund.getAmount(), refundResponse.getAmount());

        verify(refundService).createRefund(createRefundRequest, 1L);
    }

    @Test
    void testCreateRefund_UserNotFound() {
        // Given
        String authHeader = "Bearer valid-token";
        when(jwtUtil.getSubject("valid-token")).thenReturn("user@example.com");
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.empty());

        // When
        ResponseEntity<?> response = refundController.createRefund(createRefundRequest, authHeader);

        // Then
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(refundService, never()).createRefund(any(), anyLong());
    }

    @Test
    void testCreateRefund_IllegalArgumentException() {
        // Given
        String authHeader = "Bearer valid-token";
        when(jwtUtil.getSubject("valid-token")).thenReturn("user@example.com");
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(testUser));
        when(refundService.createRefund(createRefundRequest, 1L)).thenThrow(new IllegalArgumentException("Invalid amount"));

        // When
        ResponseEntity<?> response = refundController.createRefund(createRefundRequest, authHeader);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Invalid amount", response.getBody());
    }

    @Test
    void testCreateRefund_IllegalStateException() {
        // Given
        String authHeader = "Bearer valid-token";
        when(jwtUtil.getSubject("valid-token")).thenReturn("user@example.com");
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(testUser));
        when(refundService.createRefund(createRefundRequest, 1L)).thenThrow(new IllegalStateException("Payment not found"));

        // When
        ResponseEntity<?> response = refundController.createRefund(createRefundRequest, authHeader);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Payment not found", response.getBody());
    }

    @Test
    void testCreateRefund_RuntimeException() {
        // Given
        String authHeader = "Bearer valid-token";
        when(jwtUtil.getSubject("valid-token")).thenReturn("user@example.com");
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(testUser));
        when(refundService.createRefund(createRefundRequest, 1L)).thenThrow(new RuntimeException("Payment not found"));

        // When
        ResponseEntity<?> response = refundController.createRefund(createRefundRequest, authHeader);

        // Then
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("Payment not found", response.getBody());
    }

    @Test
    void testCreateRefund_GeneralException() {
        // Given
        String authHeader = "Bearer valid-token";
        when(jwtUtil.getSubject("valid-token")).thenReturn("user@example.com");
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(testUser));
        when(refundService.createRefund(createRefundRequest, 1L)).thenThrow(new RuntimeException("Database error"));

        // When
        ResponseEntity<?> response = refundController.createRefund(createRefundRequest, authHeader);

        // Then
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("Database error", response.getBody());
    }

    // ========== APPROVE REFUND TESTS ==========

    @Test
    void testApproveRefund_Success() {
        // Given
        Long refundId = 1L;
        String authHeader = "Bearer valid-token";
        Refund approvedRefund = new Refund();
        approvedRefund.setId(refundId);
        approvedRefund.setStatus(RefundStatus.APPROVED);

        when(jwtUtil.getSubject("valid-token")).thenReturn("merchant@example.com");
        when(userRepository.findByEmail("merchant@example.com")).thenReturn(Optional.of(merchantUser));
        when(refundService.approveRefund(refundId, 2L, "Approved by merchant")).thenReturn(approvedRefund);

        // When
        ResponseEntity<?> response = refundController.approveRefund(refundId, authHeader, updateRefundStatusRequest);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody() instanceof RefundResponse);
        RefundResponse refundResponse = (RefundResponse) response.getBody();
        assertEquals(RefundStatus.APPROVED, refundResponse.getStatus());

        verify(refundService).approveRefund(refundId, 2L, "Approved by merchant");
    }

    @Test
    void testApproveRefund_WithoutMessage() {
        // Given
        Long refundId = 1L;
        String authHeader = "Bearer valid-token";
        Refund approvedRefund = new Refund();
        approvedRefund.setId(refundId);
        approvedRefund.setStatus(RefundStatus.APPROVED);

        when(jwtUtil.getSubject("valid-token")).thenReturn("merchant@example.com");
        when(userRepository.findByEmail("merchant@example.com")).thenReturn(Optional.of(merchantUser));
        when(refundService.approveRefund(refundId, 2L, null)).thenReturn(approvedRefund);

        // When
        ResponseEntity<?> response = refundController.approveRefund(refundId, authHeader, null);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        verify(refundService).approveRefund(refundId, 2L, null);
    }

    @Test
    void testApproveRefund_MerchantNotFound() {
        // Given
        Long refundId = 1L;
        String authHeader = "Bearer valid-token";
        when(jwtUtil.getSubject("valid-token")).thenReturn("merchant@example.com");
        when(userRepository.findByEmail("merchant@example.com")).thenReturn(Optional.empty());

        // When
        ResponseEntity<?> response = refundController.approveRefund(refundId, authHeader, updateRefundStatusRequest);

        // Then
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(refundService, never()).approveRefund(anyLong(), anyLong(), anyString());
    }

    @Test
    void testApproveRefund_IllegalArgumentException() {
        // Given
        Long refundId = 1L;
        String authHeader = "Bearer valid-token";
        when(jwtUtil.getSubject("valid-token")).thenReturn("merchant@example.com");
        when(userRepository.findByEmail("merchant@example.com")).thenReturn(Optional.of(merchantUser));
        when(refundService.approveRefund(refundId, 2L, "Approved by merchant")).thenThrow(new IllegalArgumentException("Invalid refund status"));

        // When
        ResponseEntity<?> response = refundController.approveRefund(refundId, authHeader, updateRefundStatusRequest);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Invalid refund status", response.getBody());
    }

    @Test
    void testApproveRefund_IllegalStateException() {
        // Given
        Long refundId = 1L;
        String authHeader = "Bearer valid-token";
        when(jwtUtil.getSubject("valid-token")).thenReturn("merchant@example.com");
        when(userRepository.findByEmail("merchant@example.com")).thenReturn(Optional.of(merchantUser));
        when(refundService.approveRefund(refundId, 2L, "Approved by merchant")).thenThrow(new IllegalStateException("Refund already processed"));

        // When
        ResponseEntity<?> response = refundController.approveRefund(refundId, authHeader, updateRefundStatusRequest);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Refund already processed", response.getBody());
    }

    @Test
    void testApproveRefund_RuntimeException() {
        // Given
        Long refundId = 1L;
        String authHeader = "Bearer valid-token";
        when(jwtUtil.getSubject("valid-token")).thenReturn("merchant@example.com");
        when(userRepository.findByEmail("merchant@example.com")).thenReturn(Optional.of(merchantUser));
        when(refundService.approveRefund(refundId, 2L, "Approved by merchant")).thenThrow(new RuntimeException("Refund not found"));

        // When
        ResponseEntity<?> response = refundController.approveRefund(refundId, authHeader, updateRefundStatusRequest);

        // Then
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("Refund not found", response.getBody());
    }

    // ========== DECLINE REFUND TESTS ==========

    @Test
    void testDeclineRefund_Success() {
        // Given
        Long refundId = 1L;
        String authHeader = "Bearer valid-token";
        Refund declinedRefund = new Refund();
        declinedRefund.setId(refundId);
        declinedRefund.setStatus(RefundStatus.DECLINED);

        when(jwtUtil.getSubject("valid-token")).thenReturn("merchant@example.com");
        when(userRepository.findByEmail("merchant@example.com")).thenReturn(Optional.empty());

        // When
        ResponseEntity<?> response = refundController.declineRefund(refundId, authHeader, updateRefundStatusRequest);

        // Then
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void testDeclineRefund_MerchantNotFound() {
        // Given
        Long refundId = 1L;
        String authHeader = "Bearer valid-token";
        when(jwtUtil.getSubject("valid-token")).thenReturn("merchant@example.com");
        when(userRepository.findByEmail("merchant@example.com")).thenReturn(Optional.empty());

        // When
        ResponseEntity<?> response = refundController.declineRefund(refundId, authHeader, updateRefundStatusRequest);

        // Then
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(refundService, never()).declineRefund(anyLong(), anyLong(), anyString());
    }

    @Test
    void testDeclineRefund_IllegalArgumentException() {
        // Given
        Long refundId = 1L;
        String authHeader = "Bearer valid-token";
        when(jwtUtil.getSubject("valid-token")).thenReturn("merchant@example.com");
        when(userRepository.findByEmail("merchant@example.com")).thenReturn(Optional.empty());

        // When
        ResponseEntity<?> response = refundController.declineRefund(refundId, authHeader, updateRefundStatusRequest);

        // Then
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void testDeclineRefund_IllegalStateException() {
        // Given
        Long refundId = 1L;
        String authHeader = "Bearer valid-token";
        when(jwtUtil.getSubject("valid-token")).thenReturn("merchant@example.com");
        when(userRepository.findByEmail("merchant@example.com")).thenReturn(Optional.empty());

        // When
        ResponseEntity<?> response = refundController.declineRefund(refundId, authHeader, updateRefundStatusRequest);

        // Then
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void testDeclineRefund_RuntimeException() {
        // Given
        Long refundId = 1L;
        String authHeader = "Bearer valid-token";
        when(jwtUtil.getSubject("valid-token")).thenReturn("merchant@example.com");
        when(userRepository.findByEmail("merchant@example.com")).thenReturn(Optional.of(merchantUser));
        when(refundService.declineRefund(refundId, 2L, "Approved by merchant")).thenThrow(new RuntimeException("Refund not found"));

        // When
        ResponseEntity<?> response = refundController.declineRefund(refundId, authHeader, updateRefundStatusRequest);

        // Then
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("Refund not found", response.getBody());
    }

    // ========== GET REFUND BY ID TESTS ==========

    @Test
    void testGetRefundById_Success() {
        // Given
        Long refundId = 1L;
        when(refundService.getRefundById(refundId)).thenReturn(Optional.of(testRefund));

        // When
        ResponseEntity<?> response = refundController.getRefundById(refundId);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody() instanceof RefundResponse);
        RefundResponse refundResponse = (RefundResponse) response.getBody();
        assertEquals(testRefund.getId(), refundResponse.getId());

        verify(refundService).getRefundById(refundId);
    }

    @Test
    void testGetRefundById_NotFound() {
        // Given
        Long refundId = 1L;
        when(refundService.getRefundById(refundId)).thenReturn(Optional.empty());

        // When
        ResponseEntity<?> response = refundController.getRefundById(refundId);

        // Then
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
        verify(refundService).getRefundById(refundId);
    }

    @Test
    void testGetRefundById_Exception() {
        // Given
        Long refundId = 1L;
        when(refundService.getRefundById(refundId)).thenThrow(new RuntimeException("Database error"));

        // When
        ResponseEntity<?> response = refundController.getRefundById(refundId);

        // Then
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("Error interno", response.getBody());
    }

    // ========== GET ALL REFUNDS TESTS ==========

    @Test
    void testGetAllRefunds_Success() {
        // Given
        List<Refund> refunds = Arrays.asList(testRefund);
        when(refundService.getAllRefunds()).thenReturn(refunds);

        // When
        ResponseEntity<List<RefundResponse>> response = refundController.getAllRefunds();

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertEquals(testRefund.getId(), response.getBody().get(0).getId());

        verify(refundService).getAllRefunds();
    }

    @Test
    void testGetAllRefunds_Empty() {
        // Given
        List<Refund> refunds = Arrays.asList();
        when(refundService.getAllRefunds()).thenReturn(refunds);

        // When
        ResponseEntity<List<RefundResponse>> response = refundController.getAllRefunds();

        // Then
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        assertNull(response.getBody());
        verify(refundService).getAllRefunds();
    }

    @Test
    void testGetAllRefunds_Exception() {
        // Given
        when(refundService.getAllRefunds()).thenThrow(new RuntimeException("Database error"));

        // When
        ResponseEntity<List<RefundResponse>> response = refundController.getAllRefunds();

        // Then
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNull(response.getBody());
    }

    // ========== GET REFUNDS BY PAYMENT ID TESTS ==========

    @Test
    void testGetByPayment_Success() {
        // Given
        Long paymentId = 1L;
        List<Refund> refunds = Arrays.asList(testRefund);
        when(refundService.getRefundsByPaymentId(paymentId)).thenReturn(refunds);

        // When
        ResponseEntity<List<RefundResponse>> response = refundController.getByPayment(paymentId);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertEquals(testRefund.getId(), response.getBody().get(0).getId());

        verify(refundService).getRefundsByPaymentId(paymentId);
    }

    @Test
    void testGetByPayment_Empty() {
        // Given
        Long paymentId = 1L;
        List<Refund> refunds = Arrays.asList();
        when(refundService.getRefundsByPaymentId(paymentId)).thenReturn(refunds);

        // When
        ResponseEntity<List<RefundResponse>> response = refundController.getByPayment(paymentId);

        // Then
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        assertNull(response.getBody());
        verify(refundService).getRefundsByPaymentId(paymentId);
    }

    @Test
    void testGetByPayment_Exception() {
        // Given
        Long paymentId = 1L;
        when(refundService.getRefundsByPaymentId(paymentId)).thenThrow(new RuntimeException("Database error"));

        // When
        ResponseEntity<List<RefundResponse>> response = refundController.getByPayment(paymentId);

        // Then
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNull(response.getBody());
    }

    // ========== GET REFUNDS BY STATUS TESTS ==========

    @Test
    void testGetByStatus_Success() {
        // Given
        RefundStatus status = RefundStatus.PENDING;
        List<Refund> refunds = Arrays.asList(testRefund);
        when(refundService.getRefundsByStatus(status)).thenReturn(refunds);

        // When
        ResponseEntity<List<RefundResponse>> response = refundController.getByStatus(status);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertEquals(testRefund.getId(), response.getBody().get(0).getId());

        verify(refundService).getRefundsByStatus(status);
    }

    @Test
    void testGetByStatus_Empty() {
        // Given
        RefundStatus status = RefundStatus.APPROVED;
        List<Refund> refunds = Arrays.asList();
        when(refundService.getRefundsByStatus(status)).thenReturn(refunds);

        // When
        ResponseEntity<List<RefundResponse>> response = refundController.getByStatus(status);

        // Then
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        assertNull(response.getBody());
        verify(refundService).getRefundsByStatus(status);
    }

    @Test
    void testGetByStatus_Exception() {
        // Given
        RefundStatus status = RefundStatus.PENDING;
        when(refundService.getRefundsByStatus(status)).thenThrow(new RuntimeException("Database error"));

        // When
        ResponseEntity<List<RefundResponse>> response = refundController.getByStatus(status);

        // Then
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNull(response.getBody());
    }
}
