package backend_api.Backend.Repository;

import backend_api.Backend.Entity.ProviderData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface ProviderDataRepository extends JpaRepository<ProviderData, Long> {
    Optional<ProviderData> findByProviderId(Long providerId);
    boolean existsByProviderId(Long providerId);
}
