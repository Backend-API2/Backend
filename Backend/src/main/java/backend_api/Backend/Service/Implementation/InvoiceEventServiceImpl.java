package backend_api.Backend.Service.Implementation;

import backend_api.Backend.DTO.invoice.InvoiceEventResponse;
import backend_api.Backend.Entity.invoice.InvoiceEvent;
import backend_api.Backend.Entity.invoice.InvoiceEventType;
import backend_api.Backend.Repository.InvoiceEventRepository;
import backend_api.Backend.Service.Interface.InvoiceEventService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InvoiceEventServiceImpl implements InvoiceEventService {
    
    private final InvoiceEventRepository invoiceEventRepository;
    
    @Override
    public void createEvent(Long invoiceId, InvoiceEventType eventType, String description, Long createdBy) {
        createEvent(invoiceId, eventType, description, createdBy, null, null, null);
    }
    
    @Override
    public void createEvent(Long invoiceId, InvoiceEventType eventType, String description, Long createdBy, String eventData) {
        createEvent(invoiceId, eventType, description, createdBy, eventData, null, null);
    }
    
    @Override
    public void createEvent(Long invoiceId, InvoiceEventType eventType, String description, Long createdBy, String eventData, String ipAddress, String userAgent) {
        InvoiceEvent event = new InvoiceEvent();
        event.setInvoiceId(invoiceId);
        event.setEventType(eventType);
        event.setDescription(description);
        event.setCreatedBy(createdBy);
        event.setEventData(eventData);
        event.setIpAddress(ipAddress);
        event.setUserAgent(userAgent);
        
        invoiceEventRepository.save(event);
    }
    
    @Override
    public List<InvoiceEventResponse> getEventsByInvoiceId(Long invoiceId) {
        List<InvoiceEvent> events = invoiceEventRepository.findByInvoiceIdOrderByCreatedAtDesc(invoiceId);
        return events.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<InvoiceEventResponse> getEventsByInvoiceIdAndType(Long invoiceId, InvoiceEventType eventType) {
        List<InvoiceEvent> events = invoiceEventRepository.findByInvoiceIdAndEventType(invoiceId, eventType);
        return events.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }
    
    @Override
    public Long countEventsByInvoiceId(Long invoiceId) {
        return invoiceEventRepository.countByInvoiceId(invoiceId);
    }
    
    @Override
    public Long countEventsByInvoiceIdAndType(Long invoiceId, InvoiceEventType eventType) {
        return invoiceEventRepository.countByInvoiceIdAndEventType(invoiceId, eventType);
    }
    
    private InvoiceEventResponse convertToResponse(InvoiceEvent event) {
        return InvoiceEventResponse.builder()
                .id(event.getId())
                .invoiceId(event.getInvoiceId())
                .eventType(event.getEventType())
                .description(event.getDescription())
                .createdBy(event.getCreatedBy())
                .createdAt(event.getCreatedAt())
                .eventData(event.getEventData())
                .ipAddress(event.getIpAddress())
                .userAgent(event.getUserAgent())
                .build();
    }
}
