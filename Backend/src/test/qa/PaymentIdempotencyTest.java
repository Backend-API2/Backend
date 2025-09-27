package backend_api.Backend.qa;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import static org.junit.jupiter.api.Assertions.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

/**
 * Garantiza que, a igual requestId, no se reintenta el cargo innecesariamente.
 */
class PaymentIdempotencyTest {

  interface Gateway { String charge(String requestId); }

  static class PaymentService {
    private final Gateway gw;
    private final Map<String,String> cache = new ConcurrentHashMap<>();
    PaymentService(Gateway gw) { this.gw = gw; }
    String pay(String requestId) { return cache.computeIfAbsent(requestId, gw::charge); }
  }

  @Test
  void sameRequestId_returnsSamePayment_and_singleCharge() {
    Gateway gw = Mockito.mock(Gateway.class);
    Mockito.when(gw.charge("req-1")).thenReturn("pay_1");

    PaymentService svc = new PaymentService(gw);
    String a = svc.pay("req-1");
    String b = svc.pay("req-1");

    assertEquals("pay_1", a);
    assertEquals("pay_1", b);
    Mockito.verify(gw, Mockito.times(1)).charge("req-1");
  }
}
