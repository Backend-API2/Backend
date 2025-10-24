package backend_api.Backend.Controller;

import backend_api.Backend.messaging.dto.PaymentRequestMessage;
import backend_api.Backend.messaging.service.CoreHubService;
import backend_api.Backend.messaging.service.PaymentRequestProcessorService;
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

import java.math.BigDecimal;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class CoreWebhookControllerMatchingTest {

    @Mock
    private PaymentRequestProcessorService paymentRequestProcessorService;

    @Mock
    private CoreHubService coreHubService;

    @InjectMocks
    private CoreWebhookController coreWebhookController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(coreWebhookController).build();
        objectMapper = new ObjectMapper();
    }

    @Test
    void testReceiveMatchingPaymentRequest_Success() throws Exception {
        // Given
        PaymentRequestMessage message = createTestPaymentRequestMessage();
        Map<String, Object> processorResult = Map.of(
            "success", true,
            "message", "Solicitud de pago procesada exitosamente",
            "messageId", "test-message-123"
        );

        when(paymentRequestProcessorService.processPaymentRequest(any(PaymentRequestMessage.class)))
            .thenReturn(processorResult);
        doNothing().when(coreHubService).sendAck(anyString(), anyString());

        // When & Then
        mockMvc.perform(post("/api/core/webhook/matching-payment-requests")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(message)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Solicitud de pago procesada exitosamente"))
                .andExpect(jsonPath("$.messageId").value("test-message-123"));

        verify(paymentRequestProcessorService).processPaymentRequest(any(PaymentRequestMessage.class));
        verify(coreHubService).sendAck(anyString(), anyString());
    }

    @Test
    void testReceiveMatchingPaymentRequest_ProcessingError() throws Exception {
        // Given
        PaymentRequestMessage message = createTestPaymentRequestMessage();
        Map<String, Object> processorResult = Map.of(
            "success", false,
            "error", "Usuario no encontrado",
            "messageId", "test-message-123"
        );

        when(paymentRequestProcessorService.processPaymentRequest(any(PaymentRequestMessage.class)))
            .thenReturn(processorResult);

        // When & Then
        mockMvc.perform(post("/api/core/webhook/matching-payment-requests")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(message)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error").value("Usuario no encontrado"))
                .andExpect(jsonPath("$.messageId").value("test-message-123"));

        verify(paymentRequestProcessorService).processPaymentRequest(any(PaymentRequestMessage.class));
        verify(coreHubService, never()).sendAck(anyString(), anyString());
    }

    @Test
    void testReceiveMatchingPaymentRequest_Exception() throws Exception {
        // Given
        PaymentRequestMessage message = createTestPaymentRequestMessage();

        when(paymentRequestProcessorService.processPaymentRequest(any(PaymentRequestMessage.class)))
            .thenThrow(new RuntimeException("Processing error"));

        // When & Then
        mockMvc.perform(post("/api/core/webhook/matching-payment-requests")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(message)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.status").value("error"))
                .andExpect(jsonPath("$.messageId").value("test-message-123"))
                .andExpect(jsonPath("$.error").value("Processing error"))
                .andExpect(jsonPath("$.retryAfter").value("30"));

        verify(paymentRequestProcessorService).processPaymentRequest(any(PaymentRequestMessage.class));
        verify(coreHubService, never()).sendAck(anyString(), anyString());
    }

    @Test
    void testReceiveMatchingPaymentRequest_InvalidJson() throws Exception {
        // Given
        String invalidJson = "{ invalid json }";

        // When & Then
        mockMvc.perform(post("/api/core/webhook/matching-payment-requests")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidJson))
                .andExpect(status().isBadRequest());

        verify(paymentRequestProcessorService, never()).processPaymentRequest(any(PaymentRequestMessage.class));
        verify(coreHubService, never()).sendAck(anyString(), anyString());
    }

    @Test
    void testWebhookHealth() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/core/webhook/health"))
                .andExpect(status().isMethodNotAllowed()); // GET method expected

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/api/core/webhook/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.service").value("CORE Webhook Receiver"));
    }

    private PaymentRequestMessage createTestPaymentRequestMessage() {
        PaymentRequestMessage message = new PaymentRequestMessage();
        message.setMessageId("test-message-123");
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
        cuerpo.setIdCorrelacion("PED-TEST-123");
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
