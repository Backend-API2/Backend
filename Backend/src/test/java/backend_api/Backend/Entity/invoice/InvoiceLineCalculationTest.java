package backend_api.Backend.Entity;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.math.RoundingMode;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class InvoiceLineCalculationTest {

    @Test
    void testTotals_onlyTax_noDiscounts() {
        InvoiceLine line = new InvoiceLine();
        line.setUnitPrice(new BigDecimal("100.00"));
        line.setQuantity(3);
        line.setTaxRate(new BigDecimal("21"));
        line.setDiscountRate(null);
        line.setDiscountAmount(null);

        BigDecimal subtotal = new BigDecimal("300.00");
        BigDecimal tax = subtotal.multiply(new BigDecimal("0.21")).setScale(2, RoundingMode.HALF_UP);
        BigDecimal expectedTotal = subtotal.add(tax).setScale(2, RoundingMode.HALF_UP);

        assertEquals(subtotal, line.getSubtotal());
        assertEquals(tax, line.getTaxAmount());
        assertEquals(expectedTotal, line.getTotalAmount());
    }

    @Test
    void testTotals_onlyPercentDiscount_noTax() {
        InvoiceLine line = new InvoiceLine();
        line.setUnitPrice(new BigDecimal("250.00"));
        line.setQuantity(2);
        line.setDiscountRate(new BigDecimal("15"));
        line.setTaxRate(null);
        line.setDiscountAmount(null);

        BigDecimal subtotal = new BigDecimal("500.00");
        BigDecimal discount = subtotal.multiply(new BigDecimal("0.15")).setScale(2, RoundingMode.HALF_UP);
        BigDecimal expectedTotal = subtotal.subtract(discount).setScale(2, RoundingMode.HALF_UP);

        assertEquals(subtotal, line.getSubtotal());
        assertEquals(discount, line.getDiscountAmount());
        assertEquals(expectedTotal, line.getTotalAmount());
    }

    @Test
    void testTotals_fixedAndPercentDiscount_plusTax() {
        InvoiceLine line = new InvoiceLine();
        line.setUnitPrice(new BigDecimal("80.00"));
        line.setQuantity(5);
        line.setDiscountAmount(new BigDecimal("30.00"));
        line.setDiscountRate(new BigDecimal("10"));
        line.setTaxRate(new BigDecimal("21"));

        BigDecimal subtotal = new BigDecimal("400.00");
        BigDecimal percentDisc = subtotal.multiply(new BigDecimal("0.10")).setScale(2, RoundingMode.HALF_UP); // 40.00
        BigDecimal totalDiscount = percentDisc.add(new BigDecimal("30.00")).setScale(2, RoundingMode.HALF_UP); // 70.00
        BigDecimal taxableBase = subtotal.subtract(totalDiscount).setScale(2, RoundingMode.HALF_UP); // 330.00
        BigDecimal tax = taxableBase.multiply(new BigDecimal("0.21")).setScale(2, RoundingMode.HALF_UP); // 69.30
        BigDecimal expectedTotal = taxableBase.add(tax).setScale(2, RoundingMode.HALF_UP); // 399.30

        assertEquals(subtotal, line.getSubtotal());
        assertEquals(totalDiscount, line.getDiscountAmount());
        assertEquals(tax, line.getTaxAmount());
        assertEquals(expectedTotal, line.getTotalAmount());
    }
}
