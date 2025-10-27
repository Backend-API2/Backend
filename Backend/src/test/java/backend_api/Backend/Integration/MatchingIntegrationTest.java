package backend_api.Backend.Integration;

import backend_api.Backend.Entity.UserData;
import backend_api.Backend.Entity.ProviderData;
import backend_api.Backend.Repository.UserDataRepository;
import backend_api.Backend.Repository.ProviderDataRepository;
import backend_api.Backend.messaging.dto.PaymentRequestMessage;
import backend_api.Backend.messaging.service.PaymentRequestProcessorService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class MatchingIntegrationTest {

    @Autowired
    private UserDataRepository userDataRepository;

    @Autowired
    private ProviderDataRepository providerDataRepository;

    @Autowired
    private PaymentRequestProcessorService paymentRequestProcessorService;

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
    void testMatchingPaymentRequestFlow_Success() {
        // Given
        PaymentRequestMessage message = createTestPaymentRequestMessage();

        // When
        Map<String, Object> result = paymentRequestProcessorService.processPaymentRequest(message);

        // Then
        assertNotNull(result);
        assertTrue((Boolean) result.get("success"));
        assertEquals("Solicitud de pago procesada exitosamente", result.get("message"));
        assertEquals("test-integration-123", result.get("messageId"));
        
        @SuppressWarnings("unchecked")
        Map<String, Object> userData = (Map<String, Object>) result.get("userData");
        assertNotNull(userData);
        assertEquals(999L, userData.get("userId"));
        assertEquals("Usuario Test", userData.get("name"));
        
        @SuppressWarnings("unchecked")
        Map<String, Object> providerData = (Map<String, Object>) result.get("providerData");
        assertNotNull(providerData);
        assertEquals(1L, providerData.get("providerId"));
        assertEquals("Prestador Test", providerData.get("name"));
    }

    @Test
    void testMatchingPaymentRequestFlow_UserNotFound() {
        // Given
        PaymentRequestMessage message = createTestPaymentRequestMessage();
        message.getPayload().getCuerpo().setIdUsuario(9999L); // Usuario que no existe

        // When
        Map<String, Object> result = paymentRequestProcessorService.processPaymentRequest(message);

        // Then
        assertNotNull(result);
        assertTrue((Boolean) result.get("success"));
        assertEquals("Solicitud de pago procesada exitosamente", result.get("message"));
        assertNull(result.get("userData")); // Usuario no encontrado en BD
        assertNotNull(result.get("paymentData")); // Pero el pago se procesa
    }

    @Test
    void testMatchingPaymentRequestFlow_ProviderNotFound() {
        // Given
        PaymentRequestMessage message = createTestPaymentRequestMessage();
        message.getPayload().getCuerpo().setIdPrestador(9999L); // Prestador que no existe

        // When
        Map<String, Object> result = paymentRequestProcessorService.processPaymentRequest(message);

        // Then
        assertNotNull(result);
        assertTrue((Boolean) result.get("success"));
        assertEquals("Solicitud de pago procesada exitosamente", result.get("message"));
        assertNotNull(result.get("userData")); // Usuario sí encontrado en BD
        assertNull(result.get("providerData")); // Prestador no encontrado en BD
        assertNotNull(result.get("paymentData")); // Pero el pago se procesa
    }

    @Test
    void testPaymentRequestMessageSerialization() throws Exception {
        // Given
        PaymentRequestMessage message = createTestPaymentRequestMessage();

        // When
        String json = objectMapper.writeValueAsString(message);
        PaymentRequestMessage deserializedMessage = objectMapper.readValue(json, PaymentRequestMessage.class);

        // Then
        assertNotNull(json);
        assertNotNull(deserializedMessage);
        assertEquals(message.getMessageId(), deserializedMessage.getMessageId());
        assertEquals(message.getSource(), deserializedMessage.getSource());
        assertEquals(message.getDestination().getChannel(), deserializedMessage.getDestination().getChannel());
        assertEquals(message.getPayload().getCuerpo().getIdUsuario(), deserializedMessage.getPayload().getCuerpo().getIdUsuario());
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
