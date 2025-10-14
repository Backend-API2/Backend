package backend_api.Backend.messaging.service;

import backend_api.Backend.messaging.dto.*;
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

    public void processUserCreatedFromCore(CoreEventMessage coreMessage) {
        log.info("Procesando evento de usuario creado del CORE - MessageId: {}", coreMessage.getMessageId());

        try {
            UserCreatedMessage userCreated = objectMapper.convertValue(
                coreMessage.getPayload(),
                UserCreatedMessage.class
            );

            log.info("Usuario creado - UserId: {}, Email: {}, Role: {}",
                userCreated.getUserId(), userCreated.getEmail(), userCreated.getRole());

            // TODO: Aquí puedes agregar la lógica para guardar el usuario en tu base de datos
            // Por ejemplo: userService.saveUser(userCreated);
            
            log.info("Usuario procesado exitosamente - UserId: {}", userCreated.getUserId());

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

            // TODO: Aquí puedes agregar la lógica para actualizar el usuario en tu base de datos
            // Por ejemplo: userService.updateUser(userUpdated);
            
            log.info("Usuario actualizado exitosamente - UserId: {}", userUpdated.getUserId());

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

            // TODO: Aquí puedes agregar la lógica para desactivar el usuario en tu base de datos
            // Por ejemplo: userService.deactivateUser(userDeactivated);
            
            log.info("Usuario desactivado exitosamente - UserId: {}", userDeactivated.getUserId());

        } catch (Exception e) {
            log.error("Error procesando usuario desactivado - MessageId: {}, Error: {}",
                coreMessage.getMessageId(), e.getMessage(), e);
            throw new RuntimeException("Error al procesar usuario desactivado", e);
        }
    }
}
