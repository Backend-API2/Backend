package backend_api.Backend.messaging.service;

import backend_api.Backend.Entity.UserData;
import backend_api.Backend.Entity.ProviderData;
import backend_api.Backend.Entity.payment.Payment;
import backend_api.Backend.Repository.UserDataRepository;
import backend_api.Backend.Repository.ProviderDataRepository;
import backend_api.Backend.Service.Interface.PaymentService;
import backend_api.Backend.messaging.dto.PaymentRequestMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentRequestProcessorServiceTest {

    @Mock
    private UserDataRepository userDataRepository;

    @Mock
    private ProviderDataRepository providerDataRepository;

    @Mock
    private PaymentService paymentService;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private PaymentRequestProcessorService paymentRequestProcessorService;

    private PaymentRequestMessage message;
    private UserData userData;
    private ProviderData providerData;

    @BeforeEach
    void setUp() {
        // Crear mensaje de prueba
        message = createTestPaymentRequestMessage();
        
        // Crear datos de usuario de prueba
        userData = new UserData();
        userData.setUserId(999L);
        userData.setName("Usuario Test");
        userData.setEmail("usuario@test.com");
        userData.setPhone("123456789");

        // Crear datos de prestador de prueba
        providerData = new ProviderData();
        providerData.setProviderId(1L);
        providerData.setName("Prestador Test");
        providerData.setEmail("prestador@test.com");
        providerData.setPhone("987654321");
    }

    @Test
    void testProcessPaymentRequest_Success() {
        // Given
        when(userDataRepository.findByUserId(999L)).thenReturn(Optional.of(userData));
        when(providerDataRepository.findByProviderId(1L)).thenReturn(Optional.of(providerData));
        
        // Mock PaymentService
        Payment mockPayment = new Payment();
        mockPayment.setId(1L);
        mockPayment.setUser_id(999L);
        mockPayment.setProvider_id(1L);
        mockPayment.setCreated_at(java.time.LocalDateTime.now());
        when(paymentService.createPayment(any(Payment.class))).thenReturn(mockPayment);
        
        // Mock ObjectMapper
        try {
            when(objectMapper.writeValueAsString(any())).thenReturn("{}");
        } catch (Exception e) {
            // This won't happen in tests
        }

        // When
        Map<String, Object> result = paymentRequestProcessorService.processPaymentRequest(message);

        // Then
        assertNotNull(result);
        assertTrue((Boolean) result.get("success"));
        assertEquals("Solicitud de pago procesada exitosamente", result.get("message"));
        assertEquals("test-message-123", result.get("messageId"));
        
        // Verificar que se buscó en los repositorios
        verify(userDataRepository).findByUserId(999L);
        verify(providerDataRepository).findByProviderId(1L);
        verify(paymentService).createPayment(any(Payment.class));
    }

    @Test
    void testProcessPaymentRequest_UserNotFound() {
        // Given
        when(userDataRepository.findByUserId(999L)).thenReturn(Optional.empty());

        // When
        Map<String, Object> result = paymentRequestProcessorService.processPaymentRequest(message);

        // Then
        assertNotNull(result);
        assertFalse((Boolean) result.get("success"));
        assertEquals("Usuario no encontrado", result.get("error"));
        assertEquals(999L, result.get("userId"));
        assertEquals("test-message-123", result.get("messageId"));
        
        verify(userDataRepository).findByUserId(999L);
        verify(providerDataRepository, never()).findByProviderId(anyLong());
    }

    @Test
    void testProcessPaymentRequest_ProviderNotFound() {
        // Given
        when(userDataRepository.findByUserId(999L)).thenReturn(Optional.of(userData));
        when(providerDataRepository.findByProviderId(1L)).thenReturn(Optional.empty());

        // When
        Map<String, Object> result = paymentRequestProcessorService.processPaymentRequest(message);

        // Then
        assertNotNull(result);
        assertFalse((Boolean) result.get("success"));
        assertEquals("Prestador no encontrado", result.get("error"));
        assertEquals(1L, result.get("providerId"));
        assertEquals("test-message-123", result.get("messageId"));
        
        verify(userDataRepository).findByUserId(999L);
        verify(providerDataRepository).findByProviderId(1L);
    }

    @Test
    void testProcessPaymentRequest_Exception() {
        // Given
        when(userDataRepository.findByUserId(999L)).thenThrow(new RuntimeException("Database error"));

        // When
        Map<String, Object> result = paymentRequestProcessorService.processPaymentRequest(message);

        // Then
        assertNotNull(result);
        assertFalse((Boolean) result.get("success"));
        assertTrue(((String) result.get("error")).contains("Error procesando solicitud"));
        assertEquals("test-message-123", result.get("messageId"));
    }

    @Test
    void testProcessPaymentRequest_DataExtraction() {
        // Given
        when(userDataRepository.findByUserId(999L)).thenReturn(Optional.of(userData));
        when(providerDataRepository.findByProviderId(1L)).thenReturn(Optional.of(providerData));
        
        // Mock PaymentService
        Payment mockPayment = new Payment();
        mockPayment.setId(1L);
        mockPayment.setUser_id(999L);
        mockPayment.setProvider_id(1L);
        mockPayment.setCreated_at(java.time.LocalDateTime.now());
        when(paymentService.createPayment(any(Payment.class))).thenReturn(mockPayment);
        
        // Mock ObjectMapper
        try {
            when(objectMapper.writeValueAsString(any())).thenReturn("{}");
        } catch (Exception e) {
            // This won't happen in tests
        }

        // When
        Map<String, Object> result = paymentRequestProcessorService.processPaymentRequest(message);

        // Then
        assertNotNull(result);
        assertTrue((Boolean) result.get("success"));
        
        // Verificar datos del usuario
        @SuppressWarnings("unchecked")
        Map<String, Object> userDataResult = (Map<String, Object>) result.get("userData");
        assertNotNull(userDataResult);
        assertEquals(999L, userDataResult.get("userId"));
        assertEquals("Usuario Test", userDataResult.get("name"));
        assertEquals("usuario@test.com", userDataResult.get("email"));

        // Verificar datos del prestador
        @SuppressWarnings("unchecked")
        Map<String, Object> providerDataResult = (Map<String, Object>) result.get("providerData");
        assertNotNull(providerDataResult);
        assertEquals(1L, providerDataResult.get("providerId"));
        assertEquals("Prestador Test", providerDataResult.get("name"));
        assertEquals("prestador@test.com", providerDataResult.get("email"));
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
