package backend_api.Backend.Repository;

import backend_api.Backend.Entity.payment.types.TestCard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Optional;

@Repository
public interface TestCardRepository extends JpaRepository<TestCard, Long> {

    boolean existsByPanSha256AndCvvSha256AndIsActiveTrue(String panSha256, String cvvSha256);

    Optional<TestCard> findByPanSha256AndCvvSha256AndIsActiveTrue(String panSha256, String cvvSha256);

    // ✅ Debita si hay fondos suficientes (1 = ok, 0 = sin saldo/inhabilitada)
    @Modifying
    @Query("UPDATE TestCard t SET t.balance = t.balance - :amount " +
            "WHERE t.id = :id AND t.isActive = true AND t.balance >= :amount")
    int debitIfEnough(@Param("id") Long id, @Param("amount") BigDecimal amount);

    // ✅ Acredita (refund)
    @Modifying
    @Query("UPDATE TestCard t SET t.balance = t.balance + :amount " +
            "WHERE t.id = :id AND t.isActive = true")
    int credit(@Param("id") Long id, @Param("amount") BigDecimal amount);
}