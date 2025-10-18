package backend_api.Backend.messaging.service;

import backend_api.Backend.messaging.dto.CoreResponseMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
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

    @Value("${core.hub.api.url:https://nonprodapi.uade-corehub.com}")
    private String coreHubUrl;

    @Value("${core.hub.api.key:ch_1a05cee27dfe40b1a17370ff15d52735}")
    private String apiKey;

    @Value("${core.hub.team.name:payments}")
    private String teamName;

    @Value("${core.hub.webhook.url:https://3aadd844682e.ngrok-free.app/api/core/webhook/payment-events}")
    private String webhookUrl;

    @Value("${core.hub.user.webhook.url:https://3aadd844682e.ngrok-free.app/api/core/webhook/user-events}")
    private String userWebhookUrl;


    public Map<String, Object> publishMessage(CoreResponseMessage message) {
        String url = coreHubUrl + "/publish";

        HttpHeaders headers = new HttpHeaders();
        headers.set("X-API-KEY", apiKey);
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<CoreResponseMessage> request = new HttpEntity<>(message, headers);

        Map<String, Object> result = new HashMap<>();

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("✅ Mensaje publicado exitosamente al CORE - MessageId: {}", message.getMessageId());
                log.info("📋 Respuesta del CORE Hub: {}", response.getBody());
                log.info("🔗 URL del CORE Hub: {}", url);
                
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
        }
        
        log.info("🔧 Configuración de webhooks:");
        log.info("   - webhookUrl: {}", webhookUrl);
        log.info("   - userWebhookUrl: {}", userWebhookUrl);
        log.info("   - webhookUrlToUse: {}", webhookUrlToUse);
    
        Map<String, String> subscriptionData = new HashMap<>();
        subscriptionData.put("webhookUrl", webhookUrlToUse);
        subscriptionData.put("squadName", teamName); // Usar nuestro teamName (payments)
        subscriptionData.put("topic", String.format("%s.%s.%s", targetTeamName, domain, action));
        subscriptionData.put("eventName", action);
    
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-API-KEY", apiKey);
        headers.setContentType(MediaType.APPLICATION_JSON);
    
        HttpEntity<Map<String, String>> request = new HttpEntity<>(subscriptionData, headers);
    
        try {
            log.info("Intentando suscribirse al tópico: {}.{}.{}", targetTeamName, domain, action);
            log.info("URL: {}", url);
            log.info("Webhook URL: {}", webhookUrlToUse);
            log.info("Payload: {}", subscriptionData);
            
            ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);
    
            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("Suscripción exitosa al tópico: {}.{}.{}", targetTeamName, domain, action);
                log.info("Response: {}", response.getBody());
            } else {
                log.error("Error en suscripción - Status: {}, Response: {}",
                    response.getStatusCode(), response.getBody());
                throw new RuntimeException("Error en suscripción al CORE - Status: " + 
                    response.getStatusCode() + ", Response: " + response.getBody());
            }
    
        } catch (Exception e) {
            log.error("Error suscribiéndose al tópico: {}", e.getMessage(), e);
            throw new RuntimeException("Error en suscripción al CORE: " + e.getMessage(), e);
        }
    }


    public void sendAck(String messageId, String subscriptionId) {
        String url = String.format("%s/messages/%s/ack", coreHubUrl, messageId);

        Map<String, String> ackData = new HashMap<>();
        ackData.put("msgId", messageId);
        ackData.put("subscriptionId", subscriptionId);

        HttpHeaders headers = new HttpHeaders();
        headers.set("X-API-KEY", apiKey);
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, String>> request = new HttpEntity<>(ackData, headers);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("ACK enviado exitosamente - MessageId: {}", messageId);
            } else {
                log.error("Error enviando ACK - Status: {}, Response: {}",
                    response.getStatusCode(), response.getBody());
            }

        } catch (Exception e) {
            log.error("Error enviando ACK: {}", e.getMessage(), e);
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
