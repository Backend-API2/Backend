package backend_api.Backend.Repository;

import backend_api.Backend.Entity.payment.types.CashPayment;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CashPaymentRepository extends JpaRepository<CashPayment, Long> {
}
