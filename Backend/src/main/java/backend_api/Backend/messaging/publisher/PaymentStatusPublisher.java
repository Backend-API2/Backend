package backend_api.Backend.messaging.publisher;

import backend_api.Backend.messaging.dto.PaymentStatusUpdateMessage;
import backend_api.Backend.messaging.dto.CoreResponseMessage;
import backend_api.Backend.messaging.service.CoreHubService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;
import java.util.HashMap;

@Component
@Slf4j
@RequiredArgsConstructor
public class PaymentStatusPublisher {

    private final CoreHubService coreHubService;

    public void publishPaymentStatusUpdate(PaymentStatusUpdateMessage message) {
        try {
            log.info("Enviando actualización de estado - PaymentId: {}, Status: {} -> {}",
                message.getPaymentId(), message.getOldStatus(), message.getNewStatus());

            CoreResponseMessage coreMessage = new CoreResponseMessage();
            coreMessage.setMessageId(message.getMessageId());
            coreMessage.setTimestamp(Instant.now().toString());
            coreMessage.setDestination(createDestination("payment", "status_updated"));
            coreMessage.setPayload(createPaymentStatusPayload(message));

            coreHubService.publishMessage(coreMessage);

            log.info("Mensaje enviado exitosamente - PaymentId: {}, MessageId: {}",
                message.getPaymentId(), message.getMessageId());
        } catch (Exception e) {
            log.error("Error enviando actualización de estado - PaymentId: {}, Error: {}",
                message.getPaymentId(), e.getMessage(), e);
            throw new RuntimeException("Error al enviar mensaje de actualización", e);
        }
    }


    private CoreResponseMessage.Destination createDestination(String domain, String action){
        CoreResponseMessage.Destination dest = new CoreResponseMessage.Destination();
        dest.setTopic(domain); 
        dest.setEventName(action);
        return dest;
    }

    private Map<String, Object> createPaymentStatusPayload(PaymentStatusUpdateMessage message){
        Map<String, Object> payload = new HashMap<>();
        payload.put("paymentId", message.getPaymentId());
        payload.put("oldStatus", message.getOldStatus());
        payload.put("newStatus", message.getNewStatus());
        payload.put("reason", message.getReason());
        payload.put("amountTotal", message.getAmountTotal());
        payload.put("currency", message.getCurrency());
        payload.put("gatewayTxnId", message.getGatewayTxnId());
        payload.put("updatedAt", message.getUpdatedAt());
        return payload;
    }
}