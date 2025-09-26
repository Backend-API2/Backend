package backend_api.Backend.Entity;

import backend_api.Backend.Entity.invoice.InvoiceEvent;
import backend_api.Backend.Entity.invoice.InvoiceEventType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class InvoiceEventTest {

    private InvoiceEvent invoiceEvent;

    @BeforeEach
    void setUp() {
        invoiceEvent = new InvoiceEvent();
    }

    @Test
    void testDefaultConstructor() {
        assertNotNull(invoiceEvent);
        assertNull(invoiceEvent.getId());
        assertNull(invoiceEvent.getInvoiceId());
        assertNull(invoiceEvent.getEventType());
        assertNull(invoiceEvent.getDescription());
        assertNull(invoiceEvent.getCreatedBy());
        assertNull(invoiceEvent.getCreatedAt());
        assertNull(invoiceEvent.getEventData());
        assertNull(invoiceEvent.getIpAddress());
        assertNull(invoiceEvent.getUserAgent());
    }

    @Test
    void testSettersAndGetters() {
        // Test basic setters and getters
        invoiceEvent.setId(1L);
        invoiceEvent.setInvoiceId(100L);
        invoiceEvent.setEventType(InvoiceEventType.INVOICE_CREATED);
        invoiceEvent.setDescription("Invoice created successfully");
        invoiceEvent.setCreatedBy(200L);
        LocalDateTime now = LocalDateTime.now();
        invoiceEvent.setCreatedAt(now);
        invoiceEvent.setEventData("{\"amount\": 100.00, \"currency\": \"USD\"}");
        invoiceEvent.setIpAddress("192.168.1.1");
        invoiceEvent.setUserAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36");

        assertEquals(1L, invoiceEvent.getId());
        assertEquals(100L, invoiceEvent.getInvoiceId());
        assertEquals(InvoiceEventType.INVOICE_CREATED, invoiceEvent.getEventType());
        assertEquals("Invoice created successfully", invoiceEvent.getDescription());
        assertEquals(200L, invoiceEvent.getCreatedBy());
        assertEquals(now, invoiceEvent.getCreatedAt());
        assertEquals("{\"amount\": 100.00, \"currency\": \"USD\"}", invoiceEvent.getEventData());
        assertEquals("192.168.1.1", invoiceEvent.getIpAddress());
        assertEquals("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36", invoiceEvent.getUserAgent());
    }

    @Test
    void testOnCreate_SetsCreatedAt() {
        // Verify createdAt is null initially
        assertNull(invoiceEvent.getCreatedAt());

        // Call onCreate (simulating @PrePersist) - using reflection to access protected method
        try {
            java.lang.reflect.Method onCreateMethod = InvoiceEvent.class.getDeclaredMethod("onCreate");
            onCreateMethod.setAccessible(true);
            onCreateMethod.invoke(invoiceEvent);
        } catch (Exception e) {
            fail("Failed to call onCreate method: " + e.getMessage());
        }

        // Verify createdAt is set
        assertNotNull(invoiceEvent.getCreatedAt());
        assertTrue(invoiceEvent.getCreatedAt().isBefore(LocalDateTime.now().plusSeconds(1)));
        assertTrue(invoiceEvent.getCreatedAt().isAfter(LocalDateTime.now().minusSeconds(1)));
    }

    @Test
    void testOnCreate_MultipleCalls() {
        // Call onCreate multiple times - using reflection to access protected method
        try {
            java.lang.reflect.Method onCreateMethod = InvoiceEvent.class.getDeclaredMethod("onCreate");
            onCreateMethod.setAccessible(true);
            onCreateMethod.invoke(invoiceEvent);
        } catch (Exception e) {
            fail("Failed to call onCreate method: " + e.getMessage());
        }
        LocalDateTime firstCall = invoiceEvent.getCreatedAt();
        
        // Wait a small amount of time
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        try {
            java.lang.reflect.Method onCreateMethod = InvoiceEvent.class.getDeclaredMethod("onCreate");
            onCreateMethod.setAccessible(true);
            onCreateMethod.invoke(invoiceEvent);
        } catch (Exception e) {
            fail("Failed to call onCreate method: " + e.getMessage());
        }
        LocalDateTime secondCall = invoiceEvent.getCreatedAt();

        // Verify both calls set the timestamp
        assertNotNull(firstCall);
        assertNotNull(secondCall);
        assertTrue(secondCall.isAfter(firstCall));
    }

    @Test
    void testEventTypeEnumValues() {
        // Test all enum values
        invoiceEvent.setEventType(InvoiceEventType.INVOICE_CREATED);
        assertEquals(InvoiceEventType.INVOICE_CREATED, invoiceEvent.getEventType());

        invoiceEvent.setEventType(InvoiceEventType.INVOICE_SENT);
        assertEquals(InvoiceEventType.INVOICE_SENT, invoiceEvent.getEventType());

        invoiceEvent.setEventType(InvoiceEventType.INVOICE_VIEWED);
        assertEquals(InvoiceEventType.INVOICE_VIEWED, invoiceEvent.getEventType());

        invoiceEvent.setEventType(InvoiceEventType.PAYMENT_COMPLETED);
        assertEquals(InvoiceEventType.PAYMENT_COMPLETED, invoiceEvent.getEventType());

        invoiceEvent.setEventType(InvoiceEventType.INVOICE_OVERDUE);
        assertEquals(InvoiceEventType.INVOICE_OVERDUE, invoiceEvent.getEventType());

        invoiceEvent.setEventType(InvoiceEventType.INVOICE_CANCELED);
        assertEquals(InvoiceEventType.INVOICE_CANCELED, invoiceEvent.getEventType());

        invoiceEvent.setEventType(InvoiceEventType.INVOICE_UPDATED);
        assertEquals(InvoiceEventType.INVOICE_UPDATED, invoiceEvent.getEventType());

        invoiceEvent.setEventType(InvoiceEventType.INVOICE_DELETED);
        assertEquals(InvoiceEventType.INVOICE_DELETED, invoiceEvent.getEventType());
    }

    @Test
    void testEventDataJsonFormat() {
        // Test JSON event data
        String jsonData = "{\"amount\": 150.50, \"currency\": \"EUR\", \"payment_method\": \"credit_card\"}";
        invoiceEvent.setEventData(jsonData);
        assertEquals(jsonData, invoiceEvent.getEventData());

        // Test null event data
        invoiceEvent.setEventData(null);
        assertNull(invoiceEvent.getEventData());

        // Test empty event data
        invoiceEvent.setEventData("");
        assertEquals("", invoiceEvent.getEventData());
    }

    @Test
    void testIpAddressFormats() {
        // Test IPv4 address
        invoiceEvent.setIpAddress("192.168.1.100");
        assertEquals("192.168.1.100", invoiceEvent.getIpAddress());

        // Test IPv6 address
        invoiceEvent.setIpAddress("2001:0db8:85a3:0000:0000:8a2e:0370:7334");
        assertEquals("2001:0db8:85a3:0000:0000:8a2e:0370:7334", invoiceEvent.getIpAddress());

        // Test localhost
        invoiceEvent.setIpAddress("127.0.0.1");
        assertEquals("127.0.0.1", invoiceEvent.getIpAddress());

        // Test null IP address
        invoiceEvent.setIpAddress(null);
        assertNull(invoiceEvent.getIpAddress());
    }

    @Test
    void testUserAgentStrings() {
        // Test Chrome user agent
        String chromeUA = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36";
        invoiceEvent.setUserAgent(chromeUA);
        assertEquals(chromeUA, invoiceEvent.getUserAgent());

        // Test Firefox user agent
        String firefoxUA = "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:89.0) Gecko/20100101 Firefox/89.0";
        invoiceEvent.setUserAgent(firefoxUA);
        assertEquals(firefoxUA, invoiceEvent.getUserAgent());

        // Test mobile user agent
        String mobileUA = "Mozilla/5.0 (iPhone; CPU iPhone OS 14_6 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/14.0 Mobile/15E148 Safari/604.1";
        invoiceEvent.setUserAgent(mobileUA);
        assertEquals(mobileUA, invoiceEvent.getUserAgent());

        // Test null user agent
        invoiceEvent.setUserAgent(null);
        assertNull(invoiceEvent.getUserAgent());
    }

    @Test
    void testToString() {
        invoiceEvent.setId(1L);
        invoiceEvent.setInvoiceId(100L);
        invoiceEvent.setEventType(InvoiceEventType.INVOICE_CREATED);
        invoiceEvent.setDescription("Test event");

        String result = invoiceEvent.toString();

        assertNotNull(result);
        assertTrue(result.contains("InvoiceEvent"));
        assertTrue(result.contains("id=1"));
        assertTrue(result.contains("invoiceId=100"));
        assertTrue(result.contains("eventType=INVOICE_CREATED"));
        assertTrue(result.contains("description=Test event"));
    }

    @Test
    void testEqualsAndHashCode() {
        InvoiceEvent event1 = new InvoiceEvent();
        event1.setId(1L);
        event1.setInvoiceId(100L);
        event1.setEventType(InvoiceEventType.INVOICE_CREATED);

        InvoiceEvent event2 = new InvoiceEvent();
        event2.setId(1L);
        event2.setInvoiceId(100L);
        event2.setEventType(InvoiceEventType.INVOICE_CREATED);

        InvoiceEvent event3 = new InvoiceEvent();
        event3.setId(2L);
        event3.setInvoiceId(200L);
        event3.setEventType(InvoiceEventType.INVOICE_SENT);

        // Test equals
        assertEquals(event1, event2);
        assertNotEquals(event1, event3);
        assertNotEquals(event2, event3);

        // Test hashCode
        assertEquals(event1.hashCode(), event2.hashCode());
        assertNotEquals(event1.hashCode(), event3.hashCode());
    }

    @Test
    void testCompleteInvoiceEvent() {
        // Test a complete invoice event with all fields
        invoiceEvent.setId(1L);
        invoiceEvent.setInvoiceId(123L);
        invoiceEvent.setEventType(InvoiceEventType.PAYMENT_COMPLETED);
        invoiceEvent.setDescription("Payment received via credit card");
        invoiceEvent.setCreatedBy(456L);
        invoiceEvent.setEventData("{\"payment_id\": 789, \"amount\": 250.00, \"method\": \"credit_card\"}");
        invoiceEvent.setIpAddress("203.0.113.1");
        invoiceEvent.setUserAgent("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36");

        // Verify all fields are set correctly
        assertEquals(1L, invoiceEvent.getId());
        assertEquals(123L, invoiceEvent.getInvoiceId());
        assertEquals(InvoiceEventType.PAYMENT_COMPLETED, invoiceEvent.getEventType());
        assertEquals("Payment received via credit card", invoiceEvent.getDescription());
        assertEquals(456L, invoiceEvent.getCreatedBy());
        assertEquals("{\"payment_id\": 789, \"amount\": 250.00, \"method\": \"credit_card\"}", invoiceEvent.getEventData());
        assertEquals("203.0.113.1", invoiceEvent.getIpAddress());
        assertEquals("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36", invoiceEvent.getUserAgent());

        // Call onCreate to set timestamp - using reflection to access protected method
        try {
            java.lang.reflect.Method onCreateMethod = InvoiceEvent.class.getDeclaredMethod("onCreate");
            onCreateMethod.setAccessible(true);
            onCreateMethod.invoke(invoiceEvent);
        } catch (Exception e) {
            fail("Failed to call onCreate method: " + e.getMessage());
        }
        assertNotNull(invoiceEvent.getCreatedAt());
    }
}
