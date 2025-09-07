package backend_api.Backend.Repository;

import backend_api.Backend.Entity.payment.types.CreditCardPayment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CreditCardPaymentRepository extends JpaRepository<CreditCardPayment, Long> {
}
