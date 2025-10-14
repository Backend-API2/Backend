package backend_api.Backend.Repository;

import backend_api.Backend.Entity.UserData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface UserDataRepository extends JpaRepository<UserData, Long> {
    Optional<UserData> findByUserId(Long userId);
    boolean existsByUserId(Long userId);
}
