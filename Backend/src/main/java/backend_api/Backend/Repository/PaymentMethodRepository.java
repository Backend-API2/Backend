package backend_api.Backend.Repository;

import backend_api.Backend.Entity.payment.PaymentMethod;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PaymentMethodRepository extends JpaRepository<PaymentMethod, Long> {
    // Métodos específicos para PaymentMethod si los necesitamos
    // Por ahora, JpaRepository nos da todos los métodos básicos (save, findById, deleteById, etc.)
}
