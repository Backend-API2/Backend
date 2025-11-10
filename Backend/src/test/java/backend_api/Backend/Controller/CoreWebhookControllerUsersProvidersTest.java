package backend_api.Backend.Controller;

import backend_api.Backend.messaging.dto.CoreEventMessage;
import backend_api.Backend.messaging.service.CoreHubService;
import backend_api.Backend.messaging.service.ProviderEventProcessorService;
import backend_api.Backend.messaging.service.UserEventProcessorService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class CoreWebhookControllerUsersProvidersTest {

    @Mock private CoreHubService coreHubService; // lo dejamos mockeado por si después querés verificarlo
    @Mock private UserEventProcessorService userEventProcessorService;
    @Mock private ProviderEventProcessorService providerEventProcessorService;

    @InjectMocks
    private CoreWebhookController coreWebhookController;

    private MockMvc mockMvc;
    private ObjectMapper om;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(coreWebhookController).build();
        om = JsonMapper.builder().addModule(new JavaTimeModule()).build();
        om.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    /* ---------- PROVIDERS ---------- */
    @Test
    void provider_alta_routed_to_providerProcessor() throws Exception {
        CoreEventMessage msg = new CoreEventMessage();
        msg.setMessageId("m-prov-1");
        msg.setTimestamp(LocalDateTime.of(2025, 1, 1, 10, 0, 0));

        CoreEventMessage.Destination dest = new CoreEventMessage.Destination();
        dest.setChannel("catalogue.prestador.alta");
        dest.setEventName("alta_prestador");
        msg.setDestination(dest);

        Map<String, Object> payload = new HashMap<>();
        payload.put("id", 58);
        payload.put("nombre", "Prestadorcito");
        payload.put("apellido", "De ejemplo");
        payload.put("email", "prestador@example.com");
        payload.put("telefono", "11111111");
        payload.put("dni", "12345678");
        payload.put("activo", 1);
        payload.put("habilidades", List.of("Plomería", "Gas"));
        payload.put("zonas", List.of("Belgrano", "Flores"));
        msg.setPayload(payload);

        // este sí se usa en el controller
        doNothing().when(providerEventProcessorService).processProviderFromCore(any(CoreEventMessage.class));

        mockMvc.perform(post("/api/core/webhook/provider-events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(msg)))
                .andExpect(status().isOk());

        verify(providerEventProcessorService).processProviderFromCore(any(CoreEventMessage.class));
        // NO stubbear ni verificar coreHubService.sendAck() porque el controller no lo llama
    }

    @Test
    void provider_baja_routed_to_providerProcessor() throws Exception {
        CoreEventMessage msg = new CoreEventMessage();
        msg.setMessageId("m-prov-2");
        msg.setTimestamp(LocalDateTime.of(2025, 1, 1, 10, 0, 0));

        CoreEventMessage.Destination dest = new CoreEventMessage.Destination();
        dest.setChannel("catalogue.prestador.baja");
        dest.setEventName("baja_prestador");
        msg.setDestination(dest);
        msg.setPayload(Map.of("id", 77, "activo", 0));

        doNothing().when(providerEventProcessorService).processProviderFromCore(any(CoreEventMessage.class));

        mockMvc.perform(post("/api/core/webhook/provider-events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(msg)))
                .andExpect(status().isOk());

        verify(providerEventProcessorService).processProviderFromCore(any(CoreEventMessage.class));
    }

    /* ---------- USERS ---------- */
    @Test
    void user_create_routed_to_userProcessor() throws Exception {
        CoreEventMessage msg = new CoreEventMessage();
        msg.setMessageId("m-user-1");
        msg.setTimestamp(LocalDateTime.of(2025, 1, 1, 10, 0, 0));

        CoreEventMessage.Destination dest = new CoreEventMessage.Destination();
        dest.setChannel("user");
        dest.setEventName("user_created");
        msg.setDestination(dest);

        msg.setPayload(Map.of(
                "id", 999,
                "email", "user@example.com",
                "firstName", "Ada",
                "lastName", "Lovelace",
                "phoneNumber", "+549111111",
                "role", "CLIENTE",
                "dni", "12345678"
        ));

        doNothing().when(userEventProcessorService).processUserCreatedFromCore(any(CoreEventMessage.class));

        mockMvc.perform(post("/api/core/webhook/user-events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(msg)))
                .andExpect(status().isOk());

        verify(userEventProcessorService).processUserCreatedFromCore(any(CoreEventMessage.class));
    }

    @Test
    void user_update_routed_to_userProcessor() throws Exception {
        CoreEventMessage msg = new CoreEventMessage();
        msg.setMessageId("m-user-2");
        msg.setTimestamp(LocalDateTime.of(2025, 1, 1, 11, 0));

        CoreEventMessage.Destination dest = new CoreEventMessage.Destination();
        dest.setChannel("user");
        dest.setEventName("user_updated");
        msg.setDestination(dest);

        msg.setPayload(Map.of("userId", 100L));

        doNothing().when(userEventProcessorService).processUserUpdatedFromCore(any(CoreEventMessage.class));

        mockMvc.perform(post("/api/core/webhook/user-events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(msg)))
                .andExpect(status().isOk());

        verify(userEventProcessorService).processUserUpdatedFromCore(any(CoreEventMessage.class));
    }

    @Test
    void user_deactivated_routed_to_userProcessor() throws Exception {
        CoreEventMessage msg = new CoreEventMessage();
        msg.setMessageId("m-user-3");
        msg.setTimestamp(LocalDateTime.of(2025, 1, 1, 12, 0));

        CoreEventMessage.Destination dest = new CoreEventMessage.Destination();
        dest.setChannel("user");
        dest.setEventName("user_deactivated");
        msg.setDestination(dest);

        msg.setPayload(Map.of("userId", 101L, "message", "baja"));

        doNothing().when(userEventProcessorService).processUserDeactivatedFromCore(any(CoreEventMessage.class));

        mockMvc.perform(post("/api/core/webhook/user-events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(msg)))
                .andExpect(status().isOk());

        verify(userEventProcessorService).processUserDeactivatedFromCore(any(CoreEventMessage.class));
    }

    @Test
    void user_rejected_routed_to_userProcessor() throws Exception {
        CoreEventMessage msg = new CoreEventMessage();
        msg.setMessageId("m-user-4");
        msg.setTimestamp(LocalDateTime.of(2025, 1, 1, 13, 0));

        CoreEventMessage.Destination dest = new CoreEventMessage.Destination();
        dest.setChannel("user");
        dest.setEventName("user_rejected");
        msg.setDestination(dest);

        msg.setPayload(Map.of("email", "rej@x.com", "message", "rechazo"));

        doNothing().when(userEventProcessorService).processUserRejectedFromCore(any(CoreEventMessage.class));

        mockMvc.perform(post("/api/core/webhook/user-events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(msg)))
                .andExpect(status().isOk());

        verify(userEventProcessorService).processUserRejectedFromCore(any(CoreEventMessage.class));
    }

    /* ---------- HEALTH ---------- */
    @Test
    void webhook_health_ok() throws Exception {
        mockMvc.perform(get("/api/core/webhook/health"))
                .andExpect(status().isOk());
    }
}