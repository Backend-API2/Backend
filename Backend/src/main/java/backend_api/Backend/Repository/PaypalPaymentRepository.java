package backend_api.Backend.Repository;

import backend_api.Backend.Entity.payment.types.PaypalPayment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PaypalPaymentRepository extends JpaRepository<PaypalPayment, Long> {
}
