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
                .destination(CoreResponseMessage.Destination.builder()
                    .topic("payment") 
                    .eventName("method_selected")
                    .build())
                .payload(createMethodSelectedPayload(msg))
                .build();
            coreHubService.publishMessage(coreMessage);
            log.info("Evento de método seleccionado enviado exitosamente - paymentId: {}", msg.getPaymentId());
        } catch (Exception e) {
            log.error("⚠️ Error al enviar evento de método seleccionado al CORE - paymentId: {}, Error: {}",
                msg.getPaymentId(), e.getMessage(), e);
            // No lanzar excepción para no interrumpir el flujo de negocio
            // El método de pago ya se guardó correctamente en la BD
        }
    }

    private Map<String, Object> createMethodSelectedPayload(PaymentMethodSelectedMessage msg) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("paymentId", msg.getPaymentId());
        payload.put("userId", msg.getUserId());
        payload.put("methodType", msg.getMethodType());
        payload.put("methodId", msg.getMethodId());
        payload.put("selectedAt", msg.getSelectedAt());
        if (msg.getMethodSnapshot() != null && !msg.getMethodSnapshot().isEmpty()) {
            payload.put("methodSnapshot", msg.getMethodSnapshot());
        }
        return payload;
    }
}