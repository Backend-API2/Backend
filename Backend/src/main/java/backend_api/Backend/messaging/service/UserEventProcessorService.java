package backend_api.Backend.messaging.service;

import backend_api.Backend.messaging.dto.*;
import backend_api.Backend.Service.Implementation.DataStorageServiceImpl;
import backend_api.Backend.Repository.ProviderDataRepository;
import backend_api.Backend.Repository.UserDataRepository;
import backend_api.Backend.Entity.ProviderData;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Random;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserEventProcessorService {

    private final ObjectMapper objectMapper;
    private final DataStorageServiceImpl dataStorageService;
    private final ProviderDataRepository providerDataRepository;
    private final UserDataRepository userDataRepository;

    // En UserEventProcessorService

    public void processUserCreatedFromCore(CoreEventMessage coreMessage) {
        log.info("Procesando evento de usuario creado del CORE - MessageId: {}", coreMessage.getMessageId());

        try {
            Map<String, Object> payload = coreMessage.getPayload();

            // Según la documentación de CORE, el campo correcto es "userId"
            Long userId = extractLong(payload, "userId");
            if (userId == null) {
                // Fallback para compatibilidad con versiones anteriores
                userId = extractLong(payload, "id");
            }

            String email       = extractString(payload, "email");
            String firstName   = extractString(payload, "firstName");
            String lastName    = extractString(payload, "lastName");
            String phoneNumber = extractString(payload, "phoneNumber");
            String role        = extractString(payload, "role");
            String dni         = extractString(payload, "dni");

            log.info("Usuario creado - UserId: {}, Email: {}, Role: {}", userId, email, role);

            // PRESTADOR => provider_data vía service centralizado
            if (role != null && role.equalsIgnoreCase("PRESTADOR")) {
                Map<String, Object> pd = new java.util.HashMap<>();

                // IDs opcionales para upsert robusto
                String providerDataIdStr = extractString(payload, "providerDataId");
                if (providerDataIdStr == null) providerDataIdStr = extractString(payload, "provider_data_id");
                if (providerDataIdStr != null && !providerDataIdStr.isBlank()) {
                    pd.put("providerDataId", providerDataIdStr);
                }

                // identidad/contacto
                if (firstName != null) pd.put("firstName", firstName);
                if (lastName  != null) pd.put("lastName",  lastName);
                // opcional por compatibilidad (display)
                pd.put("name", ((firstName != null ? firstName : "") + " " + (lastName != null ? lastName : "")).trim());
                pd.put("email", email);
                pd.put("phone", phoneNumber);
                pd.put("active", true);

                // address / zones / skills si vienen y con tipo correcto
                Object addr = payload.get("address");
                if (addr instanceof java.util.List) {
                    @SuppressWarnings("unchecked")
                    java.util.List<Map<String,Object>> addresses = (java.util.List<Map<String,Object>>) addr;
                    pd.put("address", addresses);
                }
                Object zones = payload.get("zones");
                if (zones instanceof java.util.List) {
                    @SuppressWarnings("unchecked")
                    java.util.List<String> z = (java.util.List<String>) zones;
                    pd.put("zones", z);
                }
                Object skills = payload.get("skills");
                if (skills instanceof java.util.List) {
                    @SuppressWarnings("unchecked")
                    java.util.List<String> s = (java.util.List<String>) skills;
                    pd.put("skills", s);
                }

                // secondaryId para provider: uso el DNI
                dataStorageService.saveProviderData(userId, pd, dni);
                log.info("Prestador guardado en provider_data - ProviderId: {}, Email: {}", userId, email);
                return; // IMPORTANTE: no continuar por la rama de usuarios
            }

            // CLIENTE / ADMIN => user_data
            Map<String, Object> userData = new java.util.HashMap<>();
            userData.put("name",
                    ((firstName != null ? firstName : "") + " " + (lastName != null ? lastName : "")).trim());
            userData.put("firstName", firstName);
            userData.put("lastName", lastName);
            userData.put("email", email);
            userData.put("phone", phoneNumber);
            userData.put("role", role);
            userData.put("dni", dni);
            userData.put("active", true);

            // address / zones / skills si vienen
            Object addr = payload.get("address");
            if (addr instanceof java.util.List) {
                @SuppressWarnings("unchecked")
                java.util.List<Map<String, Object>> addresses = (java.util.List<Map<String, Object>>) addr;
                userData.put("address", addresses);
            }
            Object zones = payload.get("zones");
            if (zones instanceof java.util.List) {
                @SuppressWarnings("unchecked")
                java.util.List<String> z = (java.util.List<String>) zones;
                userData.put("zones", z);
            }
            Object skills = payload.get("skills");
            if (skills instanceof java.util.List) {
                @SuppressWarnings("unchecked")
                java.util.List<String> s = (java.util.List<String>) skills;
                userData.put("skills", s);
            }

            // saldo aleatorio para CLIENTE (mayor a 10 millones)
            if (role != null && role.equalsIgnoreCase("CLIENTE")) {
                java.util.Random random = new java.util.Random();
                // Saldo entre 10,000,000 y 15,000,000
                double saldo = 10000000 + (random.nextDouble() * 5000000);
                userData.put("saldoDisponible", java.math.BigDecimal.valueOf(saldo)
                        .setScale(2, java.math.RoundingMode.HALF_UP));
            }

            dataStorageService.saveUserData(userId, userData, coreMessage.getMessageId());
            log.info("Usuario guardado en user_data - UserId: {}, Role: {}", userId, role);

        } catch (Exception e) {
            log.error("Error procesando usuario creado - MessageId: {}, Error: {}",
                    coreMessage.getMessageId(), e.getMessage(), e);
            throw new RuntimeException("Error al procesar usuario creado", e);
        }
    }

    // En UserEventProcessorService

    public void processUserUpdatedFromCore(CoreEventMessage coreMessage) {
        log.info("Procesando evento de usuario actualizado del CORE - MessageId: {}", coreMessage.getMessageId());

        try {
            Map<String, Object> payload = coreMessage.getPayload();

            // Extraer userId del payload
            Long userId = extractLong(payload, "userId");
            if (userId == null) {
                throw new IllegalArgumentException("userId no encontrado en el payload");
            }

            UserUpdatedMessage userUpdated = objectMapper.convertValue(payload, UserUpdatedMessage.class);

            log.info("Usuario actualizado - UserId: {}, Email: {}, Role: {}",
                    userUpdated.getUserId(), userUpdated.getEmail(), userUpdated.getRole());

            boolean isPrestador =
                    (userUpdated.getRole() != null && "PRESTADOR".equalsIgnoreCase(userUpdated.getRole()))
                            || (userUpdated.getRole() == null && dataStorageService.providerDataExists(userUpdated.getUserId()));

            if (isPrestador) {
                Map<String, Object> pd = new java.util.HashMap<>();

                // IDs opcionales para upsert robusto
                String providerDataIdStr = extractString(payload, "providerDataId");
                if (providerDataIdStr == null) providerDataIdStr = extractString(payload, "provider_data_id");
                if (providerDataIdStr != null && !providerDataIdStr.isBlank()) {
                    pd.put("providerDataId", providerDataIdStr);
                }

                // nombres
                if (userUpdated.getFirstName() != null) pd.put("firstName", userUpdated.getFirstName());
                if (userUpdated.getLastName()  != null) pd.put("lastName",  userUpdated.getLastName());
                // opcional por compatibilidad (display)
                pd.put("name",
                        ((userUpdated.getFirstName() != null ? userUpdated.getFirstName() : "") + " " +
                                (userUpdated.getLastName()  != null ? userUpdated.getLastName()  : "")).trim());

                // contacto
                if (userUpdated.getEmail() != null)       pd.put("email", userUpdated.getEmail());
                if (userUpdated.getPhoneNumber() != null) pd.put("phone", userUpdated.getPhoneNumber());

                // address si viene
                if (userUpdated.getAddress() != null && !userUpdated.getAddress().isEmpty()) {
                    java.util.List<Map<String,Object>> addrs = userUpdated.getAddress().stream()
                            .map(a -> {
                                Map<String,Object> m = new java.util.HashMap<>();
                                m.put("state", a.getState());
                                m.put("city", a.getCity());
                                m.put("street", a.getStreet());
                                m.put("number", a.getNumber());
                                m.put("floor", a.getFloor());
                                m.put("apartment", a.getApartment());
                                return m;
                            })
                            .collect(java.util.stream.Collectors.toList());
                    pd.put("address", addrs);
                }

                // colecciones (merge/upsert se maneja en el service)
                if (userUpdated.getZones()  != null) pd.put("zones",  userUpdated.getZones());
                if (userUpdated.getSkills() != null) pd.put("skills", userUpdated.getSkills());

                dataStorageService.saveProviderData(userUpdated.getUserId(), pd, userUpdated.getDni());
                log.info("Prestador actualizado en provider_data - ProviderId: {}", userUpdated.getUserId());
                return; // IMPORTANTE: no seguir por la rama de user_data
            }

            // Caso usuarios (CLIENTE/ADMIN)
            Map<String, Object> userData = new java.util.HashMap<>();
            userData.put("name",
                    ((userUpdated.getFirstName() != null ? userUpdated.getFirstName() : "") + " " +
                            (userUpdated.getLastName()  != null ? userUpdated.getLastName()  : "")).trim());
            userData.put("firstName", userUpdated.getFirstName());
            userData.put("lastName",  userUpdated.getLastName());

            if (userUpdated.getEmail() != null)       userData.put("email", userUpdated.getEmail());
            if (userUpdated.getPhoneNumber() != null) userData.put("phone", userUpdated.getPhoneNumber());
            if (userUpdated.getRole() != null)        userData.put("role",  userUpdated.getRole());
            if (userUpdated.getDni() != null)         userData.put("dni",   userUpdated.getDni());

            if (userUpdated.getAddress() != null && !userUpdated.getAddress().isEmpty()) {
                java.util.List<Map<String, Object>> addresses = userUpdated.getAddress().stream()
                        .map(addr -> {
                            Map<String, Object> addrMap = new java.util.HashMap<>();
                            addrMap.put("state", addr.getState());
                            addrMap.put("city", addr.getCity());
                            addrMap.put("street", addr.getStreet());
                            addrMap.put("number", addr.getNumber());
                            addrMap.put("floor", addr.getFloor());
                            addrMap.put("apartment", addr.getApartment());
                            return addrMap;
                        })
                        .collect(java.util.stream.Collectors.toList());
                userData.put("address", addresses);
            }

            if (userUpdated.getZones()  != null) userData.put("zones",  userUpdated.getZones());
            if (userUpdated.getSkills() != null) userData.put("skills", userUpdated.getSkills());

            dataStorageService.saveUserData(userUpdated.getUserId(), userData, coreMessage.getMessageId());
            log.info("Usuario actualizado exitosamente en BD - UserId: {}", userUpdated.getUserId());

        } catch (Exception e) {
            log.error("Error procesando usuario actualizado - MessageId: {}, Error: {}",
                    coreMessage.getMessageId(), e.getMessage(), e);
            throw new RuntimeException("Error al procesar usuario actualizado", e);
        }
    }

    public void processUserDeactivatedFromCore(CoreEventMessage coreMessage) {
        log.info("Procesando evento de usuario desactivado del CORE - MessageId: {}", coreMessage.getMessageId());

        try {
            Map<String, Object> payload = coreMessage.getPayload();
            
            // Extraer userId del payload
            Long userId = extractLong(payload, "userId");
            String email = extractString(payload, "email");
            
            if (userId == null && email == null) {
                throw new IllegalArgumentException("userId o email no encontrado en el payload de user_deactivated");
            }
            
            String message = extractString(payload, "message");
            String deactivationReason = message != null ? message : "Usuario dado de baja";
            
            log.info("Usuario desactivado - UserId: {}, Email: {}, Reason: {}",
                userId, email, deactivationReason);

            // Si tenemos userId, usar ese método directamente (sin saveUserData para evitar conflictos)
            if (userId != null) {
                dataStorageService.deactivateUser(userId, deactivationReason);
                log.info("Usuario desactivado exitosamente en BD - UserId: {}", userId);
            } else if (email != null) {
                // Si solo tenemos email, buscar por email
                dataStorageService.deactivateUserByEmail(email, deactivationReason);
                log.info("Usuario desactivado exitosamente en BD por email - Email: {}", email);
            }

        } catch (Exception e) {
            log.error("Error procesando usuario desactivado - MessageId: {}, Error: {}",
                coreMessage.getMessageId(), e.getMessage(), e);
            throw new RuntimeException("Error al procesar usuario desactivado", e);
        }
    }

    private Long extractLong(Map<String, Object> payload, String key) {
        Object value = payload.get(key);
        if (value == null) return null;
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        if (value instanceof String) {
            try {
                return Long.parseLong((String) value);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }

    private String extractString(Map<String, Object> payload, String key) {
        Object value = payload.get(key);
        return value != null ? value.toString() : null;
    }

    /**
     * Guarda datos de prestador en provider_data
     */
    // En UserEventProcessorService
// Reescribo tu helper privado para que NO cree duplicados ni toque el repositorio directamente.
// Solo arma el mapa normalizado y delega al DataStorageServiceImpl.saveProviderData(...)

    private void saveProviderData(Long providerId,
                                  String email,
                                  String firstName,
                                  String lastName,
                                  String phoneNumber,
                                  String dni,
                                  Map<String, Object> payload) {
        try {
            Map<String, Object> m = new java.util.HashMap<>();

            // IDs opcionales para upsert robusto (si llegan por payload)
            Object pdi = payload != null ? (payload.get("providerDataId") != null ? payload.get("providerDataId")
                    : payload.get("provider_data_id")) : null;
            if (pdi != null) m.put("providerDataId", pdi.toString());

            // identidad/contacto
            if (firstName != null) m.put("firstName", firstName);
            if (lastName  != null) m.put("lastName",  lastName);
            // opcional por compatibilidad (display)
            m.put("name", ((firstName != null ? firstName : "") + " " + (lastName != null ? lastName : "")).trim());

            if (email != null)       m.put("email", email);
            if (phoneNumber != null) m.put("phone", phoneNumber);
            m.put("active", true);

            // address (tal como viene)
            if (payload != null && payload.containsKey("address")) {
                @SuppressWarnings("unchecked")
                java.util.List<Map<String, Object>> addresses = (java.util.List<Map<String, Object>>) payload.get("address");
                if (addresses != null && !addresses.isEmpty()) {
                    m.put("address", addresses);
                }
            }

            // zones
            if (payload != null && payload.containsKey("zones")) {
                @SuppressWarnings("unchecked")
                java.util.List<String> zones = (java.util.List<String>) payload.get("zones");
                if (zones != null) m.put("zones", zones);
            }

            // skills
            if (payload != null && payload.containsKey("skills")) {
                @SuppressWarnings("unchecked")
                java.util.List<String> skills = (java.util.List<String>) payload.get("skills");
                if (skills != null) m.put("skills", skills);
            }

            // delega en el service central (upsert + merge de colecciones)
            dataStorageService.saveProviderData(providerId, m, dni);

            log.info("ProviderData upsert delegado al service - ProviderId: {}, Email: {}", providerId, email);

        } catch (Exception e) {
            log.error("Error guardando/upsert ProviderData - ProviderId: {}, Error: {}",
                    providerId, e.getMessage(), e);
            throw new RuntimeException("Error guardando datos de prestador", e);
        }
    }

    public void processUserRejectedFromCore(CoreEventMessage coreMessage) {
        log.info("Procesando evento de usuario rechazado del CORE - MessageId: {}", coreMessage.getMessageId());

        try {
            Map<String, Object> payload = coreMessage.getPayload();
            
            // Extraer datos del payload manualmente
            Long userId = extractLong(payload, "userId");
            if (userId == null) {
                // Fallback para compatibilidad con versiones anteriores
                userId = extractLong(payload, "id");
            }
            String email = extractString(payload, "email");
            String message = extractString(payload, "message");
            
            log.info("Usuario rechazado - UserId: {}, Email: {}, Message: {}", userId, email, message);

            // Si no hay userId pero hay email, buscar por email
            if (userId == null && email != null) {
                log.info("No se encontró userId, buscando usuario por email: {}", email);
                List<backend_api.Backend.Entity.UserData> users = userDataRepository.findAllByEmail(email);
                if (!users.isEmpty()) {
                    backend_api.Backend.Entity.UserData userData = users.get(0);
                    userId = userData.getUserId();
                    log.info("Usuario encontrado por email, userId: {}", userId);
                } else {
                    // Si no encontramos el usuario, solo desactivamos por email (por si acaso)
                    dataStorageService.deactivateUserByEmail(email, message);
                    log.info("Usuario no encontrado en BD, rechazado procesado por email - Email: {}", email);
                    return;
                }
            }
            
            if (userId == null) {
                throw new IllegalArgumentException("userId o email no encontrado en el payload de user_rejected");
            }

            // Actualizar datos del usuario con estado de rechazo
            Map<String, Object> userData = new java.util.HashMap<>();
            userData.put("status", "REJECTED");
            userData.put("rejectionReason", message);
            userData.put("active", false);

            dataStorageService.saveUserData(userId, userData, coreMessage.getMessageId());
            
            log.info("Usuario rechazado procesado exitosamente en BD - UserId: {}", userId);

        } catch (Exception e) {
            log.error("Error procesando usuario rechazado - MessageId: {}, Error: {}",
                coreMessage.getMessageId(), e.getMessage(), e);
            throw new RuntimeException("Error al procesar usuario rechazado", e);
        }
    }
}
