package backend_api.Backend.DTO.payment;

import backend_api.Backend.Entity.payment.Payment;
import backend_api.Backend.Entity.payment.PaymentMethod;
import backend_api.Backend.Entity.payment.types.CreditCardPayment;
import backend_api.Backend.Entity.payment.PaymentStatus;
import backend_api.Backend.Entity.payment.types.PaymentMethodType;
import backend_api.Backend.Entity.user.User;
import backend_api.Backend.Repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

/**
 * Unit tests for PaymentResponse DTO
 * 
 * Tests the conversion methods and data mapping from Payment entity to PaymentResponse DTO,
 * including the fromEntity and fromEntityWithNames methods with different user roles.
 */
@ExtendWith(MockitoExtension.class)
class PaymentResponseTest {

    @Mock
    private UserRepository userRepository;

    private Payment testPayment;
    private User testUser;
    private User testProvider;

    @BeforeEach
    void setUp() {
        // Setup test payment
        testPayment = new Payment();
        testPayment.setId(1L);
        testPayment.setUser_id(1L);
        testPayment.setProvider_id(2L);
        testPayment.setSolicitud_id(10L);
        testPayment.setRefund_id(20L);
        testPayment.setAmount_subtotal(BigDecimal.valueOf(100.00));
        testPayment.setTaxes(BigDecimal.valueOf(10.00));
        testPayment.setFees(BigDecimal.valueOf(5.00));
        testPayment.setAmount_total(BigDecimal.valueOf(115.00));
        testPayment.setCurrency("USD");
        testPayment.setStatus(PaymentStatus.APPROVED);
        testPayment.setCreated_at(LocalDateTime.of(2024, 1, 1, 10, 0));
        testPayment.setUpdated_at(LocalDateTime.of(2024, 1, 1, 11, 0));
        testPayment.setCaptured_at(LocalDateTime.of(2024, 1, 1, 12, 0));
        testPayment.setExpired_at(LocalDateTime.of(2024, 1, 2, 10, 0));
        testPayment.setMetadata("Test payment metadata");

        // Setup payment method
        CreditCardPayment paymentMethod = new CreditCardPayment();
        paymentMethod.setType(PaymentMethodType.CREDIT_CARD);
        testPayment.setMethod(paymentMethod);

        // Setup test user
        testUser = new User();
        testUser.setId(1L);
        testUser.setName("Test User");
        testUser.setEmail("user@example.com");

        // Setup test provider
        testProvider = new User();
        testProvider.setId(2L);
        testProvider.setName("Test Provider");
        testProvider.setEmail("provider@example.com");
    }

    // ========== FROM ENTITY TESTS ==========

    @Test
    void testFromEntity_AllFieldsMapped() {
        // When
        PaymentResponse response = PaymentResponse.fromEntity(testPayment);

        // Then
        assertNotNull(response);
        assertEquals(testPayment.getId(), response.getId());
        assertEquals(testPayment.getUser_id(), response.getUser_id());
        assertEquals(testPayment.getProvider_id(), response.getProvider_id());
        assertEquals(testPayment.getSolicitud_id(), response.getSolicitud_id());
        assertEquals(testPayment.getRefund_id(), response.getRefund_id());
        assertEquals(testPayment.getAmount_subtotal(), response.getAmount_subtotal());
        assertEquals(testPayment.getTaxes(), response.getTaxes());
        assertEquals(testPayment.getFees(), response.getFees());
        assertEquals(testPayment.getAmount_total(), response.getAmount_total());
        assertEquals(testPayment.getCurrency(), response.getCurrency());
        assertEquals(testPayment.getMethod(), response.getMethod());
        assertEquals(testPayment.getStatus(), response.getStatus());
        assertEquals(testPayment.getCreated_at(), response.getCreated_at());
        assertEquals(testPayment.getUpdated_at(), response.getUpdated_at());
        assertEquals(testPayment.getCaptured_at(), response.getCaptured_at());
        assertEquals(testPayment.getExpired_at(), response.getExpired_at());
        assertEquals(testPayment.getMetadata(), response.getMetadata());
    }

    @Test
    void testFromEntity_NullFields() {
        // Given
        Payment paymentWithNulls = new Payment();
        paymentWithNulls.setId(1L);
        paymentWithNulls.setUser_id(1L);
        paymentWithNulls.setProvider_id(2L);
        // Leave other fields as null

        // When
        PaymentResponse response = PaymentResponse.fromEntity(paymentWithNulls);

        // Then
        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals(1L, response.getUser_id());
        assertEquals(2L, response.getProvider_id());
        assertNull(response.getSolicitud_id());
        assertNull(response.getRefund_id());
        assertNull(response.getAmount_subtotal());
        assertNull(response.getTaxes());
        assertNull(response.getFees());
        assertNull(response.getAmount_total());
        assertNull(response.getCurrency());
        assertNull(response.getMethod());
        assertNull(response.getStatus());
        assertNull(response.getCreated_at());
        assertNull(response.getUpdated_at());
        assertNull(response.getCaptured_at());
        assertNull(response.getExpired_at());
        assertNull(response.getMetadata());
    }

    @Test
    void testFromEntity_EmptyPayment() {
        // Given
        Payment emptyPayment = new Payment();

        // When
        PaymentResponse response = PaymentResponse.fromEntity(emptyPayment);

        // Then
        assertNotNull(response);
        assertNull(response.getId());
        assertNull(response.getUser_id());
        assertNull(response.getProvider_id());
        assertNull(response.getSolicitud_id());
        assertNull(response.getRefund_id());
        assertNull(response.getAmount_subtotal());
        assertNull(response.getTaxes());
        assertNull(response.getFees());
        assertNull(response.getAmount_total());
        assertNull(response.getCurrency());
        assertNull(response.getMethod());
        assertNull(response.getStatus());
        assertNull(response.getCreated_at());
        assertNull(response.getUpdated_at());
        assertNull(response.getCaptured_at());
        assertNull(response.getExpired_at());
        assertNull(response.getMetadata());
    }

    // ========== FROM ENTITY WITH NAMES TESTS ==========

    @Test
    void testFromEntityWithNames_UserRole_UserAndProviderFound() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.findById(2L)).thenReturn(Optional.of(testProvider));

        // When
        PaymentResponse response = PaymentResponse.fromEntityWithNames(testPayment, userRepository, "USER");

        // Then
        assertNotNull(response);
        assertEquals(testPayment.getId(), response.getId());
        assertNull(response.getUser_name()); // Should be null for USER role
        assertEquals("Test Provider", response.getProvider_name());
    }

    @Test
    void testFromEntityWithNames_MerchantRole_UserAndProviderFound() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.findById(2L)).thenReturn(Optional.of(testProvider));

        // When
        PaymentResponse response = PaymentResponse.fromEntityWithNames(testPayment, userRepository, "MERCHANT");

        // Then
        assertNotNull(response);
        assertEquals(testPayment.getId(), response.getId());
        assertEquals("Test User", response.getUser_name());
        assertNull(response.getProvider_name()); // Should be null for MERCHANT role
    }

    @Test
    void testFromEntityWithNames_UserNotFound() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.empty());
        when(userRepository.findById(2L)).thenReturn(Optional.of(testProvider));

        // When
        PaymentResponse response = PaymentResponse.fromEntityWithNames(testPayment, userRepository, "USER");

        // Then
        assertNotNull(response);
        assertEquals(testPayment.getId(), response.getId());
        assertNull(response.getUser_name());
        assertEquals("Test Provider", response.getProvider_name());
    }

    @Test
    void testFromEntityWithNames_ProviderNotFound() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.findById(2L)).thenReturn(Optional.empty());

        // When
        PaymentResponse response = PaymentResponse.fromEntityWithNames(testPayment, userRepository, "USER");

        // Then
        assertNotNull(response);
        assertEquals(testPayment.getId(), response.getId());
        assertNull(response.getUser_name());
        assertNull(response.getProvider_name());
    }

    @Test
    void testFromEntityWithNames_BothUsersNotFound() {
        // Given
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        // When
        PaymentResponse response = PaymentResponse.fromEntityWithNames(testPayment, userRepository, "USER");

        // Then
        assertNotNull(response);
        assertEquals(testPayment.getId(), response.getId());
        assertNull(response.getUser_name());
        assertNull(response.getProvider_name());
    }

    @Test
    void testFromEntityWithNames_RepositoryException() {
        // Given
        when(userRepository.findById(anyLong())).thenThrow(new RuntimeException("Database error"));

        // When
        PaymentResponse response = PaymentResponse.fromEntityWithNames(testPayment, userRepository, "USER");

        // Then
        assertNotNull(response);
        assertEquals(testPayment.getId(), response.getId());
        assertNull(response.getUser_name());
        assertNull(response.getProvider_name());
        // Should not throw exception, should handle gracefully
    }

    @Test
    void testFromEntityWithNames_NullUserRepository() {
        // When
        PaymentResponse response = PaymentResponse.fromEntityWithNames(testPayment, null, "USER");

        // Then
        assertNotNull(response);
        assertEquals(testPayment.getId(), response.getId());
        assertNull(response.getUser_name());
        assertNull(response.getProvider_name());
        // Should not throw exception, should handle gracefully
    }

    @Test
    void testFromEntityWithNames_NullCurrentUserRole() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.findById(2L)).thenReturn(Optional.of(testProvider));

        // When
        PaymentResponse response = PaymentResponse.fromEntityWithNames(testPayment, userRepository, null);

        // Then
        assertNotNull(response);
        assertEquals(testPayment.getId(), response.getId());
        assertNull(response.getUser_name()); // null role defaults to else branch, sets user_name to null
        assertEquals("Test Provider", response.getProvider_name());
    }

    @Test
    void testFromEntityWithNames_EmptyCurrentUserRole() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.findById(2L)).thenReturn(Optional.of(testProvider));

        // When
        PaymentResponse response = PaymentResponse.fromEntityWithNames(testPayment, userRepository, "");

        // Then
        assertNotNull(response);
        assertEquals(testPayment.getId(), response.getId());
        assertNull(response.getUser_name()); // empty role defaults to else branch, sets user_name to null
        assertEquals("Test Provider", response.getProvider_name());
    }

    @Test
    void testFromEntityWithNames_UnknownRole() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.findById(2L)).thenReturn(Optional.of(testProvider));

        // When
        PaymentResponse response = PaymentResponse.fromEntityWithNames(testPayment, userRepository, "ADMIN");

        // Then
        assertNotNull(response);
        assertEquals(testPayment.getId(), response.getId());
        assertNull(response.getUser_name()); // unknown role defaults to else branch, sets user_name to null
        assertEquals("Test Provider", response.getProvider_name());
    }

    // ========== EDGE CASES ==========

    @Test
    void testFromEntityWithNames_NullPayment() {
        // When & Then - fromEntity throws NullPointerException for null payment
        assertThrows(NullPointerException.class, () -> {
            PaymentResponse.fromEntityWithNames(null, userRepository, "USER");
        });
    }

    @Test
    void testFromEntityWithNames_PaymentWithNullUserIds() {
        // Given
        Payment paymentWithNullIds = new Payment();
        paymentWithNullIds.setId(1L);
        paymentWithNullIds.setUser_id(null);
        paymentWithNullIds.setProvider_id(null);

        // When
        PaymentResponse response = PaymentResponse.fromEntityWithNames(paymentWithNullIds, userRepository, "USER");

        // Then
        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertNull(response.getUser_name());
        assertNull(response.getProvider_name());
        // Should handle null user IDs gracefully
    }

    @Test
    void testFromEntityWithNames_UserWithNullName() {
        // Given
        User userWithNullName = new User();
        userWithNullName.setId(1L);
        userWithNullName.setName(null);
        userWithNullName.setEmail("user@example.com");

        when(userRepository.findById(1L)).thenReturn(Optional.of(userWithNullName));
        when(userRepository.findById(2L)).thenReturn(Optional.of(testProvider));

        // When
        PaymentResponse response = PaymentResponse.fromEntityWithNames(testPayment, userRepository, "MERCHANT");

        // Then
        assertNotNull(response);
        assertEquals(testPayment.getId(), response.getId());
        assertNull(response.getUser_name()); // user name is null, and MERCHANT role sets provider_name to null
        assertNull(response.getProvider_name()); // MERCHANT role sets provider_name to null
    }

    @Test
    void testFromEntityWithNames_ProviderWithNullName() {
        // Given
        User providerWithNullName = new User();
        providerWithNullName.setId(2L);
        providerWithNullName.setName(null);
        providerWithNullName.setEmail("provider@example.com");

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.findById(2L)).thenReturn(Optional.of(providerWithNullName));

        // When
        PaymentResponse response = PaymentResponse.fromEntityWithNames(testPayment, userRepository, "USER");

        // Then
        assertNotNull(response);
        assertEquals(testPayment.getId(), response.getId());
        assertNull(response.getUser_name());
        assertNull(response.getProvider_name());
    }
}
