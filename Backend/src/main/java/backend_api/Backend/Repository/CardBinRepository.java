package backend_api.Backend.Repository;

import backend_api.Backend.Entity.payment.types.CardBin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CardBinRepository extends JpaRepository<CardBin, Long> {
    
    @Query("SELECT cb FROM CardBin cb WHERE cb.bin = :bin AND cb.isActive = true")
    Optional<CardBin> findByBinAndIsActiveTrue(@Param("bin") String bin);
    
    @Query("SELECT CASE WHEN COUNT(cb) > 0 THEN true ELSE false END FROM CardBin cb WHERE cb.bin = :bin AND cb.isActive = true")
    boolean existsByBinAndIsActiveTrue(@Param("bin") String bin);
}
