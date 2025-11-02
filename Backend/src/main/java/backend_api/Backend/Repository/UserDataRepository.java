package backend_api.Backend.Repository;

import backend_api.Backend.Entity.UserData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserDataRepository extends JpaRepository<UserData, Long> {
    Optional<UserData> findByUserId(Long userId);
    Optional<UserData> findByEmail(String email);
    Optional<UserData> findFirstByEmail(String email); // Usar este para evitar NonUniqueResultException
    List<UserData> findAllByEmail(String email);
    boolean existsByUserId(Long userId);
    boolean existsByEmail(String email);
    
    // Método optimizado para batch queries
    List<UserData> findByUserIdIn(Iterable<Long> userIds);
    
    // Actualizar solo el campo active sin tocar el resto de los datos
    @Modifying
    @Query("UPDATE UserData u SET u.active = false WHERE u.userId = :userId")
    int deactivateByUserId(@Param("userId") Long userId);
    
    @Modifying
    @Query("UPDATE UserData u SET u.active = false WHERE u.email = :email")
    int deactivateByEmail(@Param("email") String email);
}
