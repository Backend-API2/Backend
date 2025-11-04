package backend_api.Backend.messaging.service;

import backend_api.Backend.Service.Implementation.DataStorageServiceImpl;
import backend_api.Backend.messaging.dto.CoreEventMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserEventProcessorServiceTest {

    @Mock private DataStorageServiceImpl dataStorageService;
    // Repos no se usan directamente en estos métodos, pero están en el constructor
    @Mock private backend_api.Backend.Repository.ProviderDataRepository providerDataRepository;
    @Mock private backend_api.Backend.Repository.UserDataRepository userDataRepository;

    private ObjectMapper objectMapper;
    private UserEventProcessorService service;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        service = new UserEventProcessorService(objectMapper, dataStorageService, providerDataRepository, userDataRepository);
    }

    private CoreEventMessage mockCoreMessage(String messageId, Map<String,Object> payload) {
        CoreEventMessage cm = mock(CoreEventMessage.class);
        when(cm.getMessageId()).thenReturn(messageId);
        when(cm.getPayload()).thenReturn(payload);
        return cm;
    }

    @Test
    void processUserCreated_prestador_guardaProviderData() {
        Map<String,Object> payload = new HashMap<>();
        payload.put("userId", 12345L);
        payload.put("email", "prestador@demo.com");
        payload.put("firstName", "Ana");
        payload.put("lastName", "Pérez");
        payload.put("phoneNumber", "111-222");
        payload.put("role", "PRESTADOR");
        payload.put("dni", "30111222");
        payload.put("address", List.of(Map.of("state","CABA","city","BA","street","X","number","123")));
        payload.put("zones", List.of("CABA","GBA Sur"));
        payload.put("skills", List.of("electricista","plomero"));

        CoreEventMessage cm = mockCoreMessage("evt-1", payload);

        service.processUserCreatedFromCore(cm);

        ArgumentCaptor<Map<String,Object>> mapCap = ArgumentCaptor.forClass(Map.class);
        verify(dataStorageService).saveProviderData(eq(12345L), mapCap.capture(), eq("30111222"));
        Map<String,Object> sent = mapCap.getValue();

        // Checks coherentes con tu implementación actual
        assertEquals("prestador@demo.com", sent.get("email"));   // el email SÍ se envía
        assertTrue(((List<?>) sent.get("zones")).contains("CABA"));
        assertTrue(((List<?>) sent.get("skills")).contains("plomero"));
    }

    @Test
    void processUserCreated_cliente_guardaUserData_conSaldo() {
        Map<String,Object> payload = new HashMap<>();
        payload.put("userId", 77L);
        payload.put("email", "cliente@demo.com");
        payload.put("firstName", "Juan");
        payload.put("lastName", "Gómez");
        payload.put("phoneNumber", "111-333");
        payload.put("role", "CLIENTE");
        payload.put("dni", "22111111");
        payload.put("zones", List.of("Oeste"));
        payload.put("skills", List.of("algo"));

        CoreEventMessage cm = mockCoreMessage("evt-2", payload);

        service.processUserCreatedFromCore(cm);

        ArgumentCaptor<Map<String,Object>> mapCap = ArgumentCaptor.forClass(Map.class);
        verify(dataStorageService).saveUserData(eq(77L), mapCap.capture(), eq("evt-2"));
        Map<String,Object> sent = mapCap.getValue();

        assertEquals("CLIENTE", sent.get("role"));
        assertTrue(sent.containsKey("saldoDisponible")); // valor aleatorio; alcanza con que exista
    }

    @Test
    void processUserUpdated_prestador_porRole() {
        Map<String,Object> payload = new HashMap<>();
        payload.put("userId", 123L);
        payload.put("role", "PRESTADOR");
        payload.put("firstName", "R");
        payload.put("lastName", "S");
        payload.put("email", "p@d.com");
        payload.put("phoneNumber", "000");
        payload.put("zones", List.of("CABA"));

        CoreEventMessage cm = mockCoreMessage("evt-3", payload);

        service.processUserUpdatedFromCore(cm);

        verify(dataStorageService).saveProviderData(eq(123L), anyMap(), isNull());
    }

    @Test
    void processUserUpdated_prestador_porExistencia() {
        // role null, pero ya existe provider
        when(dataStorageService.providerDataExists(999L)).thenReturn(true);

        Map<String,Object> payload = new HashMap<>();
        payload.put("userId", 999L);
        payload.put("firstName", "Z");
        payload.put("lastName", "Y");

        CoreEventMessage cm = mockCoreMessage("evt-4", payload);

        service.processUserUpdatedFromCore(cm);

        verify(dataStorageService).saveProviderData(eq(999L), anyMap(), isNull());
    }

    @Test
    void processUserUpdated_usuarioComun_actualizaUserData() {
        Map<String,Object> payload = new HashMap<>();
        payload.put("userId", 55L);
        payload.put("firstName", "A");
        payload.put("lastName", "B");
        payload.put("email", "a@b.com");
        payload.put("phoneNumber", "123");
        payload.put("role", "ADMIN");
        payload.put("dni", "12345678");

        CoreEventMessage cm = mockCoreMessage("evt-5", payload);

        service.processUserUpdatedFromCore(cm);

        verify(dataStorageService).saveUserData(eq(55L), anyMap(), eq("evt-5"));
    }

    @Test
    void processUserUpdated_fallaSinUserId() {
        Map<String,Object> payload = new HashMap<>();
        CoreEventMessage cm = mockCoreMessage("evt-6", payload);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> service.processUserUpdatedFromCore(cm));

        assertNotNull(ex.getCause());
        assertTrue(ex.getCause() instanceof IllegalArgumentException);
        assertEquals("userId no encontrado en el payload", ex.getCause().getMessage());
    }
}