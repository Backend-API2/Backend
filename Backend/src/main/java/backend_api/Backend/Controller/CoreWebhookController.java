package backend_api.Backend.Controller;

import backend_api.Backend.messaging.dto.CoreEventMessage;
import backend_api.Backend.messaging.dto.PaymentRequestMessage;
import backend_api.Backend.messaging.service.CoreEventProcessorService;
import backend_api.Backend.messaging.service.UserEventProcessorService;
import backend_api.Backend.messaging.service.CoreHubService;
import backend_api.Backend.messaging.service.PaymentRequestProcessorService;
import backend_api.Backend.messaging.service.ProviderEventProcessorService;
import backend_api.Backend.Service.Interface.PaymentEventService;
import backend_api.Backend.Entity.payment.PaymentEventType;
import backend_api.Backend.Entity.payment.PaymentEvent;
import backend_api.Backend.Repository.PaymentEventRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;


/**
 * Core Webhook Controller
 * Handles incoming webhook events from the Core system
 */
@RestController
@RequestMapping("/api/core/webhook")
@RequiredArgsConstructor
@Slf4j
public class CoreWebhookController {

    private final CoreEventProcessorService coreEventProcessorService;
    private final UserEventProcessorService userEventProcessorService;
    private final CoreHubService coreHubService;
    private final PaymentRequestProcessorService paymentRequestProcessorService;
    private final ProviderEventProcessorService providerEventProcessorService;
    private final PaymentEventService paymentEventService;
    private final PaymentEventRepository paymentEventRepository;
    private final ObjectMapper objectMapper;


  
    @PostMapping("/payment-events")
    public ResponseEntity<Map<String, String>> receivePaymentEvent(@RequestBody Object rawMessage) {
        try {
            log.info("🌐 ========== WEBHOOK RECIBIDO DEL CORE ==========");
            log.info("📥 Raw message type: {}", rawMessage != null ? rawMessage.getClass().getName() : "null");
            log.info("📥 Raw message completo: {}", rawMessage);
            log.info("🌐 ===============================================");
            
            // Convertir a CoreEventMessage
            CoreEventMessage message;
            if (rawMessage instanceof CoreEventMessage) {
                message = (CoreEventMessage) rawMessage;
            } else if (rawMessage instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> map = (Map<String, Object>) rawMessage;
                message = new CoreEventMessage();
                message.setMessageId((String) map.get("messageId"));
                
                // Timestamp
                Object timestamp = map.get("timestamp");
                if (timestamp != null) {
                    try {
                        if (timestamp instanceof String) {
                            message.setTimestamp(java.time.LocalDateTime.parse(timestamp.toString()));
                        }
                    } catch (Exception e) {
                        log.warn("No se pudo parsear timestamp: {}", timestamp);
                    }
                }
                
                // Destination
                Object destObj = map.get("destination");
                if (destObj instanceof Map) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> dest = (Map<String, Object>) destObj;
                    CoreEventMessage.Destination destination = new CoreEventMessage.Destination();
                    destination.setTopic((String) dest.get("topic"));
                    destination.setEventName((String) dest.get("eventName"));
                    message.setDestination(destination);
                }
                
                // Payload
                Object payloadObj = map.get("payload");
                if (payloadObj instanceof Map) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> payload = (Map<String, Object>) payloadObj;
                    message.setPayload(payload);
                }
            } else {
                throw new IllegalArgumentException("Formato de mensaje no soportado: " + 
                    (rawMessage != null ? rawMessage.getClass().getName() : "null"));
            }
            
            log.info("📥 MessageId: {}", message.getMessageId());
            log.info("📥 EventName: {}", message.getDestination() != null ? message.getDestination().getEventName() : "null");
            log.info("📥 Topic: {}", message.getDestination() != null ? message.getDestination().getTopic() : "null");
            log.info("📥 Payload completo: {}", message.getPayload());
            log.info("📥 Payload keys: {}", message.getPayload() != null ? message.getPayload().keySet() : "null");

            String subscriptionId = extractSubscriptionId(message);

            String eventName = message.getDestination().getEventName();

            switch (eventName) {
                case "CREATE_PAYMENT":
                case "PAYMENT_REQUEST":
                case "created":
                    coreEventProcessorService.processPaymentRequestFromCore(message);
                    break;

                case "status_updated":
                    log.info("✅ Evento status_updated recibido del CORE - MessageId: {}, Payload: {}", 
                        message.getMessageId(), message.getPayload());
                    try {
                        // Verificar idempotencia antes de procesar
                        if (isEventAlreadyProcessed(message.getMessageId())) {
                            log.warn("⏭️ Evento status_updated duplicado detectado - MessageId: {}, omitiendo procesamiento", 
                                message.getMessageId());
                            return ResponseEntity.ok(Map.of(
                                "status", "duplicate",
                                "messageId", message.getMessageId(),
                                "message", "Evento ya procesado anteriormente"
                            ));
                        }
                        saveStatusUpdatedEvent(message);
                        log.info("✅ Evento status_updated guardado exitosamente en payment_events");
                    } catch (Exception e) {
                        log.error("❌ Error guardando evento status_updated: {}", e.getMessage(), e);
                        throw e;
                    }
                    break;

                case "method_selected":
                    log.info("✅ Evento method_selected recibido del CORE - MessageId: {}, Payload: {}", 
                        message.getMessageId(), message.getPayload());
                    try {
                        // Verificar idempotencia antes de procesar
                        if (isEventAlreadyProcessed(message.getMessageId())) {
                            log.warn("⏭️ Evento method_selected duplicado detectado - MessageId: {}, omitiendo procesamiento", 
                                message.getMessageId());
                            return ResponseEntity.ok(Map.of(
                                "status", "duplicate",
                                "messageId", message.getMessageId(),
                                "message", "Evento ya procesado anteriormente"
                            ));
                        }
                        saveMethodSelectedEvent(message);
                        log.info("✅ Evento method_selected guardado exitosamente en payment_events");
                    } catch (Exception e) {
                        log.error("❌ Error guardando evento method_selected: {}", e.getMessage(), e);
                        throw e;
                    }
                    break;

                case "USER_PROVIDER_DATA":
                case "DATA_RESPONSE":
                    coreEventProcessorService.processUserProviderDataFromCore(message);
                    break;

                default:
                    log.warn("Evento no reconocido: {}", eventName);
            }

            if (subscriptionId != null) {
                coreHubService.sendAck(message.getMessageId(), subscriptionId);
            }

            return ResponseEntity.ok(Map.of(
                "status", "processed",
                "messageId", message.getMessageId()
            ));

        } catch (Exception e) {
            String messageId = "unknown";
            try {
                if (rawMessage instanceof Map) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> map = (Map<String, Object>) rawMessage;
                    messageId = (String) map.getOrDefault("messageId", "unknown");
                }
            } catch (Exception ignore) {}
            
            log.error("Error procesando webhook del CORE - MessageId: {}, Error: {}",
                messageId, e.getMessage(), e);

            return ResponseEntity.status(500).body(Map.of(
                "status", "error",
                "messageId", messageId,
                "error", e.getMessage(),
                "retryAfter","30"
            ));
        }
    }

   
    @PostMapping("/user-events")
    public ResponseEntity<Map<String, String>> receiveUserEvent(@RequestBody Map<String, Object> rawMessage) {
        CoreEventMessage message = null;
        try {
            // Convertir el mensaje raw a CoreEventMessage
            if (rawMessage instanceof CoreEventMessage) {
                message = (CoreEventMessage) rawMessage;
            } else {
                Map<String, Object> map = rawMessage;
                message = new CoreEventMessage();
                message.setMessageId((String) map.get("messageId"));

                // El timestamp viene como String, convertirlo
                Object timestamp = map.get("timestamp");
                if (timestamp != null) {
                    try {
                        message.setTimestamp(java.time.LocalDateTime.parse(timestamp.toString()));
                    } catch (Exception e) {
                        log.warn("No se pudo parsear timestamp: {}", timestamp);
                    }
                }

                // Destination
                Object destObj = map.get("destination");
                if (destObj instanceof Map) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> dest = (Map<String, Object>) destObj;
                    CoreEventMessage.Destination destination = new CoreEventMessage.Destination();
                    destination.setTopic((String) dest.get("topic"));
                    destination.setEventName((String) dest.get("eventName"));
                    message.setDestination(destination);
                }

                // Payload
                Object payloadObj = map.get("payload");
                if (payloadObj instanceof Map) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> payload = (Map<String, Object>) payloadObj;
                    message.setPayload(payload);
                }
            }
            
            if (message == null) {
                throw new IllegalArgumentException("No se pudo crear el mensaje");
            }
            
            log.info("Webhook de usuarios recibido del CORE - MessageId: {}, EventName: {}, Topic: {}",
                message.getMessageId(),
                message.getDestination() != null ? message.getDestination().getEventName() : "null",
                message.getDestination() != null ? message.getDestination().getTopic() : "null");

            String subscriptionId = extractSubscriptionId(message);

            String eventName = message.getDestination().getEventName();

            switch (eventName) {
                case "user_created":
                    userEventProcessorService.processUserCreatedFromCore(message);
                    break;

                case "user_updated":
                    userEventProcessorService.processUserUpdatedFromCore(message);
                    break;

                case "user_deactivated":
                    userEventProcessorService.processUserDeactivatedFromCore(message);
                    break;
                    
                case "user_rejected":
                    userEventProcessorService.processUserRejectedFromCore(message);
                    break;

                default:
                    log.warn("Evento de usuario no reconocido: {}", eventName);
            }

            if (subscriptionId != null) {
                coreHubService.sendAck(message.getMessageId(), subscriptionId);
            }

            return ResponseEntity.ok(Map.of(
                "status", "processed",
                "messageId", message.getMessageId()
            ));

        } catch (Exception e) {
            String messageId = message != null ? message.getMessageId() : "unknown";
            log.error("Error procesando webhook de usuarios del CORE - MessageId: {}, Error: {}",
                messageId, e.getMessage(), e);

            return ResponseEntity.status(500).body(Map.of(
                "status", "error",
                "messageId", messageId,
                "error", e.getMessage(),
                "retryAfter","30"
            ));
        }
    }

    @PostMapping("/provider-events")
    public ResponseEntity<Map<String, String>> receiveProviderEvent(@RequestBody CoreEventMessage message) {
        try {
            log.info("Webhook de prestadores recibido del CORE - MessageId: {}, EventName: {}, Topic: {}",
                    message.getMessageId(),
                    message.getDestination() != null ? message.getDestination().getEventName() : "null",
                    message.getDestination() != null ? message.getDestination().getChannel() : "null");

            String subscriptionId = extractSubscriptionId(message);

            providerEventProcessorService.processProviderFromCore(message);

            if (subscriptionId != null) {
                coreHubService.sendAck(message.getMessageId(), subscriptionId);
            }

            return ResponseEntity.ok(Map.of(
                    "status", "processed",
                    "messageId", message.getMessageId()
            ));
        } catch (Exception e) {
            log.error("Error procesando webhook de prestadores - MessageId: {}, Error: {}",
                    message.getMessageId(), e.getMessage(), e);

            return ResponseEntity.status(500).body(Map.of(
                    "status", "error",
                    "messageId", message.getMessageId(),
                    "error", e.getMessage(),
                    "retryAfter","30"
            ));
        }
    }

    @PostMapping("/matching-payment-requests")
    public ResponseEntity<Map<String, Object>> receiveMatchingPaymentRequest(@RequestBody java.util.Map<String, Object> rawMessage) {
        // Extraer messageId al inicio para usarlo en caso de error
        String messageId = "unknown";
        try {
            Object mid = rawMessage.get("messageId");
            if (mid != null) {
                messageId = mid.toString();
            }
        } catch (Exception ignore) {
            // Ignorar error al extraer messageId
        }
        
        try {
            log.info("🔄 Webhook de solicitud de pago de matching recibido - RawMessage: {}", rawMessage);

            // Convertir el mensaje recibido del CORE Hub
            PaymentRequestMessage message = convertCoreMessageToPaymentRequest(rawMessage);
            
            log.info("🔄 Webhook procesado - MessageId: {}, EventName: {}, Topic: {}",
                message.getMessageId(),
                message.getDestination() != null ? message.getDestination().getEventName() : "null",
                message.getDestination() != null ? message.getDestination().getTopic() : "null");

            // Procesar la solicitud de pago
            Map<String, Object> result = paymentRequestProcessorService.processPaymentRequest(message);

            // Enviar ACK solo si el procesamiento fue exitoso
            Boolean success = (Boolean) result.get("success");
            if (Boolean.TRUE.equals(success)) {
                String subscriptionId = extractSubscriptionIdFromPaymentRequest(message);
                if (subscriptionId != null) {
                    coreHubService.sendAck(message.getMessageId(), subscriptionId);
                }
            }

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            log.error("❌ Error procesando solicitud de pago de matching - MessageId: {}, Error: {}",
                messageId, e.getMessage(), e);

            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "status", "error",
                "messageId", messageId,
                "error", e.getMessage(),
                "retryAfter", "30"
            ));
        }
    }
    
    private PaymentRequestMessage convertCoreMessageToPaymentRequest(Object rawMessage) {
        log.info("📝 Convertiendo mensaje del CORE Hub a PaymentRequestMessage...");
        
        if (rawMessage == null) {
            throw new IllegalArgumentException("El mensaje no puede ser null");
        }
        
        // Si ya es PaymentRequestMessage, devolverlo directamente (para compatibilidad con tests)
        if (rawMessage instanceof PaymentRequestMessage) {
            log.info("✅ El mensaje ya es PaymentRequestMessage, devolviendo directamente");
            return (PaymentRequestMessage) rawMessage;
        }
        
        // Si es un Map, intentar convertirlo directamente a PaymentRequestMessage primero (para tests)
        if (rawMessage instanceof java.util.Map) {
            try {
                ObjectMapper mapper = new ObjectMapper();
                // Intentar convertir a PaymentRequestMessage primero
                PaymentRequestMessage directMessage = mapper.convertValue(rawMessage, PaymentRequestMessage.class);
                if (directMessage.getPayload() != null) {
                    log.info("✅ Convertido directamente a PaymentRequestMessage desde Map");
                    return directMessage;
                }
            } catch (Exception e) {
                log.debug("No se pudo convertir directamente a PaymentRequestMessage: {}", e.getMessage());
            }
        }
        
        // Mapear a CoreEventMessage
        CoreEventMessage coreMessage = null;
        if (rawMessage instanceof CoreEventMessage) {
            coreMessage = (CoreEventMessage) rawMessage;
        } else if (rawMessage instanceof java.util.Map) {
            // Convertir Map a CoreEventMessage manualmente para evitar problemas con LocalDateTime
            try {
                @SuppressWarnings("unchecked")
                Map<String, Object> map = (Map<String, Object>) rawMessage;
                coreMessage = new CoreEventMessage();
                coreMessage.setMessageId((String) map.get("messageId"));
                
                // El timestamp viene como String, convertirlo si es necesario
                Object timestamp = map.get("timestamp");
                if (timestamp != null) {
                    try {
                        coreMessage.setTimestamp(java.time.LocalDateTime.parse(timestamp.toString()));
                    } catch (Exception e) {
                        log.warn("No se pudo parsear timestamp: {}", timestamp);
                    }
                }
                
                // Destination
                Object destObj = map.get("destination");
                if (destObj instanceof Map) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> dest = (Map<String, Object>) destObj;
                    CoreEventMessage.Destination destination = new CoreEventMessage.Destination();
                    destination.setTopic((String) dest.get("topic"));
                    destination.setEventName((String) dest.get("eventName"));
                    coreMessage.setDestination(destination);
                }
                
                // Payload
                Object payloadObj = map.get("payload");
                if (payloadObj instanceof Map) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> payload = (Map<String, Object>) payloadObj;
                    coreMessage.setPayload(payload);
                }
                
            } catch (Exception e) {
                log.error("Error convirtiendo Map a CoreEventMessage: {}", e.getMessage());
                throw new RuntimeException("Formato de mensaje no válido", e);
            }
        } else {
            log.warn("Tipo de mensaje no soportado: {}", rawMessage.getClass());
            throw new IllegalArgumentException("Tipo de mensaje no soportado: " + rawMessage.getClass());
        }
        
        log.info("📋 CoreEventMessage recibido - MessageId: {}, Topic: {}, EventName: {}",
            coreMessage.getMessageId(),
            coreMessage.getDestination() != null ? coreMessage.getDestination().getTopic() : "null",
            coreMessage.getDestination() != null ? coreMessage.getDestination().getEventName() : "null");
        
        // Extraer datos del payload
        Map<String, Object> corePayload = coreMessage.getPayload();
        if (corePayload == null) {
            throw new IllegalArgumentException("El payload no puede ser null");
        }
        
        log.info("📋 Payload del CORE: {}", corePayload);
        
        // El CORE Hub envuelve el payload que publicaste
        // Extraer el objeto "pago" del payload
        @SuppressWarnings("unchecked")
        Map<String, Object> pagoData = (Map<String, Object>) corePayload.get("pago");
        
        if (pagoData == null) {
            log.error("❌ No se encontró el objeto 'pago' en el payload");
            throw new IllegalArgumentException("El payload debe contener un objeto 'pago'");
        }
        
        log.info("📋 Datos del pago extraídos: {}", pagoData);
        
        // Convertir a PaymentRequestMessage
        PaymentRequestMessage message = new PaymentRequestMessage();
        message.setMessageId(coreMessage.getMessageId());
        message.setTimestamp(coreMessage.getTimestamp() != null ? coreMessage.getTimestamp().toString() : java.time.LocalDateTime.now().toString());
        
        // Destination
        PaymentRequestMessage.Destination destination = new PaymentRequestMessage.Destination();
        if (coreMessage.getDestination() != null) {
            destination.setTopic(coreMessage.getDestination().getTopic());
            destination.setEventName(coreMessage.getDestination().getEventName());
        }
        message.setDestination(destination);
        
        // Payload con el objeto pago
        PaymentRequestMessage.Payload payload = new PaymentRequestMessage.Payload();
        payload.setGeneratedAt(corePayload.get("generatedAt") != null ? corePayload.get("generatedAt").toString() : null);
        
        PaymentRequestMessage.Pago pago = new PaymentRequestMessage.Pago();
        pago.setIdCorrelacion((String) pagoData.get("idCorrelacion"));
        pago.setIdUsuario(pagoData.get("idUsuario") != null ? ((Number) pagoData.get("idUsuario")).longValue() : null);
        pago.setIdPrestador(pagoData.get("idPrestador") != null ? ((Number) pagoData.get("idPrestador")).longValue() : null);
        pago.setIdSolicitud(pagoData.get("idSolicitud") != null ? ((Number) pagoData.get("idSolicitud")).longValue() : null);
        
        if (pagoData.get("montoSubtotal") != null) {
            pago.setMontoSubtotal(new java.math.BigDecimal(pagoData.get("montoSubtotal").toString()));
        }
        if (pagoData.get("impuestos") != null) {
            pago.setImpuestos(new java.math.BigDecimal(pagoData.get("impuestos").toString()));
        }
        if (pagoData.get("comisiones") != null) {
            pago.setComisiones(new java.math.BigDecimal(pagoData.get("comisiones").toString()));
        }
        pago.setMoneda((String) pagoData.get("moneda"));
        pago.setMetodoPreferido((String) pagoData.get("metodoPreferido"));
        pago.setDescripcion((String) pagoData.get("descripcion"));
        pago.setDescripcionSolicitud((String) pagoData.get("descripcionSolicitud"));
        
        payload.setPago(pago);
        message.setPayload(payload);
        
        log.info("✅ Mensaje convertido exitosamente - MessageId: {}", message.getMessageId());
        
        return message;
    }

    
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> webhookHealth() {
        return ResponseEntity.ok(Map.of(
            "status", "UP",
            "service", "CORE Webhook Receiver"
        ));
    }

    private String extractSubscriptionId(CoreEventMessage message) {
        try {
            Map<String, Object> payload = message.getPayload();
            if (payload != null && payload.containsKey("subscriptionId")) {
                return payload.get("subscriptionId").toString();
            }
        } catch (Exception e) {
            log.warn("No se pudo extraer subscriptionId: {}", e.getMessage());
        }
        return null;
    }

    private String extractSubscriptionIdFromPaymentRequest(PaymentRequestMessage message) {
        try {
            // Primero intentar extraer subscriptionId del payload (formato CORE)
            if (message.getPayload() != null && message.getPayload().getPago() != null) {
                PaymentRequestMessage.Pago pago = message.getPayload().getPago();
                // El subscriptionId NO viene en el objeto pago, lo obtenemos del contexto de la suscripción
                // Para ahora, vamos a usar el idCorrelacion como identificador temporal
                return pago.getIdCorrelacion();
            } else if (message.getPayload() != null && message.getPayload().getCuerpo() != null) {
                // Formato antiguo
                return message.getPayload().getCuerpo().getIdCorrelacion();
            }
        } catch (Exception e) {
            log.warn("No se pudo extraer subscriptionId de PaymentRequestMessage: {}", e.getMessage());
        }
        return null;
    }

    /**
     * Guarda el evento status_updated en payment_events cuando llega del CORE
     */
    private void saveStatusUpdatedEvent(CoreEventMessage message) {
        try {
            Map<String, Object> payload = message.getPayload();
            if (payload == null) {
                log.warn("Payload vacío en evento status_updated - MessageId: {}", message.getMessageId());
                return;
            }

            // Extraer paymentId
            Object paymentIdObj = payload.get("paymentId");
            if (paymentIdObj == null) {
                log.warn("paymentId no encontrado en payload - MessageId: {}", message.getMessageId());
                return;
            }
            Long paymentId = ((Number) paymentIdObj).longValue();

            // Extraer estados
            String oldStatusStr = (String) payload.getOrDefault("oldStatus", "UNKNOWN");
            String newStatusStr = (String) payload.getOrDefault("newStatus", "UNKNOWN");
            String reason = (String) payload.getOrDefault("reason", "Status updated from CORE");

            // Mapear newStatus a PaymentEventType
            PaymentEventType eventType = mapStatusToEventType(newStatusStr);

            // Crear payload JSON para el evento
            Map<String, Object> eventPayload = new java.util.HashMap<>();
            eventPayload.put("oldStatus", oldStatusStr);
            eventPayload.put("newStatus", newStatusStr);
            eventPayload.put("reason", reason);
            eventPayload.put("amountTotal", payload.get("amountTotal"));
            eventPayload.put("currency", payload.get("currency"));
            eventPayload.put("gatewayTxnId", payload.get("gatewayTxnId"));
            eventPayload.put("coreMessageId", message.getMessageId());

            String payloadJson = objectMapper.writeValueAsString(eventPayload);

            // Guardar evento en payment_events con correlationId para idempotencia
            PaymentEvent event = new PaymentEvent();
            event.setPaymentId(paymentId);
            event.setType(eventType);
            event.setPayload(payloadJson);
            event.setActor("CORE");
            event.setEventSource("CORE_WEBHOOK");
            event.setCorrelationId(message.getMessageId()); // Guardar messageId para idempotencia
            event.setCreatedAt(java.time.LocalDateTime.now());
            paymentEventRepository.save(event);

            log.info("✅ Evento status_updated guardado en payment_events - PaymentId: {}, EventType: {}, MessageId: {}",
                paymentId, eventType, message.getMessageId());

        } catch (Exception e) {
            log.error("❌ Error guardando evento status_updated - MessageId: {}, Error: {}",
                message.getMessageId(), e.getMessage(), e);
        }
    }

    /**
     * Guarda el evento method_selected en payment_events cuando llega del CORE
     */
    private void saveMethodSelectedEvent(CoreEventMessage message) {
        try {
            Map<String, Object> payload = message.getPayload();
            if (payload == null) {
                log.warn("Payload vacío en evento method_selected - MessageId: {}", message.getMessageId());
                return;
            }

            // Extraer paymentId
            Object paymentIdObj = payload.get("paymentId");
            if (paymentIdObj == null) {
                log.warn("paymentId no encontrado en payload - MessageId: {}", message.getMessageId());
                return;
            }
            Long paymentId = ((Number) paymentIdObj).longValue();

            // Crear payload JSON para el evento
            Map<String, Object> eventPayload = new java.util.HashMap<>();
            eventPayload.put("methodType", payload.get("methodType"));
            eventPayload.put("methodId", payload.get("methodId"));
            eventPayload.put("userId", payload.get("userId"));
            eventPayload.put("selectedAt", payload.get("selectedAt"));
            eventPayload.put("methodSnapshot", payload.get("methodSnapshot"));
            eventPayload.put("coreMessageId", message.getMessageId());

            String payloadJson = objectMapper.writeValueAsString(eventPayload);

            // Guardar evento en payment_events con correlationId para idempotencia
            PaymentEvent event = new PaymentEvent();
            event.setPaymentId(paymentId);
            event.setType(PaymentEventType.PAYMENT_METHOD_UPDATED);
            event.setPayload(payloadJson);
            event.setActor("CORE");
            event.setEventSource("CORE_WEBHOOK");
            event.setCorrelationId(message.getMessageId()); // Guardar messageId para idempotencia
            event.setCreatedAt(java.time.LocalDateTime.now());
            paymentEventRepository.save(event);

            log.info("✅ Evento method_selected guardado en payment_events - PaymentId: {}, MessageId: {}",
                paymentId, message.getMessageId());

        } catch (Exception e) {
            log.error("❌ Error guardando evento method_selected - MessageId: {}, Error: {}",
                message.getMessageId(), e.getMessage(), e);
        }
    }

    /**
     * Verifica si un evento ya fue procesado basándose en el messageId
     */
    private boolean isEventAlreadyProcessed(String messageId) {
        if (messageId == null || messageId.isEmpty()) {
            return false;
        }
        
        try {
            java.util.Optional<backend_api.Backend.Entity.payment.PaymentEvent> existingEvent = 
                paymentEventRepository.findByCorrelationId(messageId);
            
            if (existingEvent.isPresent()) {
                log.info("🔍 Evento duplicado encontrado - MessageId: {}, PaymentId: {}, EventType: {}", 
                    messageId, existingEvent.get().getPaymentId(), existingEvent.get().getType());
                return true;
            }
            
            return false;
        } catch (Exception e) {
            log.error("❌ Error verificando idempotencia - MessageId: {}, Error: {}", messageId, e.getMessage());
            // En caso de error, asumir que no está procesado para evitar perder eventos
            return false;
        }
    }
    
    /**
     * Mapea el status del CORE a PaymentEventType
     */
    private PaymentEventType mapStatusToEventType(String status) {
        if (status == null) {
            return PaymentEventType.PAYMENT_PENDING;
        }

        String statusUpper = status.toUpperCase();
        switch (statusUpper) {
            case "APPROVED":
            case "COMPLETED":
                return PaymentEventType.PAYMENT_APPROVED;
            case "REJECTED":
            case "DECLINED":
                return PaymentEventType.PAYMENT_REJECTED;
            case "CANCELLED":
            case "CANCELED":
                return PaymentEventType.PAYMENT_CANCELLED;
            case "EXPIRED":
                return PaymentEventType.PAYMENT_EXPIRED;
            case "PENDING_PAYMENT":
            case "PENDING":
            default:
                return PaymentEventType.PAYMENT_PENDING;
        }
    }
}
