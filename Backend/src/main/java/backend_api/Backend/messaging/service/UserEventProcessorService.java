package backend_api.Backend.messaging.service;

import backend_api.Backend.messaging.dto.*;
import backend_api.Backend.Service.Implementation.DataStorageServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserEventProcessorService {

    private final ObjectMapper objectMapper;
    private final DataStorageServiceImpl dataStorageService;

    public void processUserCreatedFromCore(CoreEventMessage coreMessage) {
        log.info("Procesando evento de usuario creado del CORE - MessageId: {}", coreMessage.getMessageId());

        try {
            UserCreatedMessage userCreated = objectMapper.convertValue(
                coreMessage.getPayload(),
                UserCreatedMessage.class
            );

            log.info("Usuario creado - UserId: {}, Email: {}, Role: {}",
                userCreated.getUserId(), userCreated.getEmail(), userCreated.getRole());

            Map<String, Object> userData = Map.of(
                "name", userCreated.getFirstName() + " " + userCreated.getLastName(),
                "email", userCreated.getEmail(),
                "phone", userCreated.getPhoneNumber(),
                "role", userCreated.getRole(),
                "dni", userCreated.getDni()
            );

            dataStorageService.saveUserData(userCreated.getUserId(), userData, coreMessage.getMessageId());
            
            log.info("Usuario guardado exitosamente en BD - UserId: {}", userCreated.getUserId());

        } catch (Exception e) {
            log.error("Error procesando usuario creado - MessageId: {}, Error: {}",
                coreMessage.getMessageId(), e.getMessage(), e);
            throw new RuntimeException("Error al procesar usuario creado", e);
        }
    }

    public void processUserUpdatedFromCore(CoreEventMessage coreMessage) {
        log.info("Procesando evento de usuario actualizado del CORE - MessageId: {}", coreMessage.getMessageId());

        try {
            UserUpdatedMessage userUpdated = objectMapper.convertValue(
                coreMessage.getPayload(),
                UserUpdatedMessage.class
            );

            log.info("Usuario actualizado - UserId: {}, Email: {}, Role: {}",
                userUpdated.getUserId(), userUpdated.getEmail(), userUpdated.getRole());

            Map<String, Object> userData = Map.of(
                "name", userUpdated.getFirstName() + " " + userUpdated.getLastName(),
                "email", userUpdated.getEmail(),
                "phone", userUpdated.getPhoneNumber(),
                "role", userUpdated.getRole(),
                "dni", userUpdated.getDni()
            );

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
            UserDeactivatedMessage userDeactivated = objectMapper.convertValue(
                coreMessage.getPayload(),
                UserDeactivatedMessage.class
            );

            log.info("Usuario desactivado - UserId: {}, Email: {}, Reason: {}",
                userDeactivated.getUserId(), userDeactivated.getEmail(), userDeactivated.getReason());

            Map<String, Object> userData = Map.of(
                "name", userDeactivated.getFirstName() + " " + userDeactivated.getLastName(),
                "email", userDeactivated.getEmail(),
                "phone", userDeactivated.getPhoneNumber(),
                "role", userDeactivated.getRole(),
                "dni", userDeactivated.getDni(),
                "status", "DEACTIVATED",
                "deactivationReason", userDeactivated.getReason()
            );

            dataStorageService.saveUserData(userDeactivated.getUserId(), userData, coreMessage.getMessageId());
            
            log.info("Usuario desactivado exitosamente en BD - UserId: {}", userDeactivated.getUserId());

        } catch (Exception e) {
            log.error("Error procesando usuario desactivado - MessageId: {}, Error: {}",
                coreMessage.getMessageId(), e.getMessage(), e);
            throw new RuntimeException("Error al procesar usuario desactivado", e);
        }
    }
}
