package backend_api.Backend.Service.Implementation;

import backend_api.Backend.Entity.UserData;
import backend_api.Backend.Entity.ProviderData;
import backend_api.Backend.Entity.SolicitudData;
import backend_api.Backend.Repository.UserDataRepository;
import backend_api.Backend.Repository.ProviderDataRepository;
import backend_api.Backend.Repository.SolicitudDataRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class DataStorageServiceImpl {

    private final UserDataRepository userDataRepository;
    private final ProviderDataRepository providerDataRepository;
    private final SolicitudDataRepository solicitudDataRepository;

    @Transactional
    public void saveUserData(Long userId, Map<String, Object> userDataMap, String secondaryId) {
        try {
            // Buscar si ya existe el usuario
            Optional<UserData> existingUserOpt = userDataRepository.findByUserId(userId);
            UserData userData;
            
            if (existingUserOpt.isPresent()) {
                userData = existingUserOpt.get();
                log.info("Actualizando usuario existente: userId={}", userId);
            } else {
                userData = new UserData();
                userData.setUserId(userId);
                log.info("Creando nuevo usuario: userId={}", userId);
            }
            
            // Actualizar datos
            userData.setName((String) userDataMap.get("name"));
            userData.setEmail((String) userDataMap.get("email"));
            userData.setPhone((String) userDataMap.get("phone"));
            userData.setSecondaryId(secondaryId);
            
            // Manejar estado de desactivación
            if (userDataMap.containsKey("status")) {
                String status = (String) userDataMap.get("status");
                if ("DEACTIVATED".equals(status)) {
                    log.info("Usuario desactivado: userId={}, reason={}", userId, userDataMap.get("deactivationReason"));
                }
            }

            userDataRepository.save(userData);
            log.info("Datos de usuario guardados exitosamente: userId={}, name={}, email={}", 
                userId, userData.getName(), userData.getEmail());
        } catch (Exception e) {
            log.error("Error guardando datos de usuario: userId={}, error={}", userId, e.getMessage(), e);
            throw new RuntimeException("Error al guardar datos de usuario", e);
        }
    }

    @Transactional
    public void saveProviderData(Long providerId, Map<String, Object> providerDataMap, String secondaryId) {
        try {
            ProviderData providerData = new ProviderData();
            providerData.setProviderId(providerId);
            providerData.setName((String) providerDataMap.get("name"));
            providerData.setEmail((String) providerDataMap.get("email"));
            providerData.setPhone((String) providerDataMap.get("phone"));
            providerData.setSecondaryId(secondaryId);

            providerDataRepository.save(providerData);
            log.info("Datos de prestador guardados: providerId={}, name={}", providerId, providerData.getName());
        } catch (Exception e) {
            log.error("Error guardando datos de prestador: providerId={}, error={}", providerId, e.getMessage());
        }
    }

    @Transactional
    public void saveSolicitudData(Long solicitudId, Long userId, Long providerId, 
                                 Double amount, String currency, String description, 
                                 String secondaryId, String status) {
        try {
            SolicitudData solicitudData = new SolicitudData();
            solicitudData.setSolicitudId(solicitudId);
            solicitudData.setUserId(userId);
            solicitudData.setProviderId(providerId);
            solicitudData.setAmount(amount);
            solicitudData.setCurrency(currency);
            solicitudData.setDescription(description);
            solicitudData.setSecondaryId(secondaryId);
            solicitudData.setStatus(status);

            solicitudDataRepository.save(solicitudData);
            log.info("Datos de solicitud guardados: solicitudId={}, amount={}", solicitudId, amount);
        } catch (Exception e) {
            log.error("Error guardando datos de solicitud: solicitudId={}, error={}", solicitudId, e.getMessage());
        }
    }

    public Optional<UserData> getUserData(Long userId) {
        return userDataRepository.findByUserId(userId);
    }

    public Optional<ProviderData> getProviderData(Long providerId) {
        return providerDataRepository.findByProviderId(providerId);
    }

    public Optional<SolicitudData> getSolicitudData(Long solicitudId) {
        return solicitudDataRepository.findBySolicitudId(solicitudId);
    }

    public boolean userDataExists(Long userId) {
        return userDataRepository.existsByUserId(userId);
    }

    public boolean providerDataExists(Long providerId) {
        return providerDataRepository.existsByProviderId(providerId);
    }

    public boolean solicitudDataExists(Long solicitudId) {
        return solicitudDataRepository.existsBySolicitudId(solicitudId);
    }

    @Transactional
    public void deactivateUser(Long userId, String reason) {
        try {
            Optional<UserData> userDataOpt = userDataRepository.findByUserId(userId);
            if (userDataOpt.isPresent()) {
                UserData userData = userDataOpt.get();
                // Aquí podrías agregar un campo de estado si lo necesitas
                log.info("Usuario desactivado: userId={}, reason={}", userId, reason);
            } else {
                log.warn("Usuario no encontrado para desactivar: userId={}", userId);
            }
        } catch (Exception e) {
            log.error("Error desactivando usuario: userId={}, error={}", userId, e.getMessage(), e);
            throw new RuntimeException("Error al desactivar usuario", e);
        }
    }

    @Transactional
    public void deleteUserData(Long userId) {
        try {
            Optional<UserData> userDataOpt = userDataRepository.findByUserId(userId);
            if (userDataOpt.isPresent()) {
                userDataRepository.delete(userDataOpt.get());
                log.info("Datos de usuario eliminados: userId={}", userId);
            } else {
                log.warn("Usuario no encontrado para eliminar: userId={}", userId);
            }
        } catch (Exception e) {
            log.error("Error eliminando datos de usuario: userId={}, error={}", userId, e.getMessage(), e);
            throw new RuntimeException("Error al eliminar datos de usuario", e);
        }
    }
}
