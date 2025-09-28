package backend_api.Backend.Entity.invoice;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class InvoiceStatusTest {
    @Test
    void testEnumValues() {
        assertEquals(InvoiceStatus.DRAFT, InvoiceStatus.valueOf("DRAFT"));
        assertEquals(InvoiceStatus.PAID, InvoiceStatus.valueOf("PAID"));
        assertEquals(8, InvoiceStatus.values().length);
    }
}
