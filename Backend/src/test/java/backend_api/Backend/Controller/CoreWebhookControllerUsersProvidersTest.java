package backend_api.Backend.Controller;

import backend_api.Backend.messaging.dto.CoreEventMessage;
import backend_api.Backend.messaging.service.CoreHubService;
import backend_api.Backend.messaging.service.ProviderEventProcessorService;
import backend_api.Backend.messaging.service.UserEventProcessorService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class CoreWebhookControllerUsersProvidersTest {

    @Mock
    private CoreHubService coreHubService;

    @Mock
    private UserEventProcessorService userEventProcessorService;

    @Mock
    private ProviderEventProcessorService providerEventProcessorService;

    @InjectMocks
    private CoreWebhookController coreWebhookController;

    private MockMvc mockMvc;
    private ObjectMapper om;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(coreWebhookController).build();
        om = new ObjectMapper();
    }

    /* ---------- PROVIDERS ---------- */

    @Test
    void provider_alta_routed_to_providerProcessor() throws Exception {
        CoreEventMessage msg = new CoreEventMessage();
        msg.setMessageId("m-prov-1");
        msg.setTimestamp(LocalDateTime.parse("2025-01-01T10:00:00.000Z"));

        CoreEventMessage.Destination dest = new CoreEventMessage.Destination();
        dest.setChannel("catalogue.prestador.alta");
        dest.setEventName("alta_prestador");
        msg.setDestination(dest);

        java.util.Map<String, Object> payload = new java.util.HashMap<>();
        payload.put("id", 58);
        payload.put("nombre", "Prestadorcito");
        payload.put("apellido", "De ejemplo");
        payload.put("email", "prestador@example.com");
        payload.put("telefono", "11111111");
        payload.put("dni", "12345678");
        payload.put("activo", 1);
        payload.put("habilidades", java.util.List.of("Plomería", "Gas"));
        payload.put("zonas", java.util.List.of("Belgrano", "Flores"));
        msg.setPayload(payload);

        doNothing().when(providerEventProcessorService).processProviderFromCore(any(CoreEventMessage.class));
        doNothing().when(coreHubService).sendAck(anyString(), anyString());

        mockMvc.perform(post("/api/core/webhook/core-usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(msg)))
                .andExpect(status().isOk());

        verify(providerEventProcessorService).processProviderFromCore(any(CoreEventMessage.class));
        verify(coreHubService).sendAck(eq("m-prov-1"), anyString());
    }

    @Test
    void provider_baja_routed_to_providerProcessor() throws Exception {
        CoreEventMessage msg = new CoreEventMessage();
        msg.setMessageId("m-prov-2");
        msg.setTimestamp(LocalDateTime.parse("2025-01-01T10:00:00.000Z"));
        CoreEventMessage.Destination dest = new CoreEventMessage.Destination();
        dest.setChannel("catalogue.prestador.baja");
        dest.setEventName("baja_prestador");
        msg.setDestination(dest);
        msg.setPayload(java.util.Map.of("id", 77, "activo", 0));

        doNothing().when(providerEventProcessorService).processProviderFromCore(any(CoreEventMessage.class));
        doNothing().when(coreHubService).sendAck(anyString(), anyString());

        mockMvc.perform(post("/api/core/webhook/core-usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(msg)))
                .andExpect(status().isOk());

        verify(providerEventProcessorService).processProviderFromCore(any(CoreEventMessage.class));
        verify(coreHubService).sendAck(eq("m-prov-2"), anyString());
    }

    /* ---------- USERS ---------- */

    @Test
    void user_create_routed_to_userProcessor() throws Exception {
        CoreEventMessage msg = new CoreEventMessage();
        msg.setMessageId("m-user-1");
        msg.setTimestamp(LocalDateTime.parse("2025-01-01T10:00:00.000Z"));

        CoreEventMessage.Destination dest = new CoreEventMessage.Destination();
        dest.setChannel("users.user.create_user");
        dest.setEventName("create_user");
        msg.setDestination(dest);

        msg.setPayload(java.util.Map.of(
                "id", 999,
                "email", "user@example.com",
                "firstName", "Ada",
                "lastName", "Lovelace",
                "phoneNumber", "+549111111",
                "role", "CLIENTE",
                "dni", "12345678"
        ));

        doNothing().when(userEventProcessorService).processUserCreatedFromCore(any(CoreEventMessage.class));
        doNothing().when(coreHubService).sendAck(anyString(), anyString());

        mockMvc.perform(post("/api/core/webhook/core-usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(msg)))
                .andExpect(status().isOk());

        verify(userEventProcessorService).processUserCreatedFromCore(any(CoreEventMessage.class));
        verify(coreHubService).sendAck(eq("m-user-1"), anyString());
    }

    /* ---------- HEALTH ---------- */

    @Test
    void webhook_health_ok() throws Exception {
        mockMvc.perform(get("/api/core/webhook/health"))
                .andExpect(status().isOk());
    }
}