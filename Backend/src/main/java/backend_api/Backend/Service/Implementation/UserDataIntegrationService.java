package backend_api.Backend.Service.Implementation;

import backend_api.Backend.Entity.UserData;
import backend_api.Backend.Entity.user.User;
import backend_api.Backend.Repository.UserDataRepository;
import backend_api.Backend.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

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
    
    // Método optimizado para obtener múltiples usuarios de una vez
    public Map<Long, UserInfo> getUserInfoBatch(Set<Long> userIds) {
        Map<Long, UserInfo> resultMap = new HashMap<>();
        
        // Batch query para UserData
        List<UserData> userDataList = userDataRepository.findByUserIdIn(userIds);
        for (UserData userData : userDataList) {
            log.info("Usando datos del módulo de usuarios para userId: {}", userData.getUserId());
            resultMap.put(userData.getUserId(), UserInfo.builder()
                    .userId(userData.getUserId())
                    .name(userData.getName())
                    .email(userData.getEmail())
                    .phone(userData.getPhone())
                    .source("USER_MODULE")
                    .build());
        }
        
        // Los que no están en UserData, buscarlos en User (batch)
        Set<Long> remainingIds = userIds.stream()
                .filter(id -> !resultMap.containsKey(id))
                .collect(java.util.stream.Collectors.toSet());
        
        if (!remainingIds.isEmpty()) {
            List<User> userList = userRepository.findAllById(remainingIds);
            for (User user : userList) {
                log.info("Usando datos locales para userId: {}", user.getId());
                resultMap.put(user.getId(), UserInfo.builder()
                        .userId(user.getId())
                        .name(user.getName())
                        .email(user.getEmail())
                        .phone(user.getPhone())
                        .source("LOCAL")
                        .build());
            }
        }
        
        return resultMap;
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
