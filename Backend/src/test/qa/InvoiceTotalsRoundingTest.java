package backend_api.Backend.qa;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.api.Assertions;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Verifica reglas de cálculo de total: subtotal + IVA - descuento, con HALF_UP (2 decimales).
 */
class InvoiceTotalsRoundingTest {

  private static BigDecimal calcTotal(BigDecimal subtotal, int ivaPct, int descPct) {
    BigDecimal iva  = subtotal.multiply(BigDecimal.valueOf(ivaPct)).divide(BigDecimal.valueOf(100), 6, RoundingMode.HALF_UP);
    BigDecimal desc = subtotal.multiply(BigDecimal.valueOf(descPct)).divide(BigDecimal.valueOf(100), 6, RoundingMode.HALF_UP);
    return subtotal.add(iva).subtract(desc).setScale(2, RoundingMode.HALF_UP);
  }

  @ParameterizedTest(name = "sub={0}, IVA={1}%, desc={2}% => total={3}")
  @CsvSource({
    "100.00,21,0,121.00",
    "100.10,21,10,109.11",
    "0.01,21,0,0.01",
    "999999999.99,21,0,1200000000.99",
    "199.995,21,0,241.99"   // borde de redondeo
  })
  void totalsWithRounding(String sub, int ivaPct, int descPct, String expected) {
    var out = calcTotal(new BigDecimal(sub), ivaPct, descPct);
    Assertions.assertEquals(new BigDecimal(expected), out);
  }
}
