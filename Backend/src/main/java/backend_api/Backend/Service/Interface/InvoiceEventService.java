package backend_api.Backend.Service.Interface;

import backend_api.Backend.DTO.invoice.InvoiceEventResponse;
import backend_api.Backend.Entity.invoice.InvoiceEventType;

import java.util.List;

public interface InvoiceEventService {
    
    void createEvent(Long invoiceId, InvoiceEventType eventType, String description, Long createdBy);
    void createEvent(Long invoiceId, InvoiceEventType eventType, String description, Long createdBy, String eventData);
    void createEvent(Long invoiceId, InvoiceEventType eventType, String description, Long createdBy, String eventData, String ipAddress, String userAgent);
    
    List<InvoiceEventResponse> getEventsByInvoiceId(Long invoiceId);
    List<InvoiceEventResponse> getEventsByInvoiceIdAndType(Long invoiceId, InvoiceEventType eventType);
    
    Long countEventsByInvoiceId(Long invoiceId);
    Long countEventsByInvoiceIdAndType(Long invoiceId, InvoiceEventType eventType);
}
