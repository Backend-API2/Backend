package backend_api.Backend.DTO.refund;

import backend_api.Backend.Entity.refund.Refund;
import backend_api.Backend.Entity.refund.RefundStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for RefundResponse DTO
 * 
 * Tests the conversion method from Refund entity to RefundResponse DTO,
 * including edge cases with null values and different refund statuses.
 */
class RefundResponseTest {

    private Refund testRefund;

    @BeforeEach
    void setUp() {
        // Setup test refund
        testRefund = new Refund();
        testRefund.setId(1L);
        testRefund.setPayment_id(100L);
        testRefund.setAmount(BigDecimal.valueOf(50.00));
        testRefund.setReason("Product defect");
        testRefund.setStatus(RefundStatus.PENDING);
        testRefund.setGateway_refund_id("GW_REF_12345");
        testRefund.setCreated_at(LocalDateTime.of(2024, 1, 15, 10, 30));
    }

    // ========== FROM ENTITY TESTS ==========

    @Test
    void testFromEntity_AllFieldsMapped() {
        // When
        RefundResponse response = RefundResponse.fromEntity(testRefund);

        // Then
        assertNotNull(response);
        assertEquals(testRefund.getId(), response.getId());
        assertEquals(testRefund.getPayment_id(), response.getPaymentId());
        assertEquals(testRefund.getAmount(), response.getAmount());
        assertEquals(testRefund.getReason(), response.getReason());
        assertEquals(testRefund.getStatus(), response.getStatus());
        assertEquals(testRefund.getGateway_refund_id(), response.getGatewayRefundId());
        assertEquals(testRefund.getCreated_at(), response.getCreatedAt());
    }

    @Test
    void testFromEntity_NullFields() {
        // Given
        Refund refundWithNulls = new Refund();
        refundWithNulls.setId(1L);
        refundWithNulls.setPayment_id(100L);
        // Leave other fields as null

        // When
        RefundResponse response = RefundResponse.fromEntity(refundWithNulls);

        // Then
        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals(100L, response.getPaymentId());
        assertNull(response.getAmount());
        assertNull(response.getReason());
        assertNull(response.getStatus());
        assertNull(response.getGatewayRefundId());
        assertNull(response.getCreatedAt());
    }

    @Test
    void testFromEntity_EmptyRefund() {
        // Given
        Refund emptyRefund = new Refund();

        // When
        RefundResponse response = RefundResponse.fromEntity(emptyRefund);

        // Then
        assertNotNull(response);
        assertNull(response.getId());
        assertNull(response.getPaymentId());
        assertNull(response.getAmount());
        assertNull(response.getReason());
        assertNull(response.getStatus());
        assertNull(response.getGatewayRefundId());
        assertNull(response.getCreatedAt());
    }

    @Test
    void testFromEntity_ApprovedStatus() {
        // Given
        testRefund.setStatus(RefundStatus.APPROVED);

        // When
        RefundResponse response = RefundResponse.fromEntity(testRefund);

        // Then
        assertNotNull(response);
        assertEquals(RefundStatus.APPROVED, response.getStatus());
        assertEquals(testRefund.getId(), response.getId());
        assertEquals(testRefund.getAmount(), response.getAmount());
    }

    @Test
    void testFromEntity_DeclinedStatus() {
        // Given
        testRefund.setStatus(RefundStatus.DECLINED);

        // When
        RefundResponse response = RefundResponse.fromEntity(testRefund);

        // Then
        assertNotNull(response);
        assertEquals(RefundStatus.DECLINED, response.getStatus());
        assertEquals(testRefund.getId(), response.getId());
        assertEquals(testRefund.getAmount(), response.getAmount());
    }

    @Test
    void testFromEntity_TotalRefundStatus() {
        // Given
        testRefund.setStatus(RefundStatus.TOTAL_REFUND);

        // When
        RefundResponse response = RefundResponse.fromEntity(testRefund);

        // Then
        assertNotNull(response);
        assertEquals(RefundStatus.TOTAL_REFUND, response.getStatus());
        assertEquals(testRefund.getId(), response.getId());
        assertEquals(testRefund.getAmount(), response.getAmount());
    }

    @Test
    void testFromEntity_ZeroAmount() {
        // Given
        testRefund.setAmount(BigDecimal.ZERO);

        // When
        RefundResponse response = RefundResponse.fromEntity(testRefund);

        // Then
        assertNotNull(response);
        assertEquals(BigDecimal.ZERO, response.getAmount());
        assertEquals(testRefund.getId(), response.getId());
    }

    @Test
    void testFromEntity_LargeAmount() {
        // Given
        testRefund.setAmount(BigDecimal.valueOf(999999.99));

        // When
        RefundResponse response = RefundResponse.fromEntity(testRefund);

        // Then
        assertNotNull(response);
        assertEquals(BigDecimal.valueOf(999999.99), response.getAmount());
        assertEquals(testRefund.getId(), response.getId());
    }

    @Test
    void testFromEntity_EmptyReason() {
        // Given
        testRefund.setReason("");

        // When
        RefundResponse response = RefundResponse.fromEntity(testRefund);

        // Then
        assertNotNull(response);
        assertEquals("", response.getReason());
        assertEquals(testRefund.getId(), response.getId());
    }

    @Test
    void testFromEntity_LongReason() {
        // Given
        String longReason = "This is a very long reason for the refund that contains multiple sentences. " +
                           "It explains in detail why the customer is requesting a refund for their purchase. " +
                           "The reason includes specific details about the product defect and customer experience.";
        testRefund.setReason(longReason);

        // When
        RefundResponse response = RefundResponse.fromEntity(testRefund);

        // Then
        assertNotNull(response);
        assertEquals(longReason, response.getReason());
        assertEquals(testRefund.getId(), response.getId());
    }

    @Test
    void testFromEntity_EmptyGatewayRefundId() {
        // Given
        testRefund.setGateway_refund_id("");

        // When
        RefundResponse response = RefundResponse.fromEntity(testRefund);

        // Then
        assertNotNull(response);
        assertEquals("", response.getGatewayRefundId());
        assertEquals(testRefund.getId(), response.getId());
    }

    @Test
    void testFromEntity_LongGatewayRefundId() {
        // Given
        String longGatewayId = "GW_REF_" + "A".repeat(100);
        testRefund.setGateway_refund_id(longGatewayId);

        // When
        RefundResponse response = RefundResponse.fromEntity(testRefund);

        // Then
        assertNotNull(response);
        assertEquals(longGatewayId, response.getGatewayRefundId());
        assertEquals(testRefund.getId(), response.getId());
    }

    @Test
    void testFromEntity_FutureCreatedAt() {
        // Given
        LocalDateTime futureDate = LocalDateTime.of(2030, 12, 31, 23, 59);
        testRefund.setCreated_at(futureDate);

        // When
        RefundResponse response = RefundResponse.fromEntity(testRefund);

        // Then
        assertNotNull(response);
        assertEquals(futureDate, response.getCreatedAt());
        assertEquals(testRefund.getId(), response.getId());
    }

    @Test
    void testFromEntity_PastCreatedAt() {
        // Given
        LocalDateTime pastDate = LocalDateTime.of(2020, 1, 1, 0, 0);
        testRefund.setCreated_at(pastDate);

        // When
        RefundResponse response = RefundResponse.fromEntity(testRefund);

        // Then
        assertNotNull(response);
        assertEquals(pastDate, response.getCreatedAt());
        assertEquals(testRefund.getId(), response.getId());
    }

    @Test
    void testFromEntity_ZeroId() {
        // Given
        testRefund.setId(0L);

        // When
        RefundResponse response = RefundResponse.fromEntity(testRefund);

        // Then
        assertNotNull(response);
        assertEquals(0L, response.getId());
        assertEquals(testRefund.getPayment_id(), response.getPaymentId());
    }

    @Test
    void testFromEntity_ZeroPaymentId() {
        // Given
        testRefund.setPayment_id(0L);

        // When
        RefundResponse response = RefundResponse.fromEntity(testRefund);

        // Then
        assertNotNull(response);
        assertEquals(0L, response.getPaymentId());
        assertEquals(testRefund.getId(), response.getId());
    }

    @Test
    void testFromEntity_NegativeId() {
        // Given
        testRefund.setId(-1L);

        // When
        RefundResponse response = RefundResponse.fromEntity(testRefund);

        // Then
        assertNotNull(response);
        assertEquals(-1L, response.getId());
        assertEquals(testRefund.getPayment_id(), response.getPaymentId());
    }

    @Test
    void testFromEntity_NegativePaymentId() {
        // Given
        testRefund.setPayment_id(-1L);

        // When
        RefundResponse response = RefundResponse.fromEntity(testRefund);

        // Then
        assertNotNull(response);
        assertEquals(-1L, response.getPaymentId());
        assertEquals(testRefund.getId(), response.getId());
    }

    // ========== EDGE CASES ==========

    @Test
    void testFromEntity_NullRefund() {
        // When & Then
        assertThrows(NullPointerException.class, () -> {
            RefundResponse.fromEntity(null);
        });
    }

    @Test
    void testFromEntity_AllNullFields() {
        // Given
        Refund allNullRefund = new Refund();
        // All fields remain null

        // When
        RefundResponse response = RefundResponse.fromEntity(allNullRefund);

        // Then
        assertNotNull(response);
        assertNull(response.getId());
        assertNull(response.getPaymentId());
        assertNull(response.getAmount());
        assertNull(response.getReason());
        assertNull(response.getStatus());
        assertNull(response.getGatewayRefundId());
        assertNull(response.getCreatedAt());
    }

    @Test
    void testFromEntity_SpecialCharactersInReason() {
        // Given
        String specialReason = "Reason with special chars: !@#$%^&*()_+-=[]{}|;':\",./<>?";
        testRefund.setReason(specialReason);

        // When
        RefundResponse response = RefundResponse.fromEntity(testRefund);

        // Then
        assertNotNull(response);
        assertEquals(specialReason, response.getReason());
        assertEquals(testRefund.getId(), response.getId());
    }

    @Test
    void testFromEntity_UnicodeCharactersInReason() {
        // Given
        String unicodeReason = "Reason with unicode: ñáéíóú çüöä ÑÁÉÍÓÚ ÇÜÖÄ 中文 🎉";
        testRefund.setReason(unicodeReason);

        // When
        RefundResponse response = RefundResponse.fromEntity(testRefund);

        // Then
        assertNotNull(response);
        assertEquals(unicodeReason, response.getReason());
        assertEquals(testRefund.getId(), response.getId());
    }

    @Test
    void testFromEntity_SpecialCharactersInGatewayId() {
        // Given
        String specialGatewayId = "GW_REF_!@#$%^&*()_+-=[]{}|;':\",./<>?";
        testRefund.setGateway_refund_id(specialGatewayId);

        // When
        RefundResponse response = RefundResponse.fromEntity(testRefund);

        // Then
        assertNotNull(response);
        assertEquals(specialGatewayId, response.getGatewayRefundId());
        assertEquals(testRefund.getId(), response.getId());
    }
}
