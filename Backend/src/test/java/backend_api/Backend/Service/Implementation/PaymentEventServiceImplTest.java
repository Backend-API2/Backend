package backend_api.Backend.Service.Implementation;

import backend_api.Backend.Entity.payment.PaymentEvent;
import backend_api.Backend.Entity.payment.PaymentEventType;
import backend_api.Backend.Repository.PaymentEventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

/**
 * Unit tests for PaymentEventServiceImpl
 * Testing payment event management operations
 */
@ExtendWith(MockitoExtension.class)
class PaymentEventServiceImplTest {

    @Mock
    private PaymentEventRepository paymentEventRepository;

    @InjectMocks
    private PaymentEventServiceImpl paymentEventService;

    private PaymentEvent testEvent;
    private final Long paymentId = 1L;
    private final String actor = "test-user";

    @BeforeEach
    void setUp() {
        testEvent = new PaymentEvent();
        testEvent.setId(1L);
        testEvent.setPaymentId(paymentId);
        testEvent.setType(PaymentEventType.PAYMENT_PENDING);
        testEvent.setPayload("Test payload");
        testEvent.setActor(actor);
        testEvent.setEventSource("SYSTEM");
        testEvent.setCreatedAt(LocalDateTime.now());
    }

    @Test
    void testCreateEvent_BasicOverload() {
        // Given
        when(paymentEventRepository.save(any(PaymentEvent.class))).thenReturn(testEvent);

        // When
        PaymentEvent result = paymentEventService.createEvent(
                paymentId, PaymentEventType.PAYMENT_PENDING, "Test payload", actor);

        // Then
        assertNotNull(result);
        assertEquals(paymentId, result.getPaymentId());
        assertEquals(PaymentEventType.PAYMENT_PENDING, result.getType());
        assertEquals("Test payload", result.getPayload());
        assertEquals(actor, result.getActor());
        assertEquals("SYSTEM", result.getEventSource());
        verify(paymentEventRepository).save(argThat(event -> 
                event.getPaymentId().equals(paymentId) &&
                event.getType() == PaymentEventType.PAYMENT_PENDING &&
                event.getPayload().equals("Test payload") &&
                event.getActor().equals(actor) &&
                event.getEventSource().equals("SYSTEM")
        ));
    }

    @Test
    void testCreateEvent_FullOverload() {
        // Given
        when(paymentEventRepository.save(any(PaymentEvent.class))).thenReturn(testEvent);

        // When
        PaymentEvent result = paymentEventService.createEvent(
                paymentId, PaymentEventType.PAYMENT_PENDING, "Test payload", actor, "CUSTOM_SOURCE");

        // Then
        assertNotNull(result);
        assertEquals(paymentId, result.getPaymentId());
        assertEquals(PaymentEventType.PAYMENT_PENDING, result.getType());
        assertEquals("Test payload", result.getPayload());
        assertEquals(actor, result.getActor());
        assertEquals("SYSTEM", result.getEventSource()); // Returns the mocked value
        verify(paymentEventRepository).save(argThat(event -> 
                event.getPaymentId().equals(paymentId) &&
                event.getType() == PaymentEventType.PAYMENT_PENDING &&
                event.getPayload().equals("Test payload") &&
                event.getActor().equals(actor) &&
                event.getEventSource().equals("CUSTOM_SOURCE")
        ));
    }

    @Test
    void testGetPaymentTimeline() {
        // Given
        List<PaymentEvent> events = Arrays.asList(testEvent);
        when(paymentEventRepository.findByPaymentIdOrderByCreatedAt(paymentId))
                .thenReturn(events);

        // When
        List<PaymentEvent> result = paymentEventService.getPaymentTimeline(paymentId);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testEvent, result.get(0));
        verify(paymentEventRepository).findByPaymentIdOrderByCreatedAt(paymentId);
    }

    @Test
    void testGetPaymentTimeline_EmptyResult() {
        // Given
        when(paymentEventRepository.findByPaymentIdOrderByCreatedAt(paymentId))
                .thenReturn(Arrays.asList());

        // When
        List<PaymentEvent> result = paymentEventService.getPaymentTimeline(paymentId);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(paymentEventRepository).findByPaymentIdOrderByCreatedAt(paymentId);
    }

    @Test
    void testGetEventsByType() {
        // Given
        List<PaymentEvent> events = Arrays.asList(testEvent);
        when(paymentEventRepository.findByTypeOrderByCreatedAtDesc(PaymentEventType.PAYMENT_PENDING))
                .thenReturn(events);

        // When
        List<PaymentEvent> result = paymentEventService.getEventsByType(PaymentEventType.PAYMENT_PENDING);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testEvent, result.get(0));
        verify(paymentEventRepository).findByTypeOrderByCreatedAtDesc(PaymentEventType.PAYMENT_PENDING);
    }

    @Test
    void testGetEventsByType_EmptyResult() {
        // Given
        when(paymentEventRepository.findByTypeOrderByCreatedAtDesc(PaymentEventType.PAYMENT_PENDING))
                .thenReturn(Arrays.asList());

        // When
        List<PaymentEvent> result = paymentEventService.getEventsByType(PaymentEventType.PAYMENT_PENDING);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(paymentEventRepository).findByTypeOrderByCreatedAtDesc(PaymentEventType.PAYMENT_PENDING);
    }

    @Test
    void testGetRecentEvents() {
        // Given
        LocalDateTime since = LocalDateTime.now().minusHours(1);
        List<PaymentEvent> events = Arrays.asList(testEvent);
        when(paymentEventRepository.findRecentEvents(since)).thenReturn(events);

        // When
        List<PaymentEvent> result = paymentEventService.getRecentEvents(since);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testEvent, result.get(0));
        verify(paymentEventRepository).findRecentEvents(since);
    }

    @Test
    void testGetRecentEvents_EmptyResult() {
        // Given
        LocalDateTime since = LocalDateTime.now().minusHours(1);
        when(paymentEventRepository.findRecentEvents(since)).thenReturn(Arrays.asList());

        // When
        List<PaymentEvent> result = paymentEventService.getRecentEvents(since);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(paymentEventRepository).findRecentEvents(since);
    }

    @Test
    void testGetEventsByActor() {
        // Given
        List<PaymentEvent> events = Arrays.asList(testEvent);
        when(paymentEventRepository.findByActorOrderByCreatedAtDesc(actor))
                .thenReturn(events);

        // When
        List<PaymentEvent> result = paymentEventService.getEventsByActor(actor);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testEvent, result.get(0));
        verify(paymentEventRepository).findByActorOrderByCreatedAtDesc(actor);
    }

    @Test
    void testGetEventsByActor_EmptyResult() {
        // Given
        when(paymentEventRepository.findByActorOrderByCreatedAtDesc(actor))
                .thenReturn(Arrays.asList());

        // When
        List<PaymentEvent> result = paymentEventService.getEventsByActor(actor);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(paymentEventRepository).findByActorOrderByCreatedAtDesc(actor);
    }

    @Test
    void testGetEventById_Found() {
        // Given
        when(paymentEventRepository.findById(1L)).thenReturn(Optional.of(testEvent));

        // When
        PaymentEvent result = paymentEventService.getEventById(1L);

        // Then
        assertNotNull(result);
        assertEquals(testEvent, result);
        verify(paymentEventRepository).findById(1L);
    }

    @Test
    void testGetEventById_NotFound() {
        // Given
        when(paymentEventRepository.findById(anyLong())).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                paymentEventService.getEventById(1L));
        assertEquals("PaymentEvent no fue encontrado con id: 1", exception.getMessage());
        verify(paymentEventRepository).findById(1L);
    }

    @Test
    void testGetPaymentEventsByType() {
        // Given
        List<PaymentEvent> events = Arrays.asList(testEvent);
        when(paymentEventRepository.findByPaymentIdAndType(paymentId, PaymentEventType.PAYMENT_PENDING))
                .thenReturn(events);

        // When
        List<PaymentEvent> result = paymentEventService.getPaymentEventsByType(
                paymentId, PaymentEventType.PAYMENT_PENDING);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testEvent, result.get(0));
        verify(paymentEventRepository).findByPaymentIdAndType(paymentId, PaymentEventType.PAYMENT_PENDING);
    }

    @Test
    void testGetPaymentEventsByType_EmptyResult() {
        // Given
        when(paymentEventRepository.findByPaymentIdAndType(paymentId, PaymentEventType.PAYMENT_PENDING))
                .thenReturn(Arrays.asList());

        // When
        List<PaymentEvent> result = paymentEventService.getPaymentEventsByType(
                paymentId, PaymentEventType.PAYMENT_PENDING);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(paymentEventRepository).findByPaymentIdAndType(paymentId, PaymentEventType.PAYMENT_PENDING);
    }

    @Test
    void testGetPaymentTimeline_MultipleEvents() {
        // Given
        PaymentEvent event2 = new PaymentEvent();
        event2.setId(2L);
        event2.setPaymentId(paymentId);
        event2.setType(PaymentEventType.PAYMENT_APPROVED);
        event2.setPayload("Approval payload");
        event2.setActor(actor);
        event2.setEventSource("GATEWAY");
        event2.setCreatedAt(LocalDateTime.now());

        List<PaymentEvent> events = Arrays.asList(testEvent, event2);
        when(paymentEventRepository.findByPaymentIdOrderByCreatedAt(paymentId))
                .thenReturn(events);

        // When
        List<PaymentEvent> result = paymentEventService.getPaymentTimeline(paymentId);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(testEvent, result.get(0));
        assertEquals(event2, result.get(1));
        verify(paymentEventRepository).findByPaymentIdOrderByCreatedAt(paymentId);
    }

    @Test
    void testCreateEvent_SetsCreatedAt() {
        // Given
        when(paymentEventRepository.save(any(PaymentEvent.class))).thenAnswer(invocation -> {
            PaymentEvent savedEvent = invocation.getArgument(0);
            savedEvent.setId(1L);
            return savedEvent;
        });

        // When
        PaymentEvent result = paymentEventService.createEvent(
                paymentId, PaymentEventType.PAYMENT_PENDING, "Test payload", actor, "CUSTOM");

        // Then
        verify(paymentEventRepository).save(argThat(event -> 
                event.getCreatedAt() != null
        ));
    }
}
