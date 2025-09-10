package backend_api.Backend.Repository;

import backend_api.Backend.Entity.payment.types.MercadoPagoPayment;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MercadoPagoPaymentRepository extends JpaRepository<MercadoPagoPayment, Long> {
}
