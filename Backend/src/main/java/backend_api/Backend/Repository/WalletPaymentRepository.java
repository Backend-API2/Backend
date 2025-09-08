package backend_api.Backend.Repository;

import backend_api.Backend.Entity.payment.types.WalletPayment;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface WalletPaymentRepository extends JpaRepository<WalletPayment, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT w FROM WalletPayment w WHERE w.id = :id")
    Optional<WalletPayment> findByIdForUpdate(@Param("id") Long id);

    @Modifying
    @Query("UPDATE WalletPayment w SET w.availableBalance = w.availableBalance - :amount " +
            "WHERE w.id = :id AND w.currency = :currency AND w.availableBalance >= :amount")
    int debitIfEnough(@Param("id") Long id, @Param("currency") String currency, @Param("amount") java.math.BigDecimal amount);

    @Modifying
    @Query("UPDATE WalletPayment w SET w.availableBalance = w.availableBalance + :amount " +
            "WHERE w.id = :id AND w.currency = :currency")
    int credit(@Param("id") Long id, @Param("currency") String currency, @Param("amount") java.math.BigDecimal amount);
}