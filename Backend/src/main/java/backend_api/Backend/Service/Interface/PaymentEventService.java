package backend_api.Backend.Service.Interface;

import backend_api.Backend.Entity.payment.PaymentEvent;
import backend_api.Backend.Entity.payment.PaymentEventType;

import java.time.LocalDateTime;
import java.util.List;

public interface PaymentEventService {
    
    PaymentEvent createEvent(Long paymentId, PaymentEventType type, String payload, String actor);
    
    PaymentEvent createEvent(Long paymentId, PaymentEventType type, String payload, String actor, String eventSource);
    
    List<PaymentEvent> getPaymentTimeline(Long paymentId);
    
    List<PaymentEvent> getEventsByType(PaymentEventType type);
    
    List<PaymentEvent> getRecentEvents(LocalDateTime since);
    
    List<PaymentEvent> getEventsByActor(String actor);
    
    PaymentEvent getEventById(Long id);
    
    List<PaymentEvent> getPaymentEventsByType(Long paymentId, PaymentEventType type);
}
