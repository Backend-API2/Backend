package backend_api.Backend.Controller;

import backend_api.Backend.messaging.service.CoreHubService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class DataSubscriptionControllerMatchingTest {

    @Mock
    private CoreHubService coreHubService;

    @InjectMocks
    private DataSubscriptionController dataSubscriptionController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(dataSubscriptionController).build();
    }

    @Test
    void testSubscribeToMatchingPayments_Success() throws Exception {
        // Given
        doNothing().when(coreHubService).subscribeToTopic(anyString(), anyString(), anyString());

        // When & Then
        mockMvc.perform(post("/api/data/subscriptions/subscribe-matching-payments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.message").value("Suscripción a solicitudes de pago de matching creada exitosamente"))
                .andExpect(jsonPath("$.topic").value("matching.pago.emitida"))
                .andExpect(jsonPath("$.webhookUrl").value("https://3aadd844682e.ngrok-free.app/api/core/webhook/matching-payment-requests"));

        verify(coreHubService).subscribeToTopic("matching", "pago", "emitida");
    }

    @Test
    void testSubscribeToMatchingPayments_Error() throws Exception {
        // Given
        doThrow(new RuntimeException("Subscription error"))
            .when(coreHubService).subscribeToTopic(anyString(), anyString(), anyString());

        // When & Then
        mockMvc.perform(post("/api/data/subscriptions/subscribe-matching-payments"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value("error"))
                .andExpect(jsonPath("$.message").value("Error creando suscripción a matching: Subscription error"));

        verify(coreHubService).subscribeToTopic("matching", "pago", "emitida");
    }

    @Test
    void testGetSubscriptionStatus() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/data/subscriptions/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("active"))
                .andExpect(jsonPath("$.subscriptions").isArray())
                .andExpect(jsonPath("$.subscriptions[0]").value("user.create_user"))
                .andExpect(jsonPath("$.subscriptions[1]").value("user.update_user"))
                .andExpect(jsonPath("$.subscriptions[2]").value("user.deactivate_user"))
                .andExpect(jsonPath("$.subscriptions[3]").value("pago.emitida"))
                .andExpect(jsonPath("$.webhooks").isMap())
                .andExpect(jsonPath("$.webhooks.user-events").value("/api/core/webhook/user-events"))
                .andExpect(jsonPath("$.webhooks.payment-events").value("/api/core/webhook/payment-events"))
                .andExpect(jsonPath("$.webhooks.matching-payment-requests").value("/api/core/webhook/matching-payment-requests"));
    }

    @Test
    void testCheckCoreHubConnection() throws Exception {
        // Given
        when(coreHubService.checkConnection()).thenReturn(java.util.Map.of(
            "status", "CONFIGURED",
            "coreHubUrl", "https://nonprodapi.uade-corehub.com",
            "teamName", "payments"
        ));

        // When & Then
        mockMvc.perform(get("/api/data/subscriptions/connection"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CONFIGURED"))
                .andExpect(jsonPath("$.coreHubUrl").value("https://nonprodapi.uade-corehub.com"))
                .andExpect(jsonPath("$.teamName").value("payments"));

        verify(coreHubService).checkConnection();
    }

    @Test
    void testCheckCoreHubConnection_Error() throws Exception {
        // Given
        when(coreHubService.checkConnection()).thenThrow(new RuntimeException("Connection error"));

        // When & Then
        mockMvc.perform(get("/api/data/subscriptions/connection"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value("error"))
                .andExpect(jsonPath("$.message").value("Error verificando conexión: Connection error"));

        verify(coreHubService).checkConnection();
    }
}
