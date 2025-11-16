package backend_api.Backend.Service.Implementation;

import backend_api.Backend.Entity.UserData;
import backend_api.Backend.Entity.ProviderData;
import backend_api.Backend.Entity.user.User;
import backend_api.Backend.Repository.UserDataRepository;
import backend_api.Backend.Repository.ProviderDataRepository;
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
    private final ProviderDataRepository providerDataRepository;

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
        
        // Buscar en provider_data si no está en user_data
        Optional<ProviderData> providerDataOpt = providerDataRepository.findByProviderId(userId);
        if (providerDataOpt.isPresent()) {
            ProviderData providerData = providerDataOpt.get();
            log.info("Usando datos de provider_data para providerId: {}", userId);
            
            return UserInfo.builder()
                    .userId(userId)
                    .name(providerData.getName())
                    .email(providerData.getEmail())
                    .phone(providerData.getPhone())
                    .source("PROVIDER_MODULE")
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
        
        // Los que no están en UserData, buscar en ProviderData (para prestadores)
        Set<Long> remainingIds = userIds.stream()
                .filter(id -> !resultMap.containsKey(id))
                .collect(java.util.stream.Collectors.toSet());
        
        if (!remainingIds.isEmpty()) {
            List<ProviderData> providerDataList = providerDataRepository.findByProviderIdIn(remainingIds);
            for (ProviderData providerData : providerDataList) {
                log.info("Usando datos de provider_data para providerId: {}", providerData.getProviderId());
                resultMap.put(providerData.getProviderId(), UserInfo.builder()
                        .userId(providerData.getProviderId())
                        .name(providerData.getName())
                        .email(providerData.getEmail())
                        .phone(providerData.getPhone())
                        .source("PROVIDER_MODULE")
                        .build());
            }
            
            // Los que aún no están, buscarlos en User (batch)
            Set<Long> stillRemainingIds = remainingIds.stream()
                    .filter(id -> !resultMap.containsKey(id))
                    .collect(java.util.stream.Collectors.toSet());
            
            if (!stillRemainingIds.isEmpty()) {
                List<User> userList = userRepository.findAllById(stillRemainingIds);
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
