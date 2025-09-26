package backend_api.Backend.Entity;

import backend_api.Backend.Entity.invoice.InvoiceLine;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class InvoiceLineTest {

    private InvoiceLine invoiceLine;

    @BeforeEach
    void setUp() {
        invoiceLine = new InvoiceLine();
    }

    @Test
    void testDefaultConstructor() {
        assertNotNull(invoiceLine);
        assertNull(invoiceLine.getId());
        assertNull(invoiceLine.getInvoiceId());
        assertNull(invoiceLine.getProductId());
        assertNull(invoiceLine.getDescription());
        assertNull(invoiceLine.getProductName());
        assertNull(invoiceLine.getProductCode());
        assertNull(invoiceLine.getQuantity());
        assertNull(invoiceLine.getUnitPrice());
        assertNull(invoiceLine.getSubtotal());
        assertNull(invoiceLine.getTaxRate());
        assertNull(invoiceLine.getTaxAmount());
        assertNull(invoiceLine.getDiscountRate());
        assertNull(invoiceLine.getDiscountAmount());
        assertNull(invoiceLine.getTotalAmount());
        assertNull(invoiceLine.getLineNumber());
        assertNull(invoiceLine.getUnitOfMeasure());
        assertNull(invoiceLine.getCreatedAt());
        assertNull(invoiceLine.getUpdatedAt());
    }

    @Test
    void testSettersAndGetters() {
        // Test basic setters and getters
        invoiceLine.setId(1L);
        invoiceLine.setInvoiceId(100L);
        invoiceLine.setProductId(200L);
        invoiceLine.setDescription("Test Product");
        invoiceLine.setProductName("Product Name");
        invoiceLine.setProductCode("PROD001");
        invoiceLine.setQuantity(5);
        invoiceLine.setUnitPrice(BigDecimal.valueOf(10.00));
        invoiceLine.setSubtotal(BigDecimal.valueOf(50.00));
        invoiceLine.setTaxRate(BigDecimal.valueOf(21.00));
        invoiceLine.setTaxAmount(BigDecimal.valueOf(10.50));
        invoiceLine.setDiscountRate(BigDecimal.valueOf(10.00));
        invoiceLine.setDiscountAmount(BigDecimal.valueOf(5.00));
        invoiceLine.setTotalAmount(BigDecimal.valueOf(55.50));
        invoiceLine.setLineNumber(1);
        invoiceLine.setUnitOfMeasure("pcs");
        LocalDateTime now = LocalDateTime.now();
        invoiceLine.setCreatedAt(now);
        invoiceLine.setUpdatedAt(now);

        assertEquals(1L, invoiceLine.getId());
        assertEquals(100L, invoiceLine.getInvoiceId());
        assertEquals(200L, invoiceLine.getProductId());
        assertEquals("Test Product", invoiceLine.getDescription());
        assertEquals("Product Name", invoiceLine.getProductName());
        assertEquals("PROD001", invoiceLine.getProductCode());
        assertEquals(5, invoiceLine.getQuantity());
        assertEquals(BigDecimal.valueOf(10.00), invoiceLine.getUnitPrice());
        assertEquals(BigDecimal.valueOf(50.00), invoiceLine.getSubtotal());
        assertEquals(BigDecimal.valueOf(21.00), invoiceLine.getTaxRate());
        assertEquals(BigDecimal.valueOf(10.50), invoiceLine.getTaxAmount());
        assertEquals(BigDecimal.valueOf(10.00), invoiceLine.getDiscountRate());
        assertEquals(BigDecimal.valueOf(5.00), invoiceLine.getDiscountAmount());
        assertEquals(BigDecimal.valueOf(55.50), invoiceLine.getTotalAmount());
        assertEquals(1, invoiceLine.getLineNumber());
        assertEquals("pcs", invoiceLine.getUnitOfMeasure());
        assertEquals(now, invoiceLine.getCreatedAt());
        assertEquals(now, invoiceLine.getUpdatedAt());
    }

    @Test
    void testOnCreate_CalculatesTotals() {
        // Setup invoice line with required fields
        invoiceLine.setQuantity(2);
        invoiceLine.setUnitPrice(BigDecimal.valueOf(15.00));
        invoiceLine.setDiscountRate(BigDecimal.valueOf(10.00));
        invoiceLine.setTaxRate(BigDecimal.valueOf(21.00));

        // Call onCreate (simulating @PrePersist) - using reflection to access protected method
        try {
            java.lang.reflect.Method onCreateMethod = InvoiceLine.class.getDeclaredMethod("onCreate");
            onCreateMethod.setAccessible(true);
            onCreateMethod.invoke(invoiceLine);
        } catch (Exception e) {
            fail("Failed to call onCreate method: " + e.getMessage());
        }

        // Verify totals are calculated
        assertNotNull(invoiceLine.getCreatedAt());
        assertEquals(BigDecimal.valueOf(30.00), invoiceLine.getSubtotal()); // 2 * 15.00
        assertEquals(0, BigDecimal.valueOf(3.0).compareTo(invoiceLine.getDiscountAmount())); // 30.00 * 0.10
        assertEquals(0, BigDecimal.valueOf(5.67).compareTo(invoiceLine.getTaxAmount())); // 27.00 * 0.21
        assertEquals(0, BigDecimal.valueOf(32.67).compareTo(invoiceLine.getTotalAmount())); // 27.00 + 5.67
    }

    @Test
    void testOnUpdate_CalculatesTotals() {
        // Setup invoice line with required fields
        invoiceLine.setQuantity(3);
        invoiceLine.setUnitPrice(BigDecimal.valueOf(20.00));
        invoiceLine.setDiscountRate(BigDecimal.valueOf(5.00));
        invoiceLine.setTaxRate(BigDecimal.valueOf(18.00));

        // Call onUpdate (simulating @PreUpdate) - using reflection to access protected method
        try {
            java.lang.reflect.Method onUpdateMethod = InvoiceLine.class.getDeclaredMethod("onUpdate");
            onUpdateMethod.setAccessible(true);
            onUpdateMethod.invoke(invoiceLine);
        } catch (Exception e) {
            fail("Failed to call onUpdate method: " + e.getMessage());
        }

        // Verify totals are calculated
        assertNotNull(invoiceLine.getUpdatedAt());
        assertEquals(BigDecimal.valueOf(60.00), invoiceLine.getSubtotal()); // 3 * 20.00
        assertEquals(0, BigDecimal.valueOf(3.0).compareTo(invoiceLine.getDiscountAmount())); // 60.00 * 0.05
        assertEquals(0, BigDecimal.valueOf(10.26).compareTo(invoiceLine.getTaxAmount())); // 57.00 * 0.18
        assertEquals(0, BigDecimal.valueOf(67.26).compareTo(invoiceLine.getTotalAmount())); // 57.00 + 10.26
    }

    @Test
    void testCalculateTotals_WithDiscountAmount() {
        // Setup invoice line with discount amount instead of rate
        invoiceLine.setQuantity(4);
        invoiceLine.setUnitPrice(BigDecimal.valueOf(25.00));
        invoiceLine.setDiscountAmount(BigDecimal.valueOf(10.00));
        invoiceLine.setTaxRate(BigDecimal.valueOf(15.00));

        // Call calculateTotals - using reflection to access private method
        try {
            java.lang.reflect.Method calculateTotalsMethod = InvoiceLine.class.getDeclaredMethod("calculateTotals");
            calculateTotalsMethod.setAccessible(true);
            calculateTotalsMethod.invoke(invoiceLine);
        } catch (Exception e) {
            fail("Failed to call calculateTotals method: " + e.getMessage());
        }

        // Verify totals are calculated
        assertEquals(BigDecimal.valueOf(100.00), invoiceLine.getSubtotal()); // 4 * 25.00
        assertEquals(BigDecimal.valueOf(10.00), invoiceLine.getDiscountAmount()); // Fixed amount
        assertEquals(0, BigDecimal.valueOf(13.5).compareTo(invoiceLine.getTaxAmount())); // 90.00 * 0.15
        assertEquals(0, BigDecimal.valueOf(103.50).compareTo(invoiceLine.getTotalAmount())); // 90.00 + 13.50
    }

    @Test
    void testCalculateTotals_WithTaxAmount() {
        // Setup invoice line with tax amount instead of rate
        invoiceLine.setQuantity(2);
        invoiceLine.setUnitPrice(BigDecimal.valueOf(30.00));
        invoiceLine.setDiscountRate(BigDecimal.valueOf(8.00));
        invoiceLine.setTaxAmount(BigDecimal.valueOf(5.00));

        // Call calculateTotals - using reflection to access private method
        try {
            java.lang.reflect.Method calculateTotalsMethod = InvoiceLine.class.getDeclaredMethod("calculateTotals");
            calculateTotalsMethod.setAccessible(true);
            calculateTotalsMethod.invoke(invoiceLine);
        } catch (Exception e) {
            fail("Failed to call calculateTotals method: " + e.getMessage());
        }

        // Verify totals are calculated
        assertEquals(BigDecimal.valueOf(60.00), invoiceLine.getSubtotal()); // 2 * 30.00
        assertEquals(0, BigDecimal.valueOf(4.8).compareTo(invoiceLine.getDiscountAmount())); // 60.00 * 0.08
        assertEquals(BigDecimal.valueOf(5.00), invoiceLine.getTaxAmount()); // Fixed amount
        assertEquals(0, BigDecimal.valueOf(60.20).compareTo(invoiceLine.getTotalAmount())); // 55.20 + 5.00
    }

    @Test
    void testCalculateTotals_NoDiscountNoTax() {
        // Setup invoice line without discount or tax
        invoiceLine.setQuantity(1);
        invoiceLine.setUnitPrice(BigDecimal.valueOf(50.00));

        // Call calculateTotals - using reflection to access private method
        try {
            java.lang.reflect.Method calculateTotalsMethod = InvoiceLine.class.getDeclaredMethod("calculateTotals");
            calculateTotalsMethod.setAccessible(true);
            calculateTotalsMethod.invoke(invoiceLine);
        } catch (Exception e) {
            fail("Failed to call calculateTotals method: " + e.getMessage());
        }

        // Verify totals are calculated
        assertEquals(BigDecimal.valueOf(50.00), invoiceLine.getSubtotal()); // 1 * 50.00
        assertNull(invoiceLine.getDiscountAmount());
        assertNull(invoiceLine.getTaxAmount());
        assertEquals(BigDecimal.valueOf(50.00), invoiceLine.getTotalAmount()); // No discount, no tax
    }

    @Test
    void testCalculateTotals_NullQuantity() {
        // Setup invoice line with null quantity
        invoiceLine.setUnitPrice(BigDecimal.valueOf(10.00));

        // Call calculateTotals - using reflection to access private method
        try {
            java.lang.reflect.Method calculateTotalsMethod = InvoiceLine.class.getDeclaredMethod("calculateTotals");
            calculateTotalsMethod.setAccessible(true);
            calculateTotalsMethod.invoke(invoiceLine);
        } catch (Exception e) {
            fail("Failed to call calculateTotals method: " + e.getMessage());
        }

        // Verify no calculation occurs
        assertNull(invoiceLine.getSubtotal());
        assertNull(invoiceLine.getTotalAmount());
    }

    @Test
    void testCalculateTotals_NullUnitPrice() {
        // Setup invoice line with null unit price
        invoiceLine.setQuantity(2);

        // Call calculateTotals - using reflection to access private method
        try {
            java.lang.reflect.Method calculateTotalsMethod = InvoiceLine.class.getDeclaredMethod("calculateTotals");
            calculateTotalsMethod.setAccessible(true);
            calculateTotalsMethod.invoke(invoiceLine);
        } catch (Exception e) {
            fail("Failed to call calculateTotals method: " + e.getMessage());
        }

        // Verify no calculation occurs
        assertNull(invoiceLine.getSubtotal());
        assertNull(invoiceLine.getTotalAmount());
    }

    @Test
    void testCalculateTotals_ZeroDiscountRate() {
        // Setup invoice line with zero discount rate
        invoiceLine.setQuantity(2);
        invoiceLine.setUnitPrice(BigDecimal.valueOf(15.00));
        invoiceLine.setDiscountRate(BigDecimal.ZERO);
        invoiceLine.setTaxRate(BigDecimal.valueOf(10.00));

        // Call calculateTotals - using reflection to access private method
        try {
            java.lang.reflect.Method calculateTotalsMethod = InvoiceLine.class.getDeclaredMethod("calculateTotals");
            calculateTotalsMethod.setAccessible(true);
            calculateTotalsMethod.invoke(invoiceLine);
        } catch (Exception e) {
            fail("Failed to call calculateTotals method: " + e.getMessage());
        }

        // Verify discount is not applied
        assertEquals(BigDecimal.valueOf(30.00), invoiceLine.getSubtotal()); // 2 * 15.00
        assertNull(invoiceLine.getDiscountAmount()); // Zero rate means no discount
        assertEquals(0, BigDecimal.valueOf(3.0).compareTo(invoiceLine.getTaxAmount())); // 30.00 * 0.10
        assertEquals(0, BigDecimal.valueOf(33.00).compareTo(invoiceLine.getTotalAmount())); // 30.00 + 3.00
    }

    @Test
    void testCalculateTotals_ZeroTaxRate() {
        // Setup invoice line with zero tax rate
        invoiceLine.setQuantity(3);
        invoiceLine.setUnitPrice(BigDecimal.valueOf(20.00));
        invoiceLine.setDiscountRate(BigDecimal.valueOf(5.00));
        invoiceLine.setTaxRate(BigDecimal.ZERO);

        // Call calculateTotals - using reflection to access private method
        try {
            java.lang.reflect.Method calculateTotalsMethod = InvoiceLine.class.getDeclaredMethod("calculateTotals");
            calculateTotalsMethod.setAccessible(true);
            calculateTotalsMethod.invoke(invoiceLine);
        } catch (Exception e) {
            fail("Failed to call calculateTotals method: " + e.getMessage());
        }

        // Verify tax is not applied
        assertEquals(BigDecimal.valueOf(60.00), invoiceLine.getSubtotal()); // 3 * 20.00
        assertEquals(0, BigDecimal.valueOf(3.0).compareTo(invoiceLine.getDiscountAmount())); // 60.00 * 0.05
        assertNull(invoiceLine.getTaxAmount()); // Zero rate means no tax
        assertEquals(0, BigDecimal.valueOf(57.00).compareTo(invoiceLine.getTotalAmount())); // 60.00 - 3.00
    }

    @Test
    void testCalculateTotals_ComplexScenario() {
        // Setup complex invoice line
        invoiceLine.setQuantity(10);
        invoiceLine.setUnitPrice(BigDecimal.valueOf(100.00));
        invoiceLine.setDiscountRate(BigDecimal.valueOf(15.00));
        invoiceLine.setTaxRate(BigDecimal.valueOf(21.00));

        // Call calculateTotals - using reflection to access private method
        try {
            java.lang.reflect.Method calculateTotalsMethod = InvoiceLine.class.getDeclaredMethod("calculateTotals");
            calculateTotalsMethod.setAccessible(true);
            calculateTotalsMethod.invoke(invoiceLine);
        } catch (Exception e) {
            fail("Failed to call calculateTotals method: " + e.getMessage());
        }

        // Verify complex calculation
        assertEquals(BigDecimal.valueOf(1000.00), invoiceLine.getSubtotal()); // 10 * 100.00
        assertEquals(0, BigDecimal.valueOf(150.0).compareTo(invoiceLine.getDiscountAmount())); // 1000.00 * 0.15
        assertEquals(0, BigDecimal.valueOf(178.50).compareTo(invoiceLine.getTaxAmount())); // 850.00 * 0.21
        assertEquals(0, BigDecimal.valueOf(1028.50).compareTo(invoiceLine.getTotalAmount())); // 850.00 + 178.50
    }

    @Test
    void testToString() {
        invoiceLine.setId(1L);
        invoiceLine.setDescription("Test Product");
        invoiceLine.setQuantity(2);
        invoiceLine.setUnitPrice(BigDecimal.valueOf(10.00));

        String result = invoiceLine.toString();

        assertNotNull(result);
        assertTrue(result.contains("InvoiceLine"));
        assertTrue(result.contains("id=1"));
        assertTrue(result.contains("description=Test Product"));
        assertTrue(result.contains("quantity=2"));
        assertTrue(result.contains("unitPrice=10"));
    }

    @Test
    void testEqualsAndHashCode() {
        InvoiceLine line1 = new InvoiceLine();
        line1.setId(1L);
        line1.setDescription("Product 1");

        InvoiceLine line2 = new InvoiceLine();
        line2.setId(1L);
        line2.setDescription("Product 1");

        InvoiceLine line3 = new InvoiceLine();
        line3.setId(2L);
        line3.setDescription("Product 2");

        // Test equals
        assertEquals(line1, line2);
        assertNotEquals(line1, line3);
        assertNotEquals(line2, line3);

        // Test hashCode
        assertEquals(line1.hashCode(), line2.hashCode());
        assertNotEquals(line1.hashCode(), line3.hashCode());
    }
}
