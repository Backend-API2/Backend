package backend_api.Backend.messaging.service;

import backend_api.Backend.messaging.dto.CoreEventMessage;
import backend_api.Backend.messaging.dto.ProviderCreatedMessage;
import backend_api.Backend.messaging.dto.ProviderUpdatedMessage;
import backend_api.Backend.messaging.dto.ProviderDeactivatedMessage;
import backend_api.Backend.Service.Implementation.DataStorageServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class ProviderEventProcessorService {

    private final ObjectMapper objectMapper;
    private final DataStorageServiceImpl dataStorageService;

    public void processProviderCreatedFromCore(CoreEventMessage coreMessage) {
        log.info("Procesando ALTA de prestador del CORE - msgId={}", coreMessage.getMessageId());
        ProviderCreatedMessage m = objectMapper.convertValue(coreMessage.getPayload(), ProviderCreatedMessage.class);

        Map<String,Object> providerData = buildProviderDataMap(m.getId(), m.getNombre(), m.getApellido(),
                m.getEmail(), m.getTelefono(), m.getDni(), m.getActivo());

        // Campos opcionales
        providerData.put("foto", m.getFoto());
        providerData.put("estado", m.getEstado());
        providerData.put("ciudad", m.getCiudad());
        providerData.put("calle", m.getCalle());
        providerData.put("numero", m.getNumero());
        providerData.put("piso", m.getPiso());
        providerData.put("departamento", m.getDepartamento());
        providerData.put("habilidades", m.getHabilidades());
        providerData.put("zonas", m.getZonas());

        dataStorageService.saveProviderData(m.getId(), providerData, coreMessage.getMessageId());
        log.info("Prestador guardado en BD local - providerId={}", m.getId());
    }

    public void processProviderUpdatedFromCore(CoreEventMessage coreMessage) {
        log.info("Procesando MODIFICACION de prestador del CORE - msgId={}", coreMessage.getMessageId());
        ProviderUpdatedMessage m = objectMapper.convertValue(coreMessage.getPayload(), ProviderUpdatedMessage.class);

        Map<String,Object> providerData = buildProviderDataMap(m.getId(), m.getNombre(), m.getApellido(),
                m.getEmail(), m.getTelefono(), m.getDni(), m.getActivo());

        providerData.put("foto", m.getFoto());
        providerData.put("estado", m.getEstado());
        providerData.put("ciudad", m.getCiudad());
        providerData.put("calle", m.getCalle());
        providerData.put("numero", m.getNumero());
        providerData.put("piso", m.getPiso());
        providerData.put("departamento", m.getDepartamento());
        providerData.put("habilidades", m.getHabilidades());
        providerData.put("zonas", m.getZonas());

        dataStorageService.saveProviderData(m.getId(), providerData, coreMessage.getMessageId());
        log.info("Prestador actualizado en BD local - providerId={}", m.getId());
    }

    public void processProviderDeactivatedFromCore(CoreEventMessage coreMessage) {
        log.info("Procesando BAJA de prestador del CORE - msgId={}", coreMessage.getMessageId());
        ProviderDeactivatedMessage m = objectMapper.convertValue(coreMessage.getPayload(), ProviderDeactivatedMessage.class);

        Map<String,Object> providerData = new HashMap<>();
        providerData.put("status", "DEACTIVATED");
        providerData.put("activo", m.getActivo());
        if (m.getMotivo() != null) providerData.put("reason", m.getMotivo());

        dataStorageService.saveProviderData(m.getId(), providerData, coreMessage.getMessageId());
        dataStorageService.deactivateProvider(m.getId(), m.getMotivo());
        log.info("Prestador desactivado en BD local - providerId={}", m.getId());
    }

    private Map<String,Object> buildProviderDataMap(Long id, String nombre, String apellido,
                                                    String email, String telefono, String dni, Integer activo) {
        Map<String,Object> map = new HashMap<>();
        map.put("name", (nombre != null ? nombre : "") + (apellido != null ? " " + apellido : ""));
        map.put("email", email);
        map.put("phone", telefono);
        map.put("dni", dni);
        map.put("activo", activo);
        return map;
    }
}