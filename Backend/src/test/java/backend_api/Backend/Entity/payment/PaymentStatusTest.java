package backend_api.Backend.Entity.payment;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PaymentStatusTest {
    @Test
    void testEnumValues() {
        assertEquals(PaymentStatus.PENDING_APPROVAL, PaymentStatus.valueOf("PENDING_APPROVAL"));
        assertEquals(PaymentStatus.APPROVED, PaymentStatus.valueOf("APPROVED"));
        assertEquals(8, PaymentStatus.values().length);
    }
}
