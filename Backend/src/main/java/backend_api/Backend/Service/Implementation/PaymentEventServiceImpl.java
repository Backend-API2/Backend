package backend_api.Backend.Service.Implementation;

import backend_api.Backend.Entity.payment.PaymentEvent;
import backend_api.Backend.Entity.payment.PaymentEventType;
import backend_api.Backend.Repository.PaymentEventRepository;
import backend_api.Backend.Service.Interface.PaymentEventService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class PaymentEventServiceImpl implements PaymentEventService {
    
    @Autowired
    private PaymentEventRepository paymentEventRepository;
    
    @Override
    public PaymentEvent createEvent(Long paymentId, PaymentEventType type, String payload, String actor) {
        return createEvent(paymentId, type, payload, actor, "SYSTEM");
    }
    
    @Override
    public PaymentEvent createEvent(Long paymentId, PaymentEventType type, String payload, String actor, String eventSource) {
        PaymentEvent event = new PaymentEvent();
        event.setPayment_id(paymentId);
        event.setType(type);
        event.setPayload(payload);
        event.setActor(actor);
        event.setEvent_source(eventSource);
        event.setCreated_at(LocalDateTime.now());
        
        return paymentEventRepository.save(event);
    }
    
    @Override
    public List<PaymentEvent> getPaymentTimeline(Long paymentId) {
        return paymentEventRepository.findByPaymentIdOrderByCreatedAt(paymentId);
    }
    
    @Override
    public List<PaymentEvent> getEventsByType(PaymentEventType type) {
        return paymentEventRepository.findByTypeOrderByCreatedAtDesc(type);
    }
    
    @Override
    public List<PaymentEvent> getRecentEvents(LocalDateTime since) {
        return paymentEventRepository.findRecentEvents(since);
    }
    
    @Override
    public List<PaymentEvent> getEventsByActor(String actor) {
        return paymentEventRepository.findByActorOrderByCreatedAtDesc(actor);
    }
    
    @Override
    public PaymentEvent getEventById(Long id) {
        Optional<PaymentEvent> event = paymentEventRepository.findById(id);
        return event.orElseThrow(() -> new RuntimeException("PaymentEvent not found with id: " + id));
    }
    
    @Override
    public List<PaymentEvent> getPaymentEventsByType(Long paymentId, PaymentEventType type) {
        return paymentEventRepository.findByPaymentIdAndType(paymentId, type);
    }
}
