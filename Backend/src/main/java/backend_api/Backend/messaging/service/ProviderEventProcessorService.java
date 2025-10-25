package backend_api.Backend.messaging.service;

import backend_api.Backend.Entity.ProviderData;
import backend_api.Backend.Repository.ProviderDataRepository;
import backend_api.Backend.messaging.dto.CoreEventMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProviderEventProcessorService {

    private final ProviderDataRepository providerDataRepository;

    public void processProviderFromCore(CoreEventMessage message) {
        String event = Optional.ofNullable(message.getDestination())
                .map(CoreEventMessage.Destination::getEventName)
                .orElse("")
                .toLowerCase();

        String channel = Optional.ofNullable(message.getDestination())
                .map(CoreEventMessage.Destination::getChannel)
                .orElse("")
                .toLowerCase();

        Map<String, Object> payload = Optional.ofNullable(message.getPayload()).orElse(Map.of());

        // Soportamos ambas convenciones:
        //  - channel: "catalogue.prestador.alta" | ".modificacion" | ".baja"
        //  - eventName: "alta_prestador" | "modificacion_prestador" | "baja_prestador"
        if (channel.contains("prestador.alta") || event.contains("alta")) {
            upsertProvider(payload, true);
            log.info("✅ Alta de prestador procesada (messageId={}): {}", message.getMessageId(), safeId(payload));
        } else if (channel.contains("prestador.modificacion") || event.contains("modificacion")) {
            upsertProvider(payload, false);
            log.info("✅ Modificación de prestador procesada (messageId={}): {}", message.getMessageId(), safeId(payload));
        } else if (channel.contains("prestador.baja") || event.contains("baja")) {
            deactivateProvider(payload);
            log.info("✅ Baja de prestador procesada (messageId={}): {}", message.getMessageId(), safeId(payload));
        } else {
            log.warn("⚠️ Evento de prestadores no reconocido. channel={}, eventName={}", channel, event);
        }
    }

    private String safeId(Map<String, Object> payload) {
        Object id = payload.get("id");
        return id == null ? "null" : id.toString();
    }

    private void upsertProvider(Map<String, Object> payload, boolean isCreate) {
        Long providerId = readLong(payload.get("id"));
        if (providerId == null) {
            log.warn("❗ payload.id ausente o inválido. No puedo upsert");
            return;
        }

        ProviderData entity = providerDataRepository.findByProviderId(providerId)
                .orElseGet(ProviderData::new);

        entity.setProviderId(providerId);

        // Nombre completo a partir de nombre + apellido
        String nombre = asStr(payload.get("nombre"));
        String apellido = asStr(payload.get("apellido"));
        String name = (nombre + " " + Optional.ofNullable(apellido).orElse("")).trim();
        entity.setName(!name.isBlank() ? name : asStr(payload.get("name"))); // fallback

        entity.setEmail(asStr(payload.get("email")));
        entity.setPhone(asStr(payload.get("telefono")));
        entity.setSecondaryId(firstNonBlank(asStr(payload.get("dni")), asStr(payload.get("cuit"))));
        entity.setPhoto(asStr(payload.get("foto")));

        // Dirección (flat)
        entity.setState(asStr(payload.get("estado")));
        entity.setCity(asStr(payload.get("ciudad")));
        entity.setStreet(asStr(payload.get("calle")));
        entity.setNumber(asStr(payload.get("numero")));
        entity.setFloor(asStr(payload.get("piso")));
        entity.setApartment(asStr(payload.get("departamento")));

        // activo: puede venir 1/0, boolean o string
        entity.setActive(readBooleanFlexible(payload.get("activo"), true));

        // Habilidades / Zonas (snapshot)
        entity.setSkills(readStringList(payload.get("habilidades")));
        entity.setZones(readStringList(payload.get("zonas")));

        entity.setUpdatedAt(LocalDateTime.now());
        if (entity.getId() == null) {
            entity.setCreatedAt(LocalDateTime.now());
        }

        providerDataRepository.save(entity);
    }

    private void deactivateProvider(Map<String, Object> payload) {
        Long providerId = readLong(payload.get("id"));
        if (providerId == null) {
            log.warn("❗ payload.id ausente o inválido. No puedo desactivar");
            return;
        }
        ProviderData entity = providerDataRepository.findByProviderId(providerId)
                .orElseGet(() -> {
                    ProviderData p = new ProviderData();
                    p.setProviderId(providerId);
                    p.setCreatedAt(LocalDateTime.now());
                    return p;
                });

        entity.setActive(false);
        entity.setUpdatedAt(LocalDateTime.now());
        providerDataRepository.save(entity);
    }

    /* helpers */

    private String asStr(Object o) {
        return o == null ? null : String.valueOf(o).trim();
    }

    private String firstNonBlank(String a, String b) {
        if (a != null && !a.isBlank()) return a;
        if (b != null && !b.isBlank()) return b;
        return null;
    }

    private Long readLong(Object o) {
        try {
            if (o == null) return null;
            if (o instanceof Number) return ((Number) o).longValue();
            return Long.parseLong(String.valueOf(o));
        } catch (Exception e) {
            return null;
        }
    }

    private boolean readBooleanFlexible(Object o, boolean defaultValue) {
        if (o == null) return defaultValue;
        if (o instanceof Boolean) return (Boolean) o;
        String s = String.valueOf(o).trim().toLowerCase();
        if (s.equals("true") || s.equals("1") || s.equals("yes") || s.equals("si")) return true;
        if (s.equals("false") || s.equals("0") || s.equals("no")) return false;
        try {
            return Integer.parseInt(s) != 0;
        } catch (Exception e) {
            return defaultValue;
        }
    }

    private List<String> readStringList(Object o) {
        if (o == null) return new ArrayList<>();
        if (o instanceof List<?> list) {
            return list.stream()
                    .map(item -> {
                        if (item == null) return null;
                        if (item instanceof Map<?, ?> m) {
                            // si llega como { "id": 46, "nombre": "Programador Java", ...}
                            Object nombre = m.get("nombre");
                            return nombre != null ? nombre.toString() : item.toString();
                        }
                        return item.toString();
                    })
                    .filter(Objects::nonNull)
                    .map(String::trim)
                    .filter(s -> !s.isBlank())
                    .distinct()
                    .collect(Collectors.toList());
        }
        // si viene como string con comas
        return Arrays.stream(String.valueOf(o).split(","))
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .distinct()
                .collect(Collectors.toList());
    }
}