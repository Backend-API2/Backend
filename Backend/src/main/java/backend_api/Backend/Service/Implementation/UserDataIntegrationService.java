package backend_api.Backend.Service.Implementation;

import backend_api.Backend.Entity.UserData;
import backend_api.Backend.Entity.user.User;
import backend_api.Backend.Repository.UserDataRepository;
import backend_api.Backend.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserDataIntegrationService {

    private final UserRepository userRepository;
    private final UserDataRepository userDataRepository;

    public UserInfo getUserInfo(Long userId) {
        Optional<UserData> userDataOpt = userDataRepository.findByUserId(userId);
        
        if (userDataOpt.isPresent()) {
            UserData userData = userDataOpt.get();
            log.info("Usando datos del módulo de usuarios para userId: {}", userId);
            
            return UserInfo.builder()
                    .userId(userId)
                    .name(userData.getName())
                    .email(userData.getEmail())
                    .phone(userData.getPhone())
                    .source("USER_MODULE")
                    .build();
        }
        
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            log.info("Usando datos locales para userId: {}", userId);
            
            return UserInfo.builder()
                    .userId(userId)
                    .name(user.getName())
                    .email(user.getEmail())
                    .phone(user.getPhone())
                    .source("LOCAL")
                    .build();
        }
        
        log.warn("No se encontraron datos para userId: {}", userId);
        return null;
    }

    public boolean hasUserModuleData(Long userId) {
        return userDataRepository.existsByUserId(userId);
    }
    @lombok.Data
    @lombok.Builder
    public static class UserInfo {
        private Long userId;
        private String name;
        private String email;
        private String phone;
        private String source;
    }
}
