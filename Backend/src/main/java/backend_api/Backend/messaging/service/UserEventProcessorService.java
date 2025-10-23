package backend_api.Backend.messaging.service;

import backend_api.Backend.messaging.dto.*;
import backend_api.Backend.Service.Implementation.DataStorageServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Random;

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

            Map<String, Object> userData = new java.util.HashMap<>();
            userData.put("name", (userCreated.getFirstName() != null ? userCreated.getFirstName() : "") + 
                               " " + (userCreated.getLastName() != null ? userCreated.getLastName() : ""));
            userData.put("email", userCreated.getEmail());
            userData.put("phone", userCreated.getPhoneNumber());
            userData.put("role", userCreated.getRole());
            userData.put("dni", userCreated.getDni());
            
            // Generar sueldo aleatorio entre $10,000 y $50,000 (igual que en la tabla principal)
            Random random = new Random();
            double saldo = 10000 + (random.nextDouble() * 40000);
            userData.put("saldoDisponible", BigDecimal.valueOf(saldo).setScale(2, java.math.RoundingMode.HALF_UP));

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

            Map<String, Object> userData = new java.util.HashMap<>();
            userData.put("name", (userUpdated.getFirstName() != null ? userUpdated.getFirstName() : "") + 
                               " " + (userUpdated.getLastName() != null ? userUpdated.getLastName() : ""));
            userData.put("email", userUpdated.getEmail());
            userData.put("phone", userUpdated.getPhoneNumber());
            userData.put("role", userUpdated.getRole());
            userData.put("dni", userUpdated.getDni());

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

            // Obtener el reason del payload original si no está en el DTO
            String deactivationReason = userDeactivated.getReason();
            if (deactivationReason == null) {
                // Intentar obtener del payload original
                Map<String, Object> originalPayload = coreMessage.getPayload();
                deactivationReason = (String) originalPayload.get("deactivationReason");
            }
            
            log.info("Usuario desactivado - UserId: {}, Email: {}, Reason: {}",
                userDeactivated.getUserId(), userDeactivated.getEmail(), deactivationReason);

            // Actualizar datos del usuario con estado de desactivación
            Map<String, Object> userData = new java.util.HashMap<>();
            userData.put("name", (userDeactivated.getFirstName() != null ? userDeactivated.getFirstName() : "") + 
                               " " + (userDeactivated.getLastName() != null ? userDeactivated.getLastName() : ""));
            userData.put("email", userDeactivated.getEmail());
            userData.put("phone", userDeactivated.getPhoneNumber());
            userData.put("role", userDeactivated.getRole());
            userData.put("dni", userDeactivated.getDni());
            userData.put("status", "DEACTIVATED");
            userData.put("deactivationReason", deactivationReason);

            dataStorageService.saveUserData(userDeactivated.getUserId(), userData, coreMessage.getMessageId());
            
            // También llamar al método específico de desactivación
            dataStorageService.deactivateUser(userDeactivated.getUserId(), deactivationReason);
            
            log.info("Usuario desactivado exitosamente en BD - UserId: {}", userDeactivated.getUserId());

        } catch (Exception e) {
            log.error("Error procesando usuario desactivado - MessageId: {}, Error: {}",
                coreMessage.getMessageId(), e.getMessage(), e);
            throw new RuntimeException("Error al procesar usuario desactivado", e);
        }
    }
}
