package backend_api.Backend.Repository;

import backend_api.Backend.Entity.UserData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserDataRepository extends JpaRepository<UserData, Long> {
    Optional<UserData> findByUserId(Long userId);
    Optional<UserData> findByEmail(String email);
    List<UserData> findAllByEmail(String email);
    boolean existsByUserId(Long userId);
    boolean existsByEmail(String email);
    
    // Método optimizado para batch queries
    List<UserData> findByUserIdIn(Iterable<Long> userIds);
}
