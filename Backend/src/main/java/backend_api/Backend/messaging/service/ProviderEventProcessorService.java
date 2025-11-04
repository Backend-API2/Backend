package backend_api.Backend.messaging.service;

import backend_api.Backend.Entity.ProviderData;
import backend_api.Backend.Repository.ProviderDataRepository;
import backend_api.Backend.messaging.dto.CoreEventMessage;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class ProviderEventProcessorService {

    private final ProviderDataRepository providerDataRepository;

    @Transactional
    public void processProviderFromCore(CoreEventMessage message) {
        Map<String, Object> p = message.getPayload();
        if (p == null) {
            log.warn("Payload vacío en provider-event. messageId={}", message.getMessageId());
            return;
        }

        // --- Identificadores principales
        Long providerId = asLong(firstNonNull(p, "providerId", "id"));
        String email    = asString(firstNonNull(p, "email", "mail"));

        // Buscar existente por id o email
        ProviderData entity = null;
        if (providerId != null) {
            entity = providerDataRepository.findByProviderId(providerId).orElse(null);
        }
        if (entity == null && email != null) {
            entity = providerDataRepository.findByEmail(email).orElse(null);
        }
        if (entity == null) {
            entity = new ProviderData();
            entity.setCreatedAt(LocalDateTime.now());
        }

        // --- Datos personales
        String firstName = asString(firstNonNull(p, "firstName", "nombre"));
        String lastName  = asString(firstNonNull(p, "lastName", "apellido"));
        String phone     = asString(firstNonNull(p, "phone", "telefono"));
        String dni       = asString(firstNonNull(p, "secondaryId", "dni"));
        Boolean active   = asBool(firstNonNull(p, "active", "activo"), true);

        if (providerId != null) entity.setProviderId(providerId);
        if (email != null)      entity.setEmail(email);
        entity.setName(joinName(firstName, lastName));
        entity.setPhone(phone);
        entity.setSecondaryId(dni);
        entity.setActive(active);

        // --- Address: soportar address plano o lista
        // address como lista de mapas
        Object addrObj = firstNonNull(p, "address", "addresses", "domicilio");
        if (addrObj instanceof List) {
            List<?> list = (List<?>) addrObj;
            if (!list.isEmpty() && list.get(0) instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String,Object> a = (Map<String,Object>) list.get(0);
                putAddress(entity, a);
            }
        } else if (addrObj instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String,Object> a = (Map<String,Object>) addrObj;
            putAddress(entity, a);
        } else {
            // address plano en ES
            Map<String,Object> a = new HashMap<>();
            a.put("state",  firstNonNull(p, "state",  "estado"));
            a.put("city",   firstNonNull(p, "city",   "ciudad"));
            a.put("street", firstNonNull(p, "street", "calle"));
            a.put("number", firstNonNull(p, "number", "numero"));
            a.put("floor",  firstNonNull(p, "floor",  "piso"));
            a.put("apartment", firstNonNull(p, "apartment", "depto", "departamento"));
            putAddress(entity, a);
        }

        // --- Skills / Zones (ES/EN)
        entity.setSkills(asStringList(firstNonNull(p, "skills", "habilidades")));
        entity.setZones(asStringList(firstNonNull(p, "zones", "zonas")));

        entity.setUpdatedAt(LocalDateTime.now());
        providerDataRepository.save(entity);

        log.info("✅ Provider upserted: id={}, email={}, name='{}', active={}, messageId={}",
                entity.getProviderId(), entity.getEmail(), entity.getName(), entity.getActive(), message.getMessageId());
    }

    // ---------------- helpers ----------------

    private static Object firstNonNull(Map<String,Object> m, String... keys) {
        for (String k : keys) {
            if (m.containsKey(k) && m.get(k) != null) return m.get(k);
        }
        return null;
    }

    private static Long asLong(Object o) {
        if (o == null) return null;
        if (o instanceof Number) return ((Number) o).longValue();
        try { return Long.parseLong(String.valueOf(o)); } catch (Exception ignore) { return null; }
    }

    private static String asString(Object o) {
        return o == null ? null : String.valueOf(o);
    }

    private static Boolean asBool(Object o, boolean def) {
        if (o == null) return def;
        if (o instanceof Boolean) return (Boolean) o;
        String s = String.valueOf(o).trim().toLowerCase();
        if ("true".equals(s) || "1".equals(s) || "yes".equals(s) || "si".equals(s)) return true;
        if ("false".equals(s) || "0".equals(s) || "no".equals(s)) return false;
        return def;
    }

    @SuppressWarnings("unchecked")
    private static List<String> asStringList(Object o) {
        if (o == null) return List.of();
        if (o instanceof List) {
            List<?> raw = (List<?>) o;
            List<String> out = new ArrayList<>();
            for (Object x : raw) if (x != null) out.add(String.valueOf(x));
            return out;
        }
        // permitir string "a,b,c"
        String s = String.valueOf(o);
        if (s.contains(",")) {
            String[] parts = s.split(",");
            List<String> out = new ArrayList<>();
            for (String p : parts) out.add(p.trim());
            return out;
        }
        return List.of(String.valueOf(o));
    }

    private static String joinName(String first, String last) {
        String f = first != null ? first.trim() : "";
        String l = last != null ? last.trim() : "";
        return (f + " " + l).trim();
    }

    private static void putAddress(ProviderData e, Map<String,Object> a) {
        e.setState(asString(firstNonNull(a, "state", "estado")));
        e.setCity(asString(firstNonNull(a, "city", "ciudad")));
        e.setStreet(asString(firstNonNull(a, "street", "calle")));
        e.setNumber(asString(firstNonNull(a, "number", "numero")));
        e.setFloor(asString(firstNonNull(a, "floor", "piso")));
        e.setApartment(asString(firstNonNull(a, "apartment", "depto", "departamento")));
    }
}