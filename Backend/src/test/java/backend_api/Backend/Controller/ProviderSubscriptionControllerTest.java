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

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class ProviderSubscriptionControllerTest {

    @Mock
    private CoreHubService coreHubService;

    @InjectMocks
    private ProviderSubscriptionController controller;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void subscribeAlta_ok() throws Exception {
        doNothing().when(coreHubService).subscribeToTopic("catalogue", "prestador", "alta");

        mockMvc.perform(post("/api/providers/subscriptions/subscribe-alta"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.topic").value("catalogue.prestador.alta"));

        verify(coreHubService).subscribeToTopic("catalogue", "prestador", "alta");
    }

    @Test
    void subscribeModificacion_ok() throws Exception {
        doNothing().when(coreHubService).subscribeToTopic("catalogue", "prestador", "modificacion");

        mockMvc.perform(post("/api/providers/subscriptions/subscribe-modificacion"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.topic").value("catalogue.prestador.modificacion"));

        verify(coreHubService).subscribeToTopic("catalogue", "prestador", "modificacion");
    }

    @Test
    void subscribeBaja_ok() throws Exception {
        doNothing().when(coreHubService).subscribeToTopic("catalogue", "prestador", "baja");

        mockMvc.perform(post("/api/providers/subscriptions/subscribe-baja"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.topic").value("catalogue.prestador.baja"));

        verify(coreHubService).subscribeToTopic("catalogue", "prestador", "baja");
    }

    @Test
    void subscribeAll_ok() throws Exception {
        doNothing().when(coreHubService).subscribeToTopic(anyString(), anyString(), anyString());

        mockMvc.perform(post("/api/providers/subscriptions/subscribe-all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.subscriptions[0]").value("catalogue.prestador.alta"))
                .andExpect(jsonPath("$.subscriptions[1]").value("catalogue.prestador.modificacion"))
                .andExpect(jsonPath("$.subscriptions[2]").value("catalogue.prestador.baja"));

        verify(coreHubService).subscribeToTopic("catalogue", "prestador", "alta");
        verify(coreHubService).subscribeToTopic("catalogue", "prestador", "modificacion");
        verify(coreHubService).subscribeToTopic("catalogue", "prestador", "baja");
    }
}