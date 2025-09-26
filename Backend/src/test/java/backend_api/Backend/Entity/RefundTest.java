package backend_api.Backend.Entity;

import backend_api.Backend.Entity.refund.Refund;
import backend_api.Backend.Entity.refund.RefundStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class RefundTest {

    private Refund refund;

    @BeforeEach
    void setUp() {
        refund = new Refund();
    }

    @Test
    void testDefaultConstructor() {
        assertNotNull(refund);
        assertNull(refund.getId());
        assertNull(refund.getPayment_id());
        assertNull(refund.getAmount());
        assertNull(refund.getReason());
        assertNull(refund.getStatus());
        assertNull(refund.getGateway_refund_id());
        assertNull(refund.getCreated_at());
        assertNull(refund.getUpdated_at());
        assertNull(refund.getRequestedBy());
        assertNull(refund.getReviewedBy());
        assertNull(refund.getReviewedAt());
        assertNull(refund.getDecisionMessage());
    }

    @Test
    void testSettersAndGetters() {
        // Test basic setters and getters
        refund.setId(1L);
        refund.setPayment_id(100L);
        refund.setAmount(BigDecimal.valueOf(50.00));
        refund.setReason("Product defect");
        refund.setStatus(RefundStatus.APPROVED);
        refund.setGateway_refund_id("gw_refund_123");
        LocalDateTime now = LocalDateTime.now();
        refund.setCreated_at(now);
        refund.setUpdated_at(now);
        refund.setRequestedBy(200L);
        refund.setReviewedBy(300L);
        refund.setReviewedAt(now);
        refund.setDecisionMessage("Approved by merchant");

        assertEquals(1L, refund.getId());
        assertEquals(100L, refund.getPayment_id());
        assertEquals(BigDecimal.valueOf(50.00), refund.getAmount());
        assertEquals("Product defect", refund.getReason());
        assertEquals(RefundStatus.APPROVED, refund.getStatus());
        assertEquals("gw_refund_123", refund.getGateway_refund_id());
        assertEquals(now, refund.getCreated_at());
        assertEquals(now, refund.getUpdated_at());
        assertEquals(200L, refund.getRequestedBy());
        assertEquals(300L, refund.getReviewedBy());
        assertEquals(now, refund.getReviewedAt());
        assertEquals("Approved by merchant", refund.getDecisionMessage());
    }

    @Test
    void testPrePersist_SetsTimestampsAndDefaultStatus() {
        // Verify initial state
        assertNull(refund.getCreated_at());
        assertNull(refund.getUpdated_at());
        assertNull(refund.getStatus());

        // Call prePersist (simulating @PrePersist) - using reflection to access package-private method
        try {
            java.lang.reflect.Method prePersistMethod = Refund.class.getDeclaredMethod("prePersist");
            prePersistMethod.setAccessible(true);
            prePersistMethod.invoke(refund);
        } catch (Exception e) {
            fail("Failed to call prePersist method: " + e.getMessage());
        }

        // Verify timestamps are set
        assertNotNull(refund.getCreated_at());
        assertNotNull(refund.getUpdated_at());
        assertEquals(refund.getCreated_at(), refund.getUpdated_at());
        assertTrue(refund.getCreated_at().isBefore(LocalDateTime.now().plusSeconds(1)));
        assertTrue(refund.getCreated_at().isAfter(LocalDateTime.now().minusSeconds(1)));

        // Verify default status is set
        assertEquals(RefundStatus.PENDING, refund.getStatus());
    }

    @Test
    void testPrePersist_WithExistingStatus() {
        // Set existing status
        refund.setStatus(RefundStatus.APPROVED);

        // Call prePersist - using reflection to access package-private method
        try {
            java.lang.reflect.Method prePersistMethod = Refund.class.getDeclaredMethod("prePersist");
            prePersistMethod.setAccessible(true);
            prePersistMethod.invoke(refund);
        } catch (Exception e) {
            fail("Failed to call prePersist method: " + e.getMessage());
        }

        // Verify status is not changed
        assertEquals(RefundStatus.APPROVED, refund.getStatus());
        assertNotNull(refund.getCreated_at());
        assertNotNull(refund.getUpdated_at());
    }

    @Test
    void testPreUpdate_SetsUpdatedAt() {
        // Set initial timestamps
        LocalDateTime initialTime = LocalDateTime.now().minusHours(1);
        refund.setCreated_at(initialTime);
        refund.setUpdated_at(initialTime);

        // Call preUpdate (simulating @PreUpdate) - using reflection to access package-private method
        try {
            java.lang.reflect.Method preUpdateMethod = Refund.class.getDeclaredMethod("preUpdate");
            preUpdateMethod.setAccessible(true);
            preUpdateMethod.invoke(refund);
        } catch (Exception e) {
            fail("Failed to call preUpdate method: " + e.getMessage());
        }

        // Verify updated_at is changed but created_at remains the same
        assertEquals(initialTime, refund.getCreated_at());
        assertNotEquals(initialTime, refund.getUpdated_at());
        assertTrue(refund.getUpdated_at().isAfter(initialTime));
        assertTrue(refund.getUpdated_at().isBefore(LocalDateTime.now().plusSeconds(1)));
    }

    @Test
    void testPreUpdate_MultipleCalls() {
        // Set initial timestamp
        refund.setCreated_at(LocalDateTime.now().minusHours(1));
        refund.setUpdated_at(LocalDateTime.now().minusHours(1));

        // Call preUpdate multiple times - using reflection to access package-private method
        try {
            java.lang.reflect.Method preUpdateMethod = Refund.class.getDeclaredMethod("preUpdate");
            preUpdateMethod.setAccessible(true);
            preUpdateMethod.invoke(refund);
        } catch (Exception e) {
            fail("Failed to call preUpdate method: " + e.getMessage());
        }
        LocalDateTime firstUpdate = refund.getUpdated_at();

        // Wait a small amount of time
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        try {
            java.lang.reflect.Method preUpdateMethod = Refund.class.getDeclaredMethod("preUpdate");
            preUpdateMethod.setAccessible(true);
            preUpdateMethod.invoke(refund);
        } catch (Exception e) {
            fail("Failed to call preUpdate method: " + e.getMessage());
        }
        LocalDateTime secondUpdate = refund.getUpdated_at();

        // Verify both calls update the timestamp
        assertNotNull(firstUpdate);
        assertNotNull(secondUpdate);
        assertTrue(secondUpdate.isAfter(firstUpdate));
    }

    @Test
    void testRefundStatusEnumValues() {
        // Test all enum values
        refund.setStatus(RefundStatus.PENDING);
        assertEquals(RefundStatus.PENDING, refund.getStatus());

        refund.setStatus(RefundStatus.APPROVED);
        assertEquals(RefundStatus.APPROVED, refund.getStatus());

        refund.setStatus(RefundStatus.DECLINED);
        assertEquals(RefundStatus.DECLINED, refund.getStatus());

        refund.setStatus(RefundStatus.APPROVED);
        assertEquals(RefundStatus.APPROVED, refund.getStatus());

        refund.setStatus(RefundStatus.TOTAL_REFUND);
        assertEquals(RefundStatus.TOTAL_REFUND, refund.getStatus());

        refund.setStatus(RefundStatus.FAILED);
        assertEquals(RefundStatus.FAILED, refund.getStatus());

        refund.setStatus(RefundStatus.DECLINED);
        assertEquals(RefundStatus.DECLINED, refund.getStatus());

        refund.setStatus(RefundStatus.PARTIAL_REFUND);
        assertEquals(RefundStatus.PARTIAL_REFUND, refund.getStatus());

        refund.setStatus(RefundStatus.TOTAL_REFUND);
        assertEquals(RefundStatus.TOTAL_REFUND, refund.getStatus());
    }

    @Test
    void testAmountPrecision() {
        // Test BigDecimal precision
        refund.setAmount(BigDecimal.valueOf(123.45));
        assertEquals(BigDecimal.valueOf(123.45), refund.getAmount());

        refund.setAmount(BigDecimal.valueOf(0.01));
        assertEquals(BigDecimal.valueOf(0.01), refund.getAmount());

        refund.setAmount(BigDecimal.valueOf(999999.99));
        assertEquals(BigDecimal.valueOf(999999.99), refund.getAmount());

        // Test null amount
        refund.setAmount(null);
        assertNull(refund.getAmount());
    }

    @Test
    void testGatewayRefundIdFormats() {
        // Test various gateway refund ID formats
        refund.setGateway_refund_id("gw_refund_123456");
        assertEquals("gw_refund_123456", refund.getGateway_refund_id());

        refund.setGateway_refund_id("stripe_re_1234567890");
        assertEquals("stripe_re_1234567890", refund.getGateway_refund_id());

        refund.setGateway_refund_id("paypal_refund_abc123");
        assertEquals("paypal_refund_abc123", refund.getGateway_refund_id());

        // Test null gateway refund ID
        refund.setGateway_refund_id(null);
        assertNull(refund.getGateway_refund_id());

        // Test empty gateway refund ID
        refund.setGateway_refund_id("");
        assertEquals("", refund.getGateway_refund_id());
    }

    @Test
    void testReasonDescriptions() {
        // Test various reason descriptions
        refund.setReason("Product defect");
        assertEquals("Product defect", refund.getReason());

        refund.setReason("Customer changed mind");
        assertEquals("Customer changed mind", refund.getReason());

        refund.setReason("Duplicate payment");
        assertEquals("Duplicate payment", refund.getReason());

        refund.setReason("Service not provided");
        assertEquals("Service not provided", refund.getReason());

        // Test null reason
        refund.setReason(null);
        assertNull(refund.getReason());

        // Test empty reason
        refund.setReason("");
        assertEquals("", refund.getReason());

        // Test long reason
        String longReason = "This is a very long reason description that might exceed normal length limits and should still be handled correctly by the system";
        refund.setReason(longReason);
        assertEquals(longReason, refund.getReason());
    }

    @Test
    void testDecisionMessage() {
        // Test various decision messages
        refund.setDecisionMessage("Approved by merchant");
        assertEquals("Approved by merchant", refund.getDecisionMessage());

        refund.setDecisionMessage("Declined - insufficient documentation");
        assertEquals("Declined - insufficient documentation", refund.getDecisionMessage());

        refund.setDecisionMessage("Processing refund");
        assertEquals("Processing refund", refund.getDecisionMessage());

        // Test null decision message
        refund.setDecisionMessage(null);
        assertNull(refund.getDecisionMessage());

        // Test empty decision message
        refund.setDecisionMessage("");
        assertEquals("", refund.getDecisionMessage());
    }

    @Test
    void testToString() {
        refund.setId(1L);
        refund.setPayment_id(100L);
        refund.setAmount(BigDecimal.valueOf(50.00));
        refund.setStatus(RefundStatus.PENDING);

        String result = refund.toString();

        assertNotNull(result);
        assertTrue(result.contains("Refund"));
        assertTrue(result.contains("id=1"));
        assertTrue(result.contains("payment_id=100"));
        assertTrue(result.contains("amount=50"));
        assertTrue(result.contains("status=PENDING"));
    }

    @Test
    void testEqualsAndHashCode() {
        Refund refund1 = new Refund();
        refund1.setId(1L);
        refund1.setPayment_id(100L);
        refund1.setAmount(BigDecimal.valueOf(50.00));

        Refund refund2 = new Refund();
        refund2.setId(1L);
        refund2.setPayment_id(100L);
        refund2.setAmount(BigDecimal.valueOf(50.00));

        Refund refund3 = new Refund();
        refund3.setId(2L);
        refund3.setPayment_id(200L);
        refund3.setAmount(BigDecimal.valueOf(100.00));

        // Test equals
        assertEquals(refund1, refund2);
        assertNotEquals(refund1, refund3);
        assertNotEquals(refund2, refund3);

        // Test hashCode
        assertEquals(refund1.hashCode(), refund2.hashCode());
        assertNotEquals(refund1.hashCode(), refund3.hashCode());
    }

    @Test
    void testCompleteRefundWorkflow() {
        // Test a complete refund workflow
        refund.setId(1L);
        refund.setPayment_id(123L);
        refund.setAmount(BigDecimal.valueOf(75.50));
        refund.setReason("Product not as described");
        refund.setStatus(RefundStatus.PENDING);
        refund.setRequestedBy(456L);

        // Simulate prePersist - using reflection to access package-private method
        try {
            java.lang.reflect.Method prePersistMethod = Refund.class.getDeclaredMethod("prePersist");
            prePersistMethod.setAccessible(true);
            prePersistMethod.invoke(refund);
        } catch (Exception e) {
            fail("Failed to call prePersist method: " + e.getMessage());
        }

        // Verify initial state
        assertEquals(RefundStatus.PENDING, refund.getStatus());
        assertNotNull(refund.getCreated_at());
        assertNotNull(refund.getUpdated_at());
        assertEquals(refund.getCreated_at(), refund.getUpdated_at());

        // Simulate approval
        refund.setStatus(RefundStatus.APPROVED);
        refund.setReviewedBy(789L);
        refund.setDecisionMessage("Approved after review");
        refund.setGateway_refund_id("gw_refund_456789");

        // Simulate preUpdate - using reflection to access package-private method
        try {
            java.lang.reflect.Method preUpdateMethod = Refund.class.getDeclaredMethod("preUpdate");
            preUpdateMethod.setAccessible(true);
            preUpdateMethod.invoke(refund);
        } catch (Exception e) {
            fail("Failed to call preUpdate method: " + e.getMessage());
        }

        // Verify updated state
        assertEquals(RefundStatus.APPROVED, refund.getStatus());
        assertEquals(789L, refund.getReviewedBy());
        assertEquals("Approved after review", refund.getDecisionMessage());
        assertEquals("gw_refund_456789", refund.getGateway_refund_id());
        assertTrue(refund.getUpdated_at().isAfter(refund.getCreated_at()));
    }
}
