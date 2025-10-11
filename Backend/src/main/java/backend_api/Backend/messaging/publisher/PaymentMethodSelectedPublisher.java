// src/main/java/backend_api/Backend/messaging/publisher/PaymentMethodSelectedPublisher.java
package backend_api.Backend.messaging.publisher;

import backend_api.Backend.messaging.dto.PaymentMethodSelectedMessage;
import backend_api.Backend.messaging.service.CoreHubService;
import backend_api.Backend.messaging.dto.CoreResponseMessage;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.HashMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class PaymentMethodSelectedPublisher {

    private final CoreHubService coreHubService;

    public void publish(PaymentMethodSelectedMessage msg) {
        try {
            log.info("Enviando evento de método seleccionado - paymentId={}, type={}",
                msg.getPaymentId(), msg.getMethodType());

            CoreResponseMessage coreMessage = CoreResponseMessage.builder()
                .messageId(UUID.randomUUID().toString())
                .timestamp(Instant.now().toString())
                .source("payments")
                .destination(CoreResponseMessage.Destination.builder()
                    .channel("payments.payment.method_selected")
                    .eventName("method_selected")
                    .build())
                .payload(createMethodSelectedPayload(msg))
                .build();
            coreHubService.publishMessage(coreMessage);
            log.info("Evento de método seleccionado enviado exitosamente - paymentId: {}", msg.getPaymentId());
        } catch (Exception e) {
            log.error("Error al enviar evento de método seleccionado - paymentId: {}, Error: {}",
                msg.getPaymentId(), e.getMessage(), e);
            throw new RuntimeException("Error al enviar evento de método seleccionado", e);
        }
    }

    private Map<String, Object> createMethodSelectedPayload(PaymentMethodSelectedMessage msg) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("paymentId", msg.getPaymentId());
        payload.put("methodType", msg.getMethodType());
        payload.put("methodId", msg.getMethodId());
        payload.put("selectedAt", msg.getSelectedAt());
        return payload;
    }
}