package backend_api.Backend.messaging.dto;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;

class PaymentRequestMessageTest {

    private PaymentRequestMessage message;
    private PaymentRequestMessage.Destination destination;
    private PaymentRequestMessage.Payload payload;
    private PaymentRequestMessage.Cuerpo cuerpo;

    @BeforeEach
    void setUp() {
        message = new PaymentRequestMessage();
        destination = new PaymentRequestMessage.Destination();
        payload = new PaymentRequestMessage.Payload();
        cuerpo = new PaymentRequestMessage.Cuerpo();
    }

    @Test
    void testPaymentRequestMessageCreation() {
        // Given
        message.setMessageId("test-123");
        message.setTimestamp("2025-01-27T20:30:00.000Z");
        message.setSource("matching");

        destination.setTopic("pago");
        destination.setEventName("emitida");
        message.setDestination(destination);

        payload.setSquad("Matching y Agenda");
        payload.setTopico("Pago");
        payload.setEvento("Solicitud Pago Emitida");
        payload.setCuerpo(cuerpo);
        message.setPayload(payload);

        cuerpo.setIdCorrelacion("PED-TEST-123");
        cuerpo.setIdUsuario(999L);
        cuerpo.setIdPrestador(1L);
        cuerpo.setIdSolicitud(555L);
        cuerpo.setMontoSubtotal(new BigDecimal("1000.00"));
        cuerpo.setImpuestos(new BigDecimal("0.00"));
        cuerpo.setComisiones(new BigDecimal("0.00"));
        cuerpo.setMoneda("ARS");
        cuerpo.setMetodoPreferido("MERCADO_PAGO");

        // When & Then
        assertNotNull(message);
        assertEquals("test-123", message.getMessageId());
        assertEquals("2025-01-27T20:30:00.000Z", message.getTimestamp());
        assertEquals("matching", message.getSource());
        assertNotNull(message.getDestination());
        assertNotNull(message.getPayload());
        assertNotNull(message.getPayload().getCuerpo());
    }

    @Test
    void testDestinationGettersAndSetters() {
        // Given
        destination.setTopic("pago");
        destination.setEventName("emitida");

        // When & Then
        assertEquals("pago", destination.getTopic());
        assertEquals("emitida", destination.getEventName());
    }

    @Test
    void testPayloadGettersAndSetters() {
        // Given
        payload.setSquad("Matching y Agenda");
        payload.setTopico("Pago");
        payload.setEvento("Solicitud Pago Emitida");
        payload.setCuerpo(cuerpo);

        // When & Then
        assertEquals("Matching y Agenda", payload.getSquad());
        assertEquals("Pago", payload.getTopico());
        assertEquals("Solicitud Pago Emitida", payload.getEvento());
        assertNotNull(payload.getCuerpo());
    }

    @Test
    void testCuerpoGettersAndSetters() {
        // Given
        cuerpo.setIdCorrelacion("PED-TEST-123");
        cuerpo.setIdUsuario(999L);
        cuerpo.setIdPrestador(1L);
        cuerpo.setIdSolicitud(555L);
        cuerpo.setMontoSubtotal(new BigDecimal("1000.00"));
        cuerpo.setImpuestos(new BigDecimal("50.00"));
        cuerpo.setComisiones(new BigDecimal("25.00"));
        cuerpo.setMoneda("ARS");
        cuerpo.setMetodoPreferido("MERCADO_PAGO");

        // When & Then
        assertEquals("PED-TEST-123", cuerpo.getIdCorrelacion());
        assertEquals(999L, cuerpo.getIdUsuario());
        assertEquals(1L, cuerpo.getIdPrestador());
        assertEquals(555L, cuerpo.getIdSolicitud());
        assertEquals(new BigDecimal("1000.00"), cuerpo.getMontoSubtotal());
        assertEquals(new BigDecimal("50.00"), cuerpo.getImpuestos());
        assertEquals(new BigDecimal("25.00"), cuerpo.getComisiones());
        assertEquals("ARS", cuerpo.getMoneda());
        assertEquals("MERCADO_PAGO", cuerpo.getMetodoPreferido());
    }

    @Test
    void testAllArgsConstructor() {
        // Given
        destination = new PaymentRequestMessage.Destination("pago", "emitida", null);
        cuerpo = new PaymentRequestMessage.Cuerpo(
            "PED-TEST-123", 999L, 1L, 555L, 
            new BigDecimal("1000.00"), new BigDecimal("0.00"), 
            new BigDecimal("0.00"), "ARS", "MERCADO_PAGO"
        );
        payload = new PaymentRequestMessage.Payload("Matching y Agenda", "Pago", "Solicitud Pago Emitida", cuerpo, null, null);
        message = new PaymentRequestMessage("test-123", "2025-01-27T20:30:00.000Z", "matching", destination, payload);

        // When & Then
        assertNotNull(message);
        assertEquals("test-123", message.getMessageId());
        assertEquals("matching", message.getSource());
        assertEquals("pago", message.getDestination().getTopic());
        assertEquals("emitida", message.getDestination().getEventName());
        assertEquals("Matching y Agenda", payload.getSquad());
        assertNotNull(payload.getCuerpo());
        assertEquals(999L, payload.getCuerpo().getIdUsuario());
    }

    @Test
    void testNewMatchingFormat() {
        // Given - nuevo formato de matching
        PaymentRequestMessage.Pago pago = new PaymentRequestMessage.Pago();
        pago.setIdCorrelacion("PED-NEW-456");
        pago.setIdUsuario(901L);
        pago.setIdPrestador(7L);
        pago.setIdSolicitud(220001L);
        pago.setMontoSubtotal(new BigDecimal("24000"));
        pago.setImpuestos(new BigDecimal("0"));
        pago.setComisiones(new BigDecimal("0"));
        pago.setMoneda("ARS");
        pago.setMetodoPreferido("MERCADO_PAGO");

        PaymentRequestMessage.Payload newPayload = new PaymentRequestMessage.Payload();
        newPayload.setGeneratedAt("2025-10-24T23:55:44.896903Z");
        newPayload.setPago(pago);

        PaymentRequestMessage newMessage = new PaymentRequestMessage();
        newMessage.setMessageId("64603733-4599-4e3d-810c-dcd7b3c567c5");
        newMessage.setTimestamp("2025-10-24T23:55:44.896925Z");

        PaymentRequestMessage.Destination newDestination = new PaymentRequestMessage.Destination();
        newDestination.setTopic("pago");
        newDestination.setEventName("emitida");
        newMessage.setDestination(newDestination);
        newMessage.setPayload(newPayload);

        // When & Then
        assertNotNull(newMessage);
        assertEquals("64603733-4599-4e3d-810c-dcd7b3c567c5", newMessage.getMessageId());
        assertEquals("pago", newMessage.getDestination().getTopic());
        assertEquals("emitida", newMessage.getDestination().getEventName());
        assertNotNull(newMessage.getPayload().getPago());
        assertEquals("PED-NEW-456", newMessage.getPayload().getPago().getIdCorrelacion());
        assertEquals(901L, newMessage.getPayload().getPago().getIdUsuario());
        assertEquals(7L, newMessage.getPayload().getPago().getIdPrestador());
    }
}
