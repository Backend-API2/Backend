package backend_api.Backend.qa;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.math.RoundingMode;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Asegura reglas: descuento tiene cap y el total nunca es negativo.
 */
class DiscountCapRulesTest {

  private BigDecimal applyDiscount(BigDecimal subtotal, BigDecimal discountPct, BigDecimal discountCap) {
    BigDecimal raw = subtotal.multiply(discountPct).divide(BigDecimal.valueOf(100), 6, RoundingMode.HALF_UP);
    BigDecimal applied = raw.min(discountCap);
    BigDecimal total = subtotal.subtract(applied).setScale(2, RoundingMode.HALF_UP);
    return total.max(BigDecimal.ZERO); // nunca negativo
  }

  @Test
  void respectsDiscountCap() {
    BigDecimal subtotal = new BigDecimal("1000.00");
    BigDecimal pct      = new BigDecimal("50");     // 50% serían 500
    BigDecimal cap      = new BigDecimal("300.00"); // cap aplica
    assertEquals(new BigDecimal("700.00"), applyDiscount(subtotal, pct, cap));
  }

  @Test
  void neverNegativeTotal() {
    BigDecimal subtotal = new BigDecimal("100.00");
    BigDecimal pct      = new BigDecimal("90");     // 90
    BigDecimal cap      = new BigDecimal("200.00"); // mayor al descuento
    assertEquals(new BigDecimal("10.00"), applyDiscount(subtotal, pct, cap));

    // si el cap fuese enorme y el cálculo crudo excede subtotal, total queda en 0
    BigDecimal pct2 = new BigDecimal("150"); // 150
    assertEquals(new BigDecimal("0.00"), applyDiscount(subtotal, pct2, new BigDecimal("9999")));
  }
}
