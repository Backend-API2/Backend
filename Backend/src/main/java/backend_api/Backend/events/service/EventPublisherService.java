package backend_api.Backend.events.service;

import backend_api.Backend.Entity.payment.Payment;
import backend_api.Backend.Entity.payment.PaymentStatus;
import backend_api.Backend.events.dto.PaymentStatusEventPayload;
import backend_api.Backend.events.entity.EventSubscription;
import backend_api.Backend.events.entity.EventType;
import backend_api.Backend.events.util.HmacSigner;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventPublisherService {

    private final SubscriptionService subscriptionService;
    private final RestTemplate restTemplate;

    public static boolean isFinalStatus(PaymentStatus status) {
        return switch (status) {
            case APPROVED, COMPLETED, REJECTED, CANCELLED, EXPIRED-> true;
            default -> false;
        };
    }

    @Transactional(readOnly = true)
    public void publishPaymentFinalStatus(Payment payment, String correlationId) {
        if (!isFinalStatus(payment.getStatus())) return;

        List<EventSubscription> subs = subscriptionService.listActive().stream()
                .filter(s -> s.supports(EventType.PAYMENT_FINALIZED)).toList();
        if (subs.isEmpty()) { log.info("No hay suscriptores para PAYMENT_FINALIZED"); return; }

        String eventId = UUID.randomUUID().toString();
        PaymentStatusEventPayload payload = PaymentStatusEventPayload.builder()
                .eventType(EventType.PAYMENT_FINALIZED.name())
                .eventId(eventId)
                .correlationId(correlationId)
                .paymentId(payment.getId())
                .userId(payment.getUser_id())
                .providerId(payment.getProvider_id())
                .amountTotal(payment.getAmount_total())
                .currency(payment.getCurrency())
                .finalStatus(payment.getStatus())
                .occurredAt(LocalDateTime.now())
                .timelineUrl(String.format("/api/payments/%d/timeline", payment.getId()))
                .metadata(buildMetadata(payment))
                .build();

        String json = toJson(payload);
        subs.forEach(sub -> dispatch(sub, json, eventId, correlationId));
    }

    private Map<String, Object> buildMetadata(Payment p) {
        Map<String, Object> m = new HashMap<>();
        m.put("createdAt", p.getCreated_at());
        m.put("updatedAt", p.getUpdated_at());
        if (p.getGateway_txn_id() != null) m.put("gatewayTxnId", p.getGateway_txn_id());
        if (p.getRefund_id() != null) m.put("refundId", p.getRefund_id());
        return m;
    }

    private void dispatch(EventSubscription sub, String body, String eventId, String correlationId) {
        int attempts = 0, max = Math.max(1, sub.getMaxRetries());
        while (attempts < max) {
            attempts++;
            try {
                HttpHeaders h = new HttpHeaders();
                h.setContentType(MediaType.APPLICATION_JSON);
                h.add("X-Event-Type", EventType.PAYMENT_FINALIZED.name());
                h.add("X-Event-Id", eventId);
                if (correlationId != null) h.add("X-Correlation-Id", correlationId);
                h.add("X-Signature", "sha256=" + HmacSigner.hmacSha256Base64(sub.getSecret(), body));

                ResponseEntity<String> res = restTemplate.exchange(
                        sub.getTargetUrl(), HttpMethod.POST, new HttpEntity<>(body, h), String.class);

                if (res.getStatusCode().is2xxSuccessful()) {
                    log.info("Evento OK -> {} (intento {})", sub.getTargetUrl(), attempts);
                    return;
                }
                log.warn("Respuesta {} desde {} (intento {})", res.getStatusCodeValue(), sub.getTargetUrl(), attempts);
            } catch (HttpStatusCodeException e) {
                log.warn("HTTP {} {} body={}", e.getStatusCode().value(), sub.getTargetUrl(), e.getResponseBodyAsString());
            } catch (Exception e) {
                log.warn("Error {} intento {}: {}", sub.getTargetUrl(), attempts, e.getMessage());
            }
            try { Thread.sleep(Math.max(250, sub.getBackoffMs()) * attempts); }
            catch (InterruptedException ie) { Thread.currentThread().interrupt(); break; }
        }
        log.error("Agotados reintentos para {}", sub.getTargetUrl());
    }

    private String toJson(Object o) {
        try {
            return new com.fasterxml.jackson.databind.ObjectMapper()
                    .findAndRegisterModules()
                    .writeValueAsString(o);
        } catch (Exception e) {
            throw new RuntimeException("Error serializando payload", e);
        }
    }
}