package backend_api.Backend.Repository;

import backend_api.Backend.Entity.payment.types.BankTransferPayment;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BankTransferPaymentRepository extends JpaRepository<BankTransferPayment, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT b FROM BankTransferPayment b WHERE b.id = :id")
    Optional<BankTransferPayment> findByIdForUpdate(@Param("id") Long id);

    @Modifying
    @Query("UPDATE BankTransferPayment b SET b.availableBalance = b.availableBalance - :amount " +
            "WHERE b.id = :id AND b.currency = :currency AND b.availableBalance >= :amount")
    int debitIfEnough(@Param("id") Long id, @Param("currency") String currency, @Param("amount") java.math.BigDecimal amount);

    @Modifying
    @Query("UPDATE BankTransferPayment b SET b.availableBalance = b.availableBalance + :amount " +
            "WHERE b.id = :id AND b.currency = :currency")
    int credit(@Param("id") Long id, @Param("currency") String currency, @Param("amount") java.math.BigDecimal amount);
}