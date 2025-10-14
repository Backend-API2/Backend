package backend_api.Backend.Repository;

import backend_api.Backend.Entity.SolicitudData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface SolicitudDataRepository extends JpaRepository<SolicitudData, Long> {
    Optional<SolicitudData> findBySolicitudId(Long solicitudId);
    boolean existsBySolicitudId(Long solicitudId);
}
