package backend_api.Backend.Entity.payment;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PaymentEventTypeTest {
    @Test
    void testEnumValues() {
        assertEquals(PaymentEventType.AUTHORIZATION_STARTED, PaymentEventType.valueOf("AUTHORIZATION_STARTED"));
        assertEquals(PaymentEventType.PAYMENT_APPROVED, PaymentEventType.valueOf("PAYMENT_APPROVED"));
        assertTrue(PaymentEventType.values().length > 10);
    }
}
