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
import java.math.BigDecimal;

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
            
            // Actualizar datos básicos
            if (userDataMap.containsKey("name")) {
                userData.setName((String) userDataMap.get("name"));
            }
            if (userDataMap.containsKey("firstName")) {
                userData.setFirstName((String) userDataMap.get("firstName"));
            }
            if (userDataMap.containsKey("lastName")) {
                userData.setLastName((String) userDataMap.get("lastName"));
            }
            if (userDataMap.containsKey("email")) {
                userData.setEmail((String) userDataMap.get("email"));
            }
            if (userDataMap.containsKey("phone")) {
                userData.setPhone((String) userDataMap.get("phone"));
            }
            if (secondaryId != null) {
                userData.setSecondaryId(secondaryId);
            }
            if (userDataMap.containsKey("dni")) {
                userData.setDni((String) userDataMap.get("dni"));
            }
            
            // Actualizar role si está presente
            if (userDataMap.containsKey("role")) {
                userData.setRole((String) userDataMap.get("role"));
            }
            
            // Actualizar active si está presente
            if (userDataMap.containsKey("active")) {
                Object activeObj = userDataMap.get("active");
                if (activeObj instanceof Boolean) {
                    userData.setActive((Boolean) activeObj);
                } else if (activeObj instanceof Number) {
                    userData.setActive(((Number) activeObj).intValue() == 1);
                }
            }
            
            // Actualizar dirección (tomar la primera dirección del array si viene)
            if (userDataMap.containsKey("address")) {
                @SuppressWarnings("unchecked")
                java.util.List<Map<String, Object>> addresses = (java.util.List<Map<String, Object>>) userDataMap.get("address");
                if (addresses != null && !addresses.isEmpty()) {
                    Map<String, Object> firstAddress = addresses.get(0);
                    if (firstAddress.containsKey("state")) {
                        userData.setState((String) firstAddress.get("state"));
                    }
                    if (firstAddress.containsKey("city")) {
                        userData.setCity((String) firstAddress.get("city"));
                    }
                    if (firstAddress.containsKey("street")) {
                        userData.setStreet((String) firstAddress.get("street"));
                    }
                    if (firstAddress.containsKey("number")) {
                        userData.setNumber((String) firstAddress.get("number"));
                    }
                    if (firstAddress.containsKey("floor")) {
                        userData.setFloor((String) firstAddress.get("floor"));
                    }
                    if (firstAddress.containsKey("apartment")) {
                        userData.setApartment((String) firstAddress.get("apartment"));
                    }
                }
            }
            
            // Actualizar zones si está presente
            if (userDataMap.containsKey("zones")) {
                @SuppressWarnings("unchecked")
                java.util.List<String> zones = (java.util.List<String>) userDataMap.get("zones");
                if (zones != null) {
                    userData.setZones(zones);
                }
            }
            
            // Actualizar skills si está presente
            if (userDataMap.containsKey("skills")) {
                @SuppressWarnings("unchecked")
                java.util.List<String> skills = (java.util.List<String>) userDataMap.get("skills");
                if (skills != null) {
                    userData.setSkills(skills);
                }
            }
            
            // Actualizar saldoDisponible si está presente
            if (userDataMap.containsKey("saldoDisponible")) {
                Object saldo = userDataMap.get("saldoDisponible");
                if (saldo instanceof BigDecimal) {
                    userData.setSaldoDisponible((BigDecimal) saldo);
                } else if (saldo instanceof Number) {
                    userData.setSaldoDisponible(BigDecimal.valueOf(((Number) saldo).doubleValue()));
                }
            }
            
            // Manejar estado de desactivación
            if (userDataMap.containsKey("status")) {
                String status = (String) userDataMap.get("status");
                if ("DEACTIVATED".equals(status)) {
                    userData.setActive(false);
                    log.info("Usuario desactivado: userId={}, reason={}", userId, userDataMap.get("deactivationReason"));
                } else if ("REJECTED".equals(status)) {
                    userData.setActive(false);
                    log.info("Usuario rechazado: userId={}, reason={}", userId, userDataMap.get("rejectionReason"));
                }
            }

            userDataRepository.save(userData);
            log.info("Datos de usuario guardados exitosamente: userId={}, name={}, email={}, active={}", 
                userId, userData.getName(), userData.getEmail(), userData.getActive());
        } catch (Exception e) {
            log.error("Error guardando datos de usuario: userId={}, error={}", userId, e.getMessage(), e);
            throw new RuntimeException("Error al guardar datos de usuario", e);
        }
    }

    // DataStorageServiceImpl.java
    @Transactional
    public void saveProviderData(Long providerId, Map<String, Object> m, String secondaryId) {
        try {
            // 1) upsert
            ProviderData provider = providerDataRepository.findByProviderId(providerId)
                    .orElseGet(() -> providerDataRepository.findByEmail((String) m.get("email"))
                            .orElseGet(ProviderData::new));

            if (provider.getProviderId() == null) provider.setProviderId(providerId);

            // 2) básicos
            if (m.containsKey("name"))       provider.setName((String) m.get("name"));
            if (m.containsKey("email"))      provider.setEmail((String) m.get("email"));
            if (m.containsKey("phone"))      provider.setPhone((String) m.get("phone"));
            if (secondaryId != null)         provider.setSecondaryId(secondaryId);

            // photo / active (si vienen)
            if (m.containsKey("photo"))      provider.setPhoto((String) m.get("photo"));
            Object activeObj = m.get("active");
            if (activeObj instanceof Boolean) provider.setActive((Boolean) activeObj);
            else if (activeObj instanceof Number) provider.setActive(((Number)activeObj).intValue() == 1);

            // 3) address (primera)
            if (m.containsKey("address")) {
                @SuppressWarnings("unchecked")
                java.util.List<Map<String,Object>> addrList = (java.util.List<Map<String,Object>>) m.get("address");
                if (addrList != null && !addrList.isEmpty()) {
                    Map<String,Object> a = addrList.get(0);
                    if (a.get("state")      != null) provider.setState((String) a.get("state"));
                    if (a.get("city")       != null) provider.setCity((String) a.get("city"));
                    if (a.get("street")     != null) provider.setStreet((String) a.get("street"));
                    if (a.get("number")     != null) provider.setNumber((String) a.get("number"));
                    if (a.get("floor")      != null) provider.setFloor((String) a.get("floor"));
                    if (a.get("apartment")  != null) provider.setApartment((String) a.get("apartment"));
                }
            }

            // 4) zones / skills
            if (m.containsKey("zones")) {
                @SuppressWarnings("unchecked")
                java.util.List<String> zones = (java.util.List<String>) m.get("zones");
                provider.getZones().clear();
                if (zones != null) provider.getZones().addAll(zones);
            }
            if (m.containsKey("skills")) {
                @SuppressWarnings("unchecked")
                java.util.List<String> skills = (java.util.List<String>) m.get("skills");
                provider.getSkills().clear();
                if (skills != null) provider.getSkills().addAll(skills);
            }

            providerDataRepository.save(provider);
            log.info("✅ Provider upsert: providerId={}, email={}", provider.getProviderId(), provider.getEmail());
        } catch (Exception e) {
            log.error("Error guardando datos de prestador: providerId={}, error={}", providerId, e.getMessage(), e);
            throw new RuntimeException("Error al guardar datos de prestador", e);
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
            // Verificar que el usuario existe antes de desactivar
            if (!userDataRepository.existsByUserId(userId)) {
                log.warn("Usuario no encontrado para desactivar: userId={}", userId);
                return;
            }
            
            // Usar consulta directa para actualizar solo el campo active sin tocar el resto
            int updated = userDataRepository.deactivateByUserId(userId);
            
            if (updated > 0) {
                log.info("Usuario desactivado exitosamente: userId={}, reason={}", userId, reason);
            } else {
                log.warn("No se pudo desactivar el usuario: userId={}", userId);
            }
        } catch (Exception e) {
            log.error("Error desactivando usuario: userId={}, error={}", userId, e.getMessage(), e);
            throw new RuntimeException("Error al desactivar usuario", e);
        }
    }
    
    @Transactional
    public void deactivateUserByEmail(String email, String reason) {
        try {
            // Verificar que el usuario existe antes de desactivar
            if (!userDataRepository.existsByEmail(email)) {
                log.warn("Usuario no encontrado para desactivar: email={}", email);
                return;
            }
            
            // Usar consulta directa para actualizar solo el campo active sin tocar el resto
            int updated = userDataRepository.deactivateByEmail(email);
            
            if (updated > 0) {
                log.info("Usuario desactivado exitosamente por email: email={}, reason={}", email, reason);
            } else {
                log.warn("No se pudo desactivar el usuario: email={}", email);
            }
        } catch (Exception e) {
            log.error("Error desactivando usuario por email: email={}, error={}", email, e.getMessage(), e);
            throw new RuntimeException("Error al desactivar usuario por email", e);
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
