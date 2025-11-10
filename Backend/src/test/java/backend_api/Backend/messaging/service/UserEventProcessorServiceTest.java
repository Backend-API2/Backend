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
    @Test
    void processUserCreated_prestador_roleIgnoreCase_y_providerDataId() {
        Map<String,Object> payload = new HashMap<>();
        payload.put("userId", 555L);
        payload.put("email", "p@demo.com");
        payload.put("firstName", "Ana");
        payload.put("lastName", "María");
        payload.put("phoneNumber", "123");
        payload.put("role", "prestador"); // lower-case
        payload.put("dni", "30111222");
        payload.put("providerDataId", "pd-77");
        payload.put("address", List.of(Map.of("state","GBA Sur","city","Lomas","street","Alsina","number","2201")));
        payload.put("zones", List.of("GBA Sur"));
        payload.put("skills", List.of("electricista","gasista"));

        CoreEventMessage cm = mockCoreMessage("evt-X1", payload);

        service.processUserCreatedFromCore(cm);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<Map<String,Object>> cap = ArgumentCaptor.forClass(Map.class);
        verify(dataStorageService).saveProviderData(eq(555L), cap.capture(), eq("30111222"));

        Map<String,Object> sent = cap.getValue();
        assertEquals("p@demo.com", sent.get("email"));
        assertEquals("pd-77", sent.get("providerDataId"));
        assertEquals(List.of("GBA Sur"), sent.get("zones"));
        assertEquals(List.of("electricista","gasista"), sent.get("skills"));
    }

    @Test
    void processUserCreated_prestador_collectionsVacias_pasadasYNoNulas() {
        Map<String,Object> payload = new HashMap<>();
        payload.put("userId", 99001L);
        payload.put("email", "presta.demo+01@example.com");
        payload.put("firstName", "Ana María");
        payload.put("lastName", "López");
        payload.put("phoneNumber", "+54 11 5555-0009");
        payload.put("role", "PRESTADOR");
        payload.put("dni", "30111222");
        payload.put("address", List.of(Map.of("state","GBA Sur","city","Lomas","street","Alsina","number","2201")));
        payload.put("zones", List.of("GBA Sur"));
        payload.put("skills", List.of()); // ← vacío

        CoreEventMessage cm = mockCoreMessage("evt-X2", payload);

        service.processUserCreatedFromCore(cm);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<Map<String,Object>> cap = ArgumentCaptor.forClass(Map.class);
        verify(dataStorageService).saveProviderData(eq(99001L), cap.capture(), eq("30111222"));
        Map<String,Object> sent = cap.getValue();

        assertTrue(sent.containsKey("skills"));
        assertNotNull(sent.get("skills"));
        assertTrue(((List<?>) sent.get("skills")).isEmpty());
    }

    @Test
    void processUserCreated_prestador_tiposInvalidosEnZonesSkills_seOmiten() {
        Map<String,Object> payload = new HashMap<>();
        payload.put("userId", 1234L);
        payload.put("email", "p@demo.com");
        payload.put("role", "PRESTADOR");
        payload.put("dni", "30");
        payload.put("zones", "NO_LIST");   // tipo inválido
        payload.put("skills", "NO_LIST");  // tipo inválido

        CoreEventMessage cm = mockCoreMessage("evt-X3", payload);

        service.processUserCreatedFromCore(cm);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<Map<String,Object>> cap = ArgumentCaptor.forClass(Map.class);
        verify(dataStorageService).saveProviderData(eq(1234L), cap.capture(), eq("30"));
        Map<String,Object> sent = cap.getValue();

        assertFalse(sent.containsKey("zones"));
        assertFalse(sent.containsKey("skills"));
    }

    @Test
    void processUserCreated_roleNull_vaARamaUserData() {
        Map<String,Object> payload = new HashMap<>();
        payload.put("userId", 77L);
        payload.put("email", "u@x.com");
        payload.put("firstName", "Ada");
        payload.put("lastName", "L");
        payload.put("dni", "11");

        CoreEventMessage cm = mockCoreMessage("evt-X4", payload);

        service.processUserCreatedFromCore(cm);

        verify(dataStorageService).saveUserData(eq(77L), anyMap(), eq("evt-X4"));
    }

    @Test
    void processUserUpdated_prestador_collectionsVacias_hacenClear() {
        Map<String,Object> payload = new HashMap<>();
        payload.put("userId", 4321L);
        payload.put("role", "PRESTADOR");
        payload.put("firstName", "Roberto");
        payload.put("lastName", "S");
        payload.put("zones", List.of());   // vacío
        payload.put("skills", List.of());  // vacío

        CoreEventMessage cm = mockCoreMessage("evt-X5", payload);

        service.processUserUpdatedFromCore(cm);

        verify(dataStorageService).saveProviderData(eq(4321L), argThat(m ->
                m.containsKey("zones") && ((List<?>) m.get("zones")).isEmpty() &&
                        m.containsKey("skills") && ((List<?>) m.get("skills")).isEmpty()
        ), isNull());
    }

    @Test
    void processUserDeactivated_conUserId_llamaDeactivateById() {
        Map<String,Object> payload = Map.of("userId", 11L, "message", "baja pedida");
        CoreEventMessage cm = mockCoreMessage("evt-X6", payload);

        service.processUserDeactivatedFromCore(cm);

        verify(dataStorageService).deactivateUser(11L, "baja pedida");
    }

    @Test
    void processUserDeactivated_conEmail_llamaDeactivateByEmail() {
        Map<String,Object> payload = Map.of("email", "x@y.com", "message", "baja");
        CoreEventMessage cm = mockCoreMessage("evt-X7", payload);

        service.processUserDeactivatedFromCore(cm);

        verify(dataStorageService).deactivateUserByEmail("x@y.com", "baja");
    }

    @Test
    void processUserRejected_conUserId_guardaEstadoRejected() {
        Map<String,Object> payload = Map.of("userId", 22L, "email", "u@x.com", "message", "rechazo KYC");
        CoreEventMessage cm = mockCoreMessage("evt-X8", payload);

        service.processUserRejectedFromCore(cm);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<Map<String,Object>> cap = ArgumentCaptor.forClass(Map.class);
        verify(dataStorageService).saveUserData(eq(22L), cap.capture(), eq("evt-X8"));
        Map<String,Object> sent = cap.getValue();
        assertEquals("REJECTED", sent.get("status"));
        assertEquals("rechazo KYC", sent.get("rejectionReason"));
        assertEquals(false, sent.get("active"));
    }

    @Test
    void processUserRejected_soloEmail_noEncontrado_desactivaPorEmail() {
        when(userDataRepository.findAllByEmail("nada@x.com")).thenReturn(List.of());

        Map<String,Object> payload = Map.of("email", "nada@x.com", "message", "rechazado");
        CoreEventMessage cm = mockCoreMessage("evt-X9", payload);

        service.processUserRejectedFromCore(cm);

        verify(dataStorageService).deactivateUserByEmail("nada@x.com", "rechazado");
    }

    @Test
    void processUserRejected_soloEmail_encontrado_guardaRejected() {
        backend_api.Backend.Entity.UserData u = new backend_api.Backend.Entity.UserData();
        u.setUserId(909L);
        when(userDataRepository.findAllByEmail("encontrado@x.com")).thenReturn(List.of(u));

        Map<String,Object> payload = Map.of("email", "encontrado@x.com", "message", "rechazo");
        CoreEventMessage cm = mockCoreMessage("evt-X10", payload);

        service.processUserRejectedFromCore(cm);

        verify(dataStorageService).saveUserData(eq(909L), anyMap(), eq("evt-X10"));
    }

}