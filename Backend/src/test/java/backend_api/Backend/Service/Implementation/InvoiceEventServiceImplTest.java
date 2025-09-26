package backend_api.Backend.Service.Implementation;

import backend_api.Backend.DTO.invoice.InvoiceEventResponse;
import backend_api.Backend.Entity.invoice.InvoiceEvent;
import backend_api.Backend.Entity.invoice.InvoiceEventType;
import backend_api.Backend.Repository.InvoiceEventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for InvoiceEventServiceImpl
 * Testing invoice event management operations
 */
@ExtendWith(MockitoExtension.class)
class InvoiceEventServiceImplTest {

    @Mock
    private InvoiceEventRepository invoiceEventRepository;

    @InjectMocks
    private InvoiceEventServiceImpl invoiceEventService;

    private InvoiceEvent testEvent;
    private final Long invoiceId = 1L;
    private final Long createdBy = 1L;

    @BeforeEach
    void setUp() {
        testEvent = new InvoiceEvent();
        testEvent.setId(1L);
        testEvent.setInvoiceId(invoiceId);
        testEvent.setEventType(InvoiceEventType.INVOICE_CREATED);
        testEvent.setDescription("Invoice created");
        testEvent.setCreatedBy(createdBy);
        testEvent.setCreatedAt(LocalDateTime.now());
        testEvent.setEventData("Test data");
        testEvent.setIpAddress("192.168.1.1");
        testEvent.setUserAgent("Test User Agent");
    }

    @Test
    void testCreateEvent_BasicOverload() {
        // Given
        when(invoiceEventRepository.save(any(InvoiceEvent.class))).thenReturn(testEvent);

        // When
        invoiceEventService.createEvent(invoiceId, InvoiceEventType.INVOICE_CREATED, "Invoice created", createdBy);

        // Then
        verify(invoiceEventRepository).save(any(InvoiceEvent.class));
    }

    @Test
    void testCreateEvent_WithEventData() {
        // Given
        when(invoiceEventRepository.save(any(InvoiceEvent.class))).thenReturn(testEvent);

        // When
        invoiceEventService.createEvent(invoiceId, InvoiceEventType.INVOICE_CREATED, "Invoice created", createdBy, "Event data");

        // Then
        verify(invoiceEventRepository).save(any(InvoiceEvent.class));
    }

    @Test
    void testCreateEvent_FullOverload() {
        // Given
        when(invoiceEventRepository.save(any(InvoiceEvent.class))).thenAnswer(invocation -> {
            InvoiceEvent savedEvent = invocation.getArgument(0);
            savedEvent.setId(1L);
            savedEvent.setCreatedAt(LocalDateTime.now());
            return savedEvent;
        });

        // When
        invoiceEventService.createEvent(
                invoiceId, 
                InvoiceEventType.INVOICE_CREATED, 
                "Invoice created", 
                createdBy, 
                "Event data", 
                "192.168.1.1", 
                "Mozilla/5.0"
        );

        // Then
        verify(invoiceEventRepository).save(argThat(event -> 
                event.getInvoiceId().equals(invoiceId) &&
                event.getEventType() == InvoiceEventType.INVOICE_CREATED &&
                event.getDescription().equals("Invoice created") &&
                event.getCreatedBy().equals(createdBy) &&
                event.getEventData().equals("Event data") &&
                event.getIpAddress().equals("192.168.1.1") &&
                event.getUserAgent().equals("Mozilla/5.0")
        ));
    }

    @Test
    void testGetEventsByInvoiceId() {
        // Given
        List<InvoiceEvent> events = Arrays.asList(testEvent);
        when(invoiceEventRepository.findByInvoiceIdOrderByCreatedAtDesc(invoiceId))
                .thenReturn(events);

        // When
        List<InvoiceEventResponse> result = invoiceEventService.getEventsByInvoiceId(invoiceId);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        InvoiceEventResponse response = result.get(0);
        assertEquals(testEvent.getId(), response.getId());
        assertEquals(testEvent.getInvoiceId(), response.getInvoiceId());
        assertEquals(testEvent.getEventType(), response.getEventType());
        assertEquals(testEvent.getDescription(), response.getDescription());
        assertEquals(testEvent.getCreatedBy(), response.getCreatedBy());
        assertEquals(testEvent.getEventData(), response.getEventData());
        assertEquals(testEvent.getIpAddress(), response.getIpAddress());
        assertEquals(testEvent.getUserAgent(), response.getUserAgent());
        verify(invoiceEventRepository).findByInvoiceIdOrderByCreatedAtDesc(invoiceId);
    }

    @Test
    void testGetEventsByInvoiceId_EmptyResult() {
        // Given
        when(invoiceEventRepository.findByInvoiceIdOrderByCreatedAtDesc(invoiceId))
                .thenReturn(Arrays.asList());

        // When
        List<InvoiceEventResponse> result = invoiceEventService.getEventsByInvoiceId(invoiceId);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(invoiceEventRepository).findByInvoiceIdOrderByCreatedAtDesc(invoiceId);
    }

    @Test
    void testGetEventsByInvoiceIdAndType() {
        // Given
        List<InvoiceEvent> events = Arrays.asList(testEvent);
        when(invoiceEventRepository.findByInvoiceIdAndEventType(invoiceId, InvoiceEventType.INVOICE_CREATED))
                .thenReturn(events);

        // When
        List<InvoiceEventResponse> result = invoiceEventService.getEventsByInvoiceIdAndType(
                invoiceId, InvoiceEventType.INVOICE_CREATED);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        InvoiceEventResponse response = result.get(0);
        assertEquals(testEvent.getId(), response.getId());
        assertEquals(testEvent.getEventType(), response.getEventType());
        verify(invoiceEventRepository).findByInvoiceIdAndEventType(invoiceId, InvoiceEventType.INVOICE_CREATED);
    }

    @Test
    void testGetEventsByInvoiceIdAndType_EmptyResult() {
        // Given
        when(invoiceEventRepository.findByInvoiceIdAndEventType(invoiceId, InvoiceEventType.INVOICE_CREATED))
                .thenReturn(Arrays.asList());

        // When
        List<InvoiceEventResponse> result = invoiceEventService.getEventsByInvoiceIdAndType(
                invoiceId, InvoiceEventType.INVOICE_CREATED);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(invoiceEventRepository).findByInvoiceIdAndEventType(invoiceId, InvoiceEventType.INVOICE_CREATED);
    }

    @Test
    void testCountEventsByInvoiceId() {
        // Given
        when(invoiceEventRepository.countByInvoiceId(invoiceId)).thenReturn(5L);

        // When
        Long result = invoiceEventService.countEventsByInvoiceId(invoiceId);

        // Then
        assertEquals(5L, result);
        verify(invoiceEventRepository).countByInvoiceId(invoiceId);
    }

    @Test
    void testCountEventsByInvoiceId_ZeroEvents() {
        // Given
        when(invoiceEventRepository.countByInvoiceId(invoiceId)).thenReturn(0L);

        // When
        Long result = invoiceEventService.countEventsByInvoiceId(invoiceId);

        // Then
        assertEquals(0L, result);
        verify(invoiceEventRepository).countByInvoiceId(invoiceId);
    }

    @Test
    void testCountEventsByInvoiceIdAndType() {
        // Given
        when(invoiceEventRepository.countByInvoiceIdAndEventType(invoiceId, InvoiceEventType.INVOICE_CREATED))
                .thenReturn(3L);

        // When
        Long result = invoiceEventService.countEventsByInvoiceIdAndType(invoiceId, InvoiceEventType.INVOICE_CREATED);

        // Then
        assertEquals(3L, result);
        verify(invoiceEventRepository).countByInvoiceIdAndEventType(invoiceId, InvoiceEventType.INVOICE_CREATED);
    }

    @Test
    void testCountEventsByInvoiceIdAndType_ZeroEvents() {
        // Given
        when(invoiceEventRepository.countByInvoiceIdAndEventType(invoiceId, InvoiceEventType.INVOICE_CREATED))
                .thenReturn(0L);

        // When
        Long result = invoiceEventService.countEventsByInvoiceIdAndType(invoiceId, InvoiceEventType.INVOICE_CREATED);

        // Then
        assertEquals(0L, result);
        verify(invoiceEventRepository).countByInvoiceIdAndEventType(invoiceId, InvoiceEventType.INVOICE_CREATED);
    }

    @Test
    void testGetEventsByInvoiceId_MultipleEvents() {
        // Given
        InvoiceEvent event2 = new InvoiceEvent();
        event2.setId(2L);
        event2.setInvoiceId(invoiceId);
        event2.setEventType(InvoiceEventType.INVOICE_UPDATED);
        event2.setDescription("Invoice updated");
        event2.setCreatedBy(createdBy);
        event2.setCreatedAt(LocalDateTime.now());

        List<InvoiceEvent> events = Arrays.asList(testEvent, event2);
        when(invoiceEventRepository.findByInvoiceIdOrderByCreatedAtDesc(invoiceId))
                .thenReturn(events);

        // When
        List<InvoiceEventResponse> result = invoiceEventService.getEventsByInvoiceId(invoiceId);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(testEvent.getId(), result.get(0).getId());
        assertEquals(event2.getId(), result.get(1).getId());
        verify(invoiceEventRepository).findByInvoiceIdOrderByCreatedAtDesc(invoiceId);
    }

    @Test
    void testCreateEvent_WithNullValues() {
        // Given
        when(invoiceEventRepository.save(any(InvoiceEvent.class))).thenAnswer(invocation -> {
            InvoiceEvent savedEvent = invocation.getArgument(0);
            savedEvent.setId(1L);
            savedEvent.setCreatedAt(LocalDateTime.now());
            return savedEvent;
        });

        // When
        invoiceEventService.createEvent(
                invoiceId, 
                InvoiceEventType.INVOICE_CREATED, 
                "Invoice created", 
                createdBy, 
                null, 
                null, 
                null
        );

        // Then
        verify(invoiceEventRepository).save(argThat(event -> 
                event.getInvoiceId().equals(invoiceId) &&
                event.getEventType() == InvoiceEventType.INVOICE_CREATED &&
                event.getDescription().equals("Invoice created") &&
                event.getCreatedBy().equals(createdBy) &&
                event.getEventData() == null &&
                event.getIpAddress() == null &&
                event.getUserAgent() == null
        ));
    }
}
