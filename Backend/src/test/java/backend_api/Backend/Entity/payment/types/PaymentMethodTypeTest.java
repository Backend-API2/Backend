package backend_api.Backend.Entity.payment.types;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PaymentMethodTypeTest {
    @Test
    void testEnumValues() {
        assertEquals(PaymentMethodType.CREDIT_CARD, PaymentMethodType.valueOf("CREDIT_CARD"));
        assertEquals(PaymentMethodType.PAYPAL, PaymentMethodType.valueOf("PAYPAL"));
        assertEquals(7, PaymentMethodType.values().length);
    }
}
