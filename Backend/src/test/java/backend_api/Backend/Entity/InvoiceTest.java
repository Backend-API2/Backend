package backend_api.Backend.Entity;

import backend_api.Backend.Entity.invoice.Invoice;
import backend_api.Backend.Entity.invoice.InvoiceLine;
import backend_api.Backend.Entity.invoice.InvoiceStatus;
import backend_api.Backend.Entity.invoice.InvoiceType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class InvoiceTest {

    private Invoice invoice;

    @BeforeEach
    void setUp() {
        invoice = new Invoice();
    }

    @Test
    void testDefaultConstructor() {
        assertNotNull(invoice);
        assertNull(invoice.getId());
        assertNull(invoice.getPaymentId());
        assertNull(invoice.getInvoiceNumber());
        assertNull(invoice.getIssueDate());
        assertNull(invoice.getDueDate());
        assertNull(invoice.getTotalAmount());
        assertNull(invoice.getSubtotalAmount());
        assertNull(invoice.getTaxAmount());
        assertNull(invoice.getDiscountAmount());
        assertNull(invoice.getStatus());
        assertNull(invoice.getType());
        assertNull(invoice.getUserId());
        assertNull(invoice.getProviderId());
        assertNull(invoice.getCurrency());
        assertNull(invoice.getLegalFields());
        assertNull(invoice.getPdfUrl());
        assertNull(invoice.getNotes());
        assertNull(invoice.getCreatedAt());
        assertNull(invoice.getUpdatedAt());
        assertNull(invoice.getSentAt());
        assertNull(invoice.getPaidAt());
        assertNull(invoice.getMetadata());
    }

    @Test
    void testSettersAndGetters() {
        // Test basic setters and getters
        invoice.setId(1L);
        invoice.setPaymentId(100L);
        invoice.setInvoiceNumber("INV-001");
        LocalDateTime now = LocalDateTime.now();
        invoice.setIssueDate(now);
        invoice.setDueDate(now.plusDays(30));
        invoice.setTotalAmount(BigDecimal.valueOf(100.00));
        invoice.setSubtotalAmount(BigDecimal.valueOf(80.00));
        invoice.setTaxAmount(BigDecimal.valueOf(20.00));
        invoice.setDiscountAmount(BigDecimal.valueOf(10.00));
        invoice.setStatus(InvoiceStatus.PAID);
        invoice.setType(InvoiceType.STANDARD);
        invoice.setUserId(200L);
        invoice.setProviderId(300L);
        invoice.setCurrency("USD");
        invoice.setLegalFields("Legal terms and conditions");
        invoice.setPdfUrl("https://example.com/invoice.pdf");
        invoice.setNotes("Payment notes");
        invoice.setCreatedAt(now);
        invoice.setUpdatedAt(now);
        invoice.setSentAt(now);
        invoice.setPaidAt(now);
        invoice.setMetadata("{\"key\": \"value\"}");

        assertEquals(1L, invoice.getId());
        assertEquals(100L, invoice.getPaymentId());
        assertEquals("INV-001", invoice.getInvoiceNumber());
        assertEquals(now, invoice.getIssueDate());
        assertEquals(now.plusDays(30), invoice.getDueDate());
        assertEquals(BigDecimal.valueOf(100.00), invoice.getTotalAmount());
        assertEquals(BigDecimal.valueOf(80.00), invoice.getSubtotalAmount());
        assertEquals(BigDecimal.valueOf(20.00), invoice.getTaxAmount());
        assertEquals(BigDecimal.valueOf(10.00), invoice.getDiscountAmount());
        assertEquals(InvoiceStatus.PAID, invoice.getStatus());
        assertEquals(InvoiceType.STANDARD, invoice.getType());
        assertEquals(200L, invoice.getUserId());
        assertEquals(300L, invoice.getProviderId());
        assertEquals("USD", invoice.getCurrency());
        assertEquals("Legal terms and conditions", invoice.getLegalFields());
        assertEquals("https://example.com/invoice.pdf", invoice.getPdfUrl());
        assertEquals("Payment notes", invoice.getNotes());
        assertEquals(now, invoice.getCreatedAt());
        assertEquals(now, invoice.getUpdatedAt());
        assertEquals(now, invoice.getSentAt());
        assertEquals(now, invoice.getPaidAt());
        assertEquals("{\"key\": \"value\"}", invoice.getMetadata());
    }

    @Test
    void testOnCreate_SetsDefaults() {
        // Verify initial state
        assertNull(invoice.getCreatedAt());
        assertNull(invoice.getStatus());
        assertNull(invoice.getType());

        // Call onCreate (simulating @PrePersist) - using reflection to access protected method
        try {
            java.lang.reflect.Method onCreateMethod = Invoice.class.getDeclaredMethod("onCreate");
            onCreateMethod.setAccessible(true);
            onCreateMethod.invoke(invoice);
        } catch (Exception e) {
            fail("Failed to call onCreate method: " + e.getMessage());
        }

        // Verify defaults are set
        assertNotNull(invoice.getCreatedAt());
        assertEquals(InvoiceStatus.DRAFT, invoice.getStatus());
        assertEquals(InvoiceType.STANDARD, invoice.getType());
        assertTrue(invoice.getCreatedAt().isBefore(LocalDateTime.now().plusSeconds(1)));
        assertTrue(invoice.getCreatedAt().isAfter(LocalDateTime.now().minusSeconds(1)));
    }

    @Test
    void testOnCreate_WithExistingStatusAndType() {
        // Set existing status and type
        invoice.setStatus(InvoiceStatus.PAID);
        invoice.setType(InvoiceType.CREDIT);

        // Call onCreate - using reflection to access protected method
        try {
            java.lang.reflect.Method onCreateMethod = Invoice.class.getDeclaredMethod("onCreate");
            onCreateMethod.setAccessible(true);
            onCreateMethod.invoke(invoice);
        } catch (Exception e) {
            fail("Failed to call onCreate method: " + e.getMessage());
        }

        // Verify existing values are not changed
        assertEquals(InvoiceStatus.PAID, invoice.getStatus());
        assertEquals(InvoiceType.CREDIT, invoice.getType());
        assertNotNull(invoice.getCreatedAt());
    }

    @Test
    void testOnUpdate_SetsUpdatedAt() {
        // Set initial timestamp
        LocalDateTime initialTime = LocalDateTime.now().minusHours(1);
        invoice.setCreatedAt(initialTime);
        invoice.setUpdatedAt(initialTime);

        // Call onUpdate (simulating @PreUpdate) - using reflection to access protected method
        try {
            java.lang.reflect.Method onUpdateMethod = Invoice.class.getDeclaredMethod("onUpdate");
            onUpdateMethod.setAccessible(true);
            onUpdateMethod.invoke(invoice);
        } catch (Exception e) {
            fail("Failed to call onUpdate method: " + e.getMessage());
        }

        // Verify updated_at is changed but created_at remains the same
        assertEquals(initialTime, invoice.getCreatedAt());
        assertNotEquals(initialTime, invoice.getUpdatedAt());
        assertTrue(invoice.getUpdatedAt().isAfter(initialTime));
        assertTrue(invoice.getUpdatedAt().isBefore(LocalDateTime.now().plusSeconds(1)));
    }

    @Test
    void testInvoiceStatusEnumValues() {
        // Test all enum values
        invoice.setStatus(InvoiceStatus.DRAFT);
        assertEquals(InvoiceStatus.DRAFT, invoice.getStatus());

        invoice.setStatus(InvoiceStatus.SENT);
        assertEquals(InvoiceStatus.SENT, invoice.getStatus());

        invoice.setStatus(InvoiceStatus.PAID);
        assertEquals(InvoiceStatus.PAID, invoice.getStatus());

        invoice.setStatus(InvoiceStatus.OVERDUE);
        assertEquals(InvoiceStatus.OVERDUE, invoice.getStatus());

        invoice.setStatus(InvoiceStatus.CANCELED);
        assertEquals(InvoiceStatus.CANCELED, invoice.getStatus());

        invoice.setStatus(InvoiceStatus.OVERDUE);
        assertEquals(InvoiceStatus.OVERDUE, invoice.getStatus());
    }

    @Test
    void testInvoiceTypeEnumValues() {
        // Test all enum values
        invoice.setType(InvoiceType.STANDARD);
        assertEquals(InvoiceType.STANDARD, invoice.getType());

        invoice.setType(InvoiceType.CREDIT);
        assertEquals(InvoiceType.CREDIT, invoice.getType());

        invoice.setType(InvoiceType.DEBIT);
        assertEquals(InvoiceType.DEBIT, invoice.getType());

        invoice.setType(InvoiceType.PROFORMA);
        assertEquals(InvoiceType.PROFORMA, invoice.getType());

        invoice.setType(InvoiceType.RECURRING);
        assertEquals(InvoiceType.RECURRING, invoice.getType());
    }

    @Test
    void testAmountPrecision() {
        // Test BigDecimal precision
        invoice.setTotalAmount(BigDecimal.valueOf(123.45));
        assertEquals(BigDecimal.valueOf(123.45), invoice.getTotalAmount());

        invoice.setSubtotalAmount(BigDecimal.valueOf(100.00));
        assertEquals(BigDecimal.valueOf(100.00), invoice.getSubtotalAmount());

        invoice.setTaxAmount(BigDecimal.valueOf(23.45));
        assertEquals(BigDecimal.valueOf(23.45), invoice.getTaxAmount());

        invoice.setDiscountAmount(BigDecimal.valueOf(10.00));
        assertEquals(BigDecimal.valueOf(10.00), invoice.getDiscountAmount());

        // Test null amounts
        invoice.setTotalAmount(null);
        assertNull(invoice.getTotalAmount());

        invoice.setSubtotalAmount(null);
        assertNull(invoice.getSubtotalAmount());

        invoice.setTaxAmount(null);
        assertNull(invoice.getTaxAmount());

        invoice.setDiscountAmount(null);
        assertNull(invoice.getDiscountAmount());
    }

    @Test
    void testCurrencyCodes() {
        // Test various currency codes
        invoice.setCurrency("USD");
        assertEquals("USD", invoice.getCurrency());

        invoice.setCurrency("EUR");
        assertEquals("EUR", invoice.getCurrency());

        invoice.setCurrency("GBP");
        assertEquals("GBP", invoice.getCurrency());

        invoice.setCurrency("JPY");
        assertEquals("JPY", invoice.getCurrency());

        invoice.setCurrency("CAD");
        assertEquals("CAD", invoice.getCurrency());

        // Test null currency
        invoice.setCurrency(null);
        assertNull(invoice.getCurrency());

        // Test empty currency
        invoice.setCurrency("");
        assertEquals("", invoice.getCurrency());
    }

    @Test
    void testInvoiceNumberFormats() {
        // Test various invoice number formats
        invoice.setInvoiceNumber("INV-001");
        assertEquals("INV-001", invoice.getInvoiceNumber());

        invoice.setInvoiceNumber("2024-001");
        assertEquals("2024-001", invoice.getInvoiceNumber());

        invoice.setInvoiceNumber("INV-2024-001");
        assertEquals("INV-2024-001", invoice.getInvoiceNumber());

        invoice.setInvoiceNumber("123456");
        assertEquals("123456", invoice.getInvoiceNumber());

        // Test null invoice number
        invoice.setInvoiceNumber(null);
        assertNull(invoice.getInvoiceNumber());

        // Test empty invoice number
        invoice.setInvoiceNumber("");
        assertEquals("", invoice.getInvoiceNumber());
    }

    @Test
    void testDateFields() {
        LocalDateTime now = LocalDateTime.now();

        // Test issue date
        invoice.setIssueDate(now);
        assertEquals(now, invoice.getIssueDate());

        // Test due date
        invoice.setDueDate(now.plusDays(30));
        assertEquals(now.plusDays(30), invoice.getDueDate());

        // Test sent date
        invoice.setSentAt(now.plusDays(1));
        assertEquals(now.plusDays(1), invoice.getSentAt());

        // Test paid date
        invoice.setPaidAt(now.plusDays(2));
        assertEquals(now.plusDays(2), invoice.getPaidAt());

        // Test null dates
        invoice.setIssueDate(null);
        assertNull(invoice.getIssueDate());

        invoice.setDueDate(null);
        assertNull(invoice.getDueDate());

        invoice.setSentAt(null);
        assertNull(invoice.getSentAt());

        invoice.setPaidAt(null);
        assertNull(invoice.getPaidAt());
    }

    @Test
    void testUrlFields() {
        // Test PDF URL
        invoice.setPdfUrl("https://example.com/invoice.pdf");
        assertEquals("https://example.com/invoice.pdf", invoice.getPdfUrl());

        invoice.setPdfUrl("http://localhost:8080/invoices/123.pdf");
        assertEquals("http://localhost:8080/invoices/123.pdf", invoice.getPdfUrl());

        // Test null URL
        invoice.setPdfUrl(null);
        assertNull(invoice.getPdfUrl());

        // Test empty URL
        invoice.setPdfUrl("");
        assertEquals("", invoice.getPdfUrl());
    }

    @Test
    void testTextFields() {
        // Test legal fields
        invoice.setLegalFields("Terms and conditions apply");
        assertEquals("Terms and conditions apply", invoice.getLegalFields());

        // Test notes
        invoice.setNotes("Payment received via credit card");
        assertEquals("Payment received via credit card", invoice.getNotes());

        // Test metadata JSON
        invoice.setMetadata("{\"payment_method\": \"credit_card\", \"gateway\": \"stripe\"}");
        assertEquals("{\"payment_method\": \"credit_card\", \"gateway\": \"stripe\"}", invoice.getMetadata());

        // Test null text fields
        invoice.setLegalFields(null);
        assertNull(invoice.getLegalFields());

        invoice.setNotes(null);
        assertNull(invoice.getNotes());

        invoice.setMetadata(null);
        assertNull(invoice.getMetadata());
    }

    @Test
    void testToString() {
        invoice.setId(1L);
        invoice.setInvoiceNumber("INV-001");
        invoice.setStatus(InvoiceStatus.DRAFT);
        invoice.setType(InvoiceType.STANDARD);

        String result = invoice.toString();

        assertNotNull(result);
        assertTrue(result.contains("Invoice"));
        assertTrue(result.contains("id=1"));
        assertTrue(result.contains("invoiceNumber=INV-001"));
        assertTrue(result.contains("status=DRAFT"));
        assertTrue(result.contains("type=STANDARD"));
    }

    @Test
    void testEqualsAndHashCode() {
        Invoice invoice1 = new Invoice();
        invoice1.setId(1L);
        invoice1.setInvoiceNumber("INV-001");
        invoice1.setStatus(InvoiceStatus.DRAFT);

        Invoice invoice2 = new Invoice();
        invoice2.setId(1L);
        invoice2.setInvoiceNumber("INV-001");
        invoice2.setStatus(InvoiceStatus.DRAFT);

        Invoice invoice3 = new Invoice();
        invoice3.setId(2L);
        invoice3.setInvoiceNumber("INV-002");
        invoice3.setStatus(InvoiceStatus.PAID);

        // Test equals
        assertEquals(invoice1, invoice2);
        assertNotEquals(invoice1, invoice3);
        assertNotEquals(invoice2, invoice3);

        // Test hashCode
        assertEquals(invoice1.hashCode(), invoice2.hashCode());
        assertNotEquals(invoice1.hashCode(), invoice3.hashCode());
    }

    @Test
    void testCompleteInvoiceWorkflow() {
        // Test a complete invoice workflow
        invoice.setId(1L);
        invoice.setPaymentId(123L);
        invoice.setInvoiceNumber("INV-2024-001");
        invoice.setIssueDate(LocalDateTime.now());
        invoice.setDueDate(LocalDateTime.now().plusDays(30));
        invoice.setTotalAmount(BigDecimal.valueOf(150.00));
        invoice.setSubtotalAmount(BigDecimal.valueOf(125.00));
        invoice.setTaxAmount(BigDecimal.valueOf(25.00));
        invoice.setDiscountAmount(BigDecimal.valueOf(5.00));
        invoice.setStatus(InvoiceStatus.DRAFT);
        invoice.setType(InvoiceType.STANDARD);
        invoice.setUserId(456L);
        invoice.setProviderId(789L);
        invoice.setCurrency("USD");
        invoice.setLegalFields("Standard terms and conditions");
        invoice.setNotes("Initial invoice creation");
        invoice.setMetadata("{\"created_by\": \"system\"}");

        // Simulate prePersist - using reflection to access protected method
        try {
            java.lang.reflect.Method onCreateMethod = Invoice.class.getDeclaredMethod("onCreate");
            onCreateMethod.setAccessible(true);
            onCreateMethod.invoke(invoice);
        } catch (Exception e) {
            fail("Failed to call onCreate method: " + e.getMessage());
        }

        // Verify initial state
        assertEquals(InvoiceStatus.DRAFT, invoice.getStatus());
        assertEquals(InvoiceType.STANDARD, invoice.getType());
        assertNotNull(invoice.getCreatedAt());

        // Simulate sending invoice
        invoice.setStatus(InvoiceStatus.SENT);
        invoice.setSentAt(LocalDateTime.now());
        invoice.setNotes("Invoice sent to customer");

        // Simulate preUpdate - using reflection to access protected method
        try {
            java.lang.reflect.Method onUpdateMethod = Invoice.class.getDeclaredMethod("onUpdate");
            onUpdateMethod.setAccessible(true);
            onUpdateMethod.invoke(invoice);
        } catch (Exception e) {
            fail("Failed to call onUpdate method: " + e.getMessage());
        }

        // Verify updated state
        assertEquals(InvoiceStatus.SENT, invoice.getStatus());
        assertNotNull(invoice.getSentAt());
        assertEquals("Invoice sent to customer", invoice.getNotes());
        assertTrue(invoice.getUpdatedAt().isAfter(invoice.getCreatedAt()));

        // Simulate payment
        invoice.setStatus(InvoiceStatus.PAID);
        invoice.setPaidAt(LocalDateTime.now());
        invoice.setNotes("Payment received");

        // Simulate preUpdate - using reflection to access protected method
        try {
            java.lang.reflect.Method onUpdateMethod = Invoice.class.getDeclaredMethod("onUpdate");
            onUpdateMethod.setAccessible(true);
            onUpdateMethod.invoke(invoice);
        } catch (Exception e) {
            fail("Failed to call onUpdate method: " + e.getMessage());
        }

        // Verify final state
        assertEquals(InvoiceStatus.PAID, invoice.getStatus());
        assertNotNull(invoice.getPaidAt());
        assertEquals("Payment received", invoice.getNotes());
    }

}
