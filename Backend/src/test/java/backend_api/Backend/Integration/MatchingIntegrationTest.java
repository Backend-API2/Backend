package backend_api.Backend.Integration;

import backend_api.Backend.Entity.UserData;
import backend_api.Backend.Entity.ProviderData;
import backend_api.Backend.Repository.UserDataRepository;
import backend_api.Backend.Repository.ProviderDataRepository;
import backend_api.Backend.messaging.dto.PaymentRequestMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureWebMvc
@ActiveProfiles("test")
@Transactional
class MatchingIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserDataRepository userDataRepository;

    @Autowired
    private ProviderDataRepository providerDataRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private UserData testUser;
    private ProviderData testProvider;

    @BeforeEach
    void setUp() {
        // Crear usuario de prueba
        testUser = new UserData();
        testUser.setUserId(999L);
        testUser.setName("Usuario Test");
        testUser.setEmail("usuario@test.com");
        testUser.setPhone("123456789");
        userDataRepository.save(testUser);

        // Crear prestador de prueba
        testProvider = new ProviderData();
        testProvider.setProviderId(1L);
        testProvider.setName("Prestador Test");
        testProvider.setEmail("prestador@test.com");
        testProvider.setPhone("987654321");
        providerDataRepository.save(testProvider);
    }

    @Test
    void testMatchingPaymentRequestFlow_Success() throws Exception {
        // Given
        PaymentRequestMessage message = createTestPaymentRequestMessage();

        // When & Then - Probar webhook de matching
        mockMvc.perform(post("/api/core/webhook/matching-payment-requests")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(message)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Solicitud de pago procesada exitosamente"))
                .andExpect(jsonPath("$.messageId").value("test-integration-123"))
                .andExpect(jsonPath("$.userData.userId").value(999))
                .andExpect(jsonPath("$.userData.name").value("Usuario Test"))
                .andExpect(jsonPath("$.providerData.providerId").value(1))
                .andExpect(jsonPath("$.providerData.name").value("Prestador Test"));
    }

    @Test
    void testMatchingPaymentRequestFlow_UserNotFound() throws Exception {
        // Given
        PaymentRequestMessage message = createTestPaymentRequestMessage();
        message.getPayload().getCuerpo().setIdUsuario(9999L); // Usuario que no existe

        // When & Then
        mockMvc.perform(post("/api/core/webhook/matching-payment-requests")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(message)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error").value("Usuario no encontrado"))
                .andExpect(jsonPath("$.userId").value(9999));
    }

    @Test
    void testMatchingPaymentRequestFlow_ProviderNotFound() throws Exception {
        // Given
        PaymentRequestMessage message = createTestPaymentRequestMessage();
        message.getPayload().getCuerpo().setIdPrestador(9999L); // Prestador que no existe

        // When & Then
        mockMvc.perform(post("/api/core/webhook/matching-payment-requests")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(message)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error").value("Prestador no encontrado"))
                .andExpect(jsonPath("$.providerId").value(9999));
    }

    @Test
    void testSubscriptionEndpoints() throws Exception {
        // When & Then - Probar endpoint de suscripción
        mockMvc.perform(post("/api/data/subscriptions/subscribe-matching-payments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.message").value("Suscripción a solicitudes de pago de matching creada exitosamente"))
                .andExpect(jsonPath("$.topic").value("matching.pago.emitida"));

        // When & Then - Probar endpoint de estado
        mockMvc.perform(get("/api/data/subscriptions/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("active"))
                .andExpect(jsonPath("$.subscriptions").isArray())
                .andExpect(jsonPath("$.webhooks.matching-payment-requests").value("/api/core/webhook/matching-payment-requests"));
    }

    @Test
    void testWebhookHealth() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/core/webhook/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.service").value("CORE Webhook Receiver"));
    }

    @Test
    void testInvalidJsonRequest() throws Exception {
        // Given
        String invalidJson = "{ invalid json }";

        // When & Then
        mockMvc.perform(post("/api/core/webhook/matching-payment-requests")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidJson))
                .andExpect(status().isBadRequest());
    }

    private PaymentRequestMessage createTestPaymentRequestMessage() {
        PaymentRequestMessage message = new PaymentRequestMessage();
        message.setMessageId("test-integration-123");
        message.setTimestamp("2025-01-27T20:30:00.000Z");
        message.setSource("matching");

        PaymentRequestMessage.Destination destination = new PaymentRequestMessage.Destination();
        destination.setChannel("matching.pago.emitida");
        destination.setEventName("emitida");
        message.setDestination(destination);

        PaymentRequestMessage.Payload payload = new PaymentRequestMessage.Payload();
        payload.setSquad("Matching y Agenda");
        payload.setTopico("Pago");
        payload.setEvento("Solicitud Pago Emitida");

        PaymentRequestMessage.Cuerpo cuerpo = new PaymentRequestMessage.Cuerpo();
        cuerpo.setIdCorrelacion("INTEGRATION-TEST-123");
        cuerpo.setIdUsuario(999L);
        cuerpo.setIdPrestador(1L);
        cuerpo.setIdSolicitud(555L);
        cuerpo.setMontoSubtotal(new BigDecimal("1000.00"));
        cuerpo.setImpuestos(new BigDecimal("50.00"));
        cuerpo.setComisiones(new BigDecimal("25.00"));
        cuerpo.setMoneda("ARS");
        cuerpo.setMetodoPreferido("MERCADO_PAGO");

        payload.setCuerpo(cuerpo);
        message.setPayload(payload);

        return message;
    }
}
