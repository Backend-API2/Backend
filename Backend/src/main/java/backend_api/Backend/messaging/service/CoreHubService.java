package backend_api.Backend.messaging.service;

import backend_api.Backend.messaging.dto.CoreResponseMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class CoreHubService {

    private final RestTemplate restTemplate;

    @Value("${core.hub.api.url:https://api.arreglacore.click}")
    private String coreHubUrl;

    @Value("${core.hub.api.key:ch_1a05cee27dfe40b1a17370ff15d52735}")
    private String apiKey;

    @Value("${core.hub.team.name:payments}")
    private String teamName;

    @Value("${core.hub.webhook.url:http://18.119.150.122:8082/api/core/webhook/payment-events}")
    private String webhookUrl;

    @Value("${core.hub.user.webhook.url:http://18.119.150.122:8082/api/core/webhook/user-events}")
    private String userWebhookUrl;

    @Value("${core.hub.matching.webhook.url:http://18.119.150.122:8082/api/core/webhook/matching-payment-requests}")
    private String matchingWebhookUrl;


    public Map<String, Object> publishMessage(CoreResponseMessage message) {
        String url = coreHubUrl + "/publish";

        HttpHeaders headers = new HttpHeaders();
        headers.set("X-API-KEY", apiKey);
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> newFormatMessage = new HashMap<>();
        newFormatMessage.put("messageId", message.getMessageId());
        newFormatMessage.put("timestamp", message.getTimestamp());
        
        Map<String, Object> destination = new HashMap<>();
        if (message.getDestination() != null) {
            String topic = message.getDestination().getTopic();
            destination.put("topic", topic != null ? topic : "");
            destination.put("eventName", message.getDestination().getEventName());
        }
        newFormatMessage.put("destination", destination);
        newFormatMessage.put("payload", message.getPayload());

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(newFormatMessage, headers);

        Map<String, Object> result = new HashMap<>();

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("✅ Mensaje publicado exitosamente al CORE - MessageId: {}", message.getMessageId());
                log.info("📋 Respuesta del CORE Hub: {}", response.getBody());
                log.info("🔗 URL del CORE Hub: {}", url);
                log.info("📊 Formato nuevo usado: topic={}, eventName={}", 
                    destination.get("topic"), destination.get("eventName"));
                
                result.put("success", true);
                result.put("statusCode", response.getStatusCode().value());
                result.put("response", response.getBody());
                result.put("messageId", message.getMessageId());
            } else {
                log.error("❌ Error publicando mensaje al CORE - Status: {}, Response: {}",
                    response.getStatusCode(), response.getBody());
                
                result.put("success", false);
                result.put("statusCode", response.getStatusCode().value());
                result.put("response", response.getBody());
                result.put("error", "Error publicando al CORE");
            }

        } catch (Exception e) {
            log.error("❌ Error al publicar mensaje al CORE: {}", e.getMessage(), e);
            result.put("success", false);
            result.put("error", e.getMessage());
            throw new RuntimeException("Error publicando al CORE", e);
        }
        
        return result;
    }

   
    public void subscribeToTopic(String targetTeamName, String domain, String action) {
        String url = coreHubUrl + "/subscribe";
    
        // Determinar el webhook URL correcto según el tipo de evento
        String webhookUrlToUse = webhookUrl;
        if ("users".equals(targetTeamName)) {
            webhookUrlToUse = userWebhookUrl;
        } else if ("matching".equals(targetTeamName)) {
            webhookUrlToUse = matchingWebhookUrl;
        }
        
        log.info("🔧 Configuración de webhooks:");
        log.info("   - webhookUrl: {}", webhookUrl);
        log.info("   - userWebhookUrl: {}", userWebhookUrl);
        log.info("   - webhookUrlToUse: {}", webhookUrlToUse);
    
        Map<String, Object> subscriptionData = new HashMap<>();
        subscriptionData.put("webhookUrl", webhookUrlToUse);
        subscriptionData.put("squadName", targetTeamName); 
        subscriptionData.put("topic", domain); 
        subscriptionData.put("eventName", action);
        subscriptionData.put("ack", true); 
    
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-API-KEY", apiKey);
        headers.setContentType(MediaType.APPLICATION_JSON);
    
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(subscriptionData, headers);
    
        try {
            log.info("Intentando suscribirse al tópico: {} con eventName: {}", domain, action);
            log.info("URL: {}", url);
            log.info("Webhook URL: {}", webhookUrlToUse);
            log.info("Payload: {}", subscriptionData);
            
            ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);
    
            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("✅ Suscripción exitosa al tópico: {} con eventName: {}", domain, action);
                log.info("📋 Response: {}", response.getBody());
            } else {
                log.error("❌ Error en suscripción - Status: {}, Response: {}",
                    response.getStatusCode(), response.getBody());
                throw new RuntimeException("Error en suscripción al CORE - Status: " + 
                    response.getStatusCode() + ", Response: " + response.getBody());
            }
    
        } catch (Exception e) {
            log.error("❌ Error suscribiéndose al tópico: {}", e.getMessage(), e);
            throw new RuntimeException("Error en suscripción al CORE: " + e.getMessage(), e);
        }
    }


    public void sendAck(String messageId, String subscriptionId) {
        String url = String.format("%s/messages/ack/%s", coreHubUrl, subscriptionId);

        Map<String, String> ackData = new HashMap<>();
        ackData.put("messageId", messageId);

        HttpHeaders headers = new HttpHeaders();
        headers.set("X-API-KEY", apiKey);
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, String>> request = new HttpEntity<>(ackData, headers);

        try {
            log.info("Enviando ACK - MessageId: {}, SubscriptionId: {}", messageId, subscriptionId);
            log.info("URL: {}", url);
            log.info("Payload: {}", ackData);
            
            ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("✅ ACK enviado exitosamente - MessageId: {}, SubscriptionId: {}", messageId, subscriptionId);
                log.info("📋 Response: {}", response.getBody());
            } else {
                log.error("❌ Error enviando ACK - Status: {}, Response: {}",
                    response.getStatusCode(), response.getBody());
            }

        } catch (Exception e) {
            log.error("❌ Error enviando ACK: {}", e.getMessage(), e);
        }
    }

   
    public Map<String, Object> checkConnection() {
        Map<String, Object> result = new HashMap<>();

        try {
            // Intenta hacer una petición de prueba
            HttpHeaders headers = new HttpHeaders();
            headers.set("X-API-KEY", apiKey);

            result.put("coreHubUrl", coreHubUrl);
            result.put("teamName", teamName);
            result.put("apiKeyConfigured", apiKey != null && !apiKey.isEmpty());
            result.put("webhookUrl", webhookUrl);
            result.put("status", "CONFIGURED");

        } catch (Exception e) {
            result.put("status", "ERROR");
            result.put("error", e.getMessage());
        }

        return result;
    }
}
