package backend_api.Backend.Service.Implementation;

import backend_api.Backend.Entity.UserData;
import backend_api.Backend.Repository.UserDataRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserDataAuthService {

    private final UserDataRepository userDataRepository;

    public Optional<UserData> authenticateUser(String email, String password) {
        try {
            Optional<UserData> userDataOpt = userDataRepository.findByEmail(email);
            
            if (userDataOpt.isPresent()) {
                UserData userData = userDataOpt.get();
                log.info("Usuario encontrado en user_data: {}", email);
                
                
                return userDataOpt;
            }
            
            log.warn("Usuario no encontrado: {}", email);
            return Optional.empty();
            
        } catch (Exception e) {
            log.error("Error autenticando usuario: {}", e.getMessage());
            return Optional.empty();
        }
    }

    public Optional<UserData> findUserByEmail(String email) {
        return userDataRepository.findByEmail(email);
    }

    public Optional<UserData> findUserById(Long userId) {
        return userDataRepository.findByUserId(userId);
    }
}
