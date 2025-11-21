package backend_api.Backend.messaging.service;

import backend_api.Backend.Entity.UserData;
import backend_api.Backend.Entity.ProviderData;
import backend_api.Backend.Entity.payment.Payment;
import backend_api.Backend.Entity.payment.PaymentStatus;
import backend_api.Backend.Repository.UserDataRepository;
import backend_api.Backend.Repository.ProviderDataRepository;
import backend_api.Backend.Service.Interface.PaymentService;
import backend_api.Backend.Service.Interface.PaymentEventService;
import backend_api.Backend.Entity.payment.PaymentEventType;
import backend_api.Backend.messaging.dto.PaymentRequestMessage;
import backend_api.Backend.messaging.dto.CoreResponseMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class PaymentRequestProcessorService {

    private final UserDataRepository userDataRepository;
    private final ProviderDataRepository providerDataRepository;
    private final PaymentService paymentService;
    private final PaymentEventService paymentEventService;
    private final ObjectMapper objectMapper;
    private final CoreHubService coreHubService;

    public Map<String, Object> processPaymentRequest(PaymentRequestMessage message) {
        try {
            log.info("🔄 Procesando solicitud de pago de matching - MessageId: {}", message.getMessageId());

            // Extraer datos del mensaje
            // Manejar tanto el formato viejo (payload.cuerpo) como el nuevo (payload.pago)
            String idCorrelacion;
            Long idUsuario;
            Long idPrestador;
            Long idSolicitud;
            BigDecimal montoSubtotal;
            BigDecimal impuestos;
            BigDecimal comisiones;
            String moneda;
            String metodoPreferido;
            String descripcion;
            String descripcionSolicitud;

            if (message.getPayload().getPago() != null) {
                // Nuevo formato: usar payload.pago
                PaymentRequestMessage.Pago pago = message.getPayload().getPago();
                idCorrelacion = pago.getIdCorrelacion();
                idUsuario = pago.getIdUsuario();
                idPrestador = pago.getIdPrestador();
                idSolicitud = pago.getIdSolicitud();
                montoSubtotal = pago.getMontoSubtotal();
                impuestos = pago.getImpuestos();
                comisiones = pago.getComisiones();
                moneda = pago.getMoneda();
                metodoPreferido = pago.getMetodoPreferido();
                descripcion = pago.getDescripcion();
                descripcionSolicitud = pago.getDescripcionSolicitud();
            } else if (message.getPayload().getCuerpo() != null) {
                // Formato viejo: usar payload.cuerpo
                PaymentRequestMessage.Cuerpo cuerpo = message.getPayload().getCuerpo();
                idCorrelacion = cuerpo.getIdCorrelacion();
                idUsuario = cuerpo.getIdUsuario();
                idPrestador = cuerpo.getIdPrestador();
                idSolicitud = cuerpo.getIdSolicitud();
                montoSubtotal = cuerpo.getMontoSubtotal();
                impuestos = cuerpo.getImpuestos();
                comisiones = cuerpo.getComisiones();
                moneda = cuerpo.getMoneda();
                metodoPreferido = cuerpo.getMetodoPreferido();
                descripcion = cuerpo.getDescripcion();
                descripcionSolicitud = cuerpo.getDescripcionSolicitud();
            } else {
                log.error("❌ No se pudo extraer datos del payload");
                return Map.of(
                        "success", false,
                        "error", "Formato de payload no válido",
                        "messageId", message.getMessageId());
            }

            // Extraer generatedAt del payload para usarlo como created_at
            java.time.LocalDateTime createdAt = extractGeneratedAt(message);

            log.info("📋 Datos extraídos - Usuario: {}, Prestador: {}, Solicitud: {}, Monto: {} {}",
                    idUsuario, idPrestador, idSolicitud, montoSubtotal, moneda);
            log.info("📝 Descripción: {}, Descripción Solicitud: {}, Método Preferido: {}",
                    descripcion, descripcionSolicitud, metodoPreferido);
            log.info("📅 Fecha generación (CORE): {}", createdAt);

            // Buscar datos del usuario (si existe)
            UserData userData = null;
            if (idUsuario != null) {
                Optional<UserData> userDataOpt = userDataRepository.findByUserId(idUsuario);
                if (userDataOpt.isPresent()) {
                    userData = userDataOpt.get();
                    log.info("✅ Usuario encontrado - Name: {}, Email: {}", userData.getName(), userData.getEmail());
                } else {
                    log.warn("⚠️ Usuario no encontrado en BD - ID: {}", idUsuario);
                }
            }

            // Buscar datos del prestador (si existe)
            ProviderData providerData = null;
            if (idPrestador != null) {
                Optional<ProviderData> providerDataOpt = providerDataRepository.findByProviderId(idPrestador);
                if (providerDataOpt.isPresent()) {
                    providerData = providerDataOpt.get();
                    log.info("✅ Prestador encontrado - Name: {}, Email: {}", providerData.getName(),
                            providerData.getEmail());
                } else {
                    log.warn("⚠️ Prestador no encontrado en BD - ID: {}", idPrestador);
                }
            }

            // Calcular monto total
            BigDecimal montoTotal = montoSubtotal.add(impuestos).add(comisiones);

            // Verificar si ya existe un pago para esta solicitud (idempotencia)
            Payment existingPayment = findExistingPayment(idSolicitud, idCorrelacion);
            Payment savedPayment;

            if (existingPayment != null) {
                log.warn(
                        "⚠️ Pago duplicado detectado - Ya existe un pago con solicitud_id: {} o idCorrelacion: {}. Retornando pago existente (ID: {})",
                        idSolicitud, idCorrelacion, existingPayment.getId());
                // Actualizar campos que podrían estar null en el pago existente
                boolean needsUpdate = false;
                if (existingPayment.getDescripcion() == null && descripcion != null) {
                    existingPayment.setDescripcion(descripcion);
                    needsUpdate = true;
                }
                if (existingPayment.getDescripcionSolicitud() == null && descripcionSolicitud != null) {
                    existingPayment.setDescripcionSolicitud(descripcionSolicitud);
                    needsUpdate = true;
                }
                // Corregir created_at si es diferente al generatedAt (pagos creados con código
                // viejo)
                if (existingPayment.getCreated_at() != null && !existingPayment.getCreated_at().equals(createdAt)) {
                    log.info("🔄 Corrigiendo created_at del pago duplicado - Viejo: {}, Nuevo (CORE): {}",
                            existingPayment.getCreated_at(), createdAt);
                    existingPayment.setCreated_at(createdAt);
                    needsUpdate = true;
                }
                // Actualizar metadata si metodoPreferido está null
                if (metodoPreferido != null) {
                    try {
                        String metadataStr = existingPayment.getMetadata();
                        if (metadataStr != null && !metadataStr.isEmpty()) {
                            @SuppressWarnings("unchecked")
                            Map<String, Object> metadata = objectMapper.readValue(metadataStr, Map.class);
                            if (metadata.get("metodoPreferido") == null) {
                                metadata.put("metodoPreferido", metodoPreferido);
                                existingPayment.setMetadata(objectMapper.writeValueAsString(metadata));
                                needsUpdate = true;
                            }
                        }
                    } catch (Exception e) {
                        log.warn("⚠️ Error actualizando metadata del pago duplicado: {}", e.getMessage());
                    }
                }
                if (needsUpdate) {
                    existingPayment.setUpdated_at(java.time.LocalDateTime.now());
                    savedPayment = paymentService.createPayment(existingPayment);
                    log.info("✅ Pago duplicado actualizado con campos faltantes - PaymentId: {}", savedPayment.getId());
                } else {
                    savedPayment = existingPayment;
                }
                // NO guardar evento duplicado - el evento ya se guardó cuando se creó el pago
                // originalmente
            } else {
                // DOUBLE-CHECK: Verificar nuevamente justo antes de crear (para evitar
                // condición de carrera)
                // Esto previene que dos requests simultáneos creen pagos duplicados
                Payment doubleCheckPayment = findExistingPayment(idSolicitud, idCorrelacion);
                if (doubleCheckPayment != null) {
                    log.warn(
                            "⚠️ Pago duplicado detectado en double-check - Ya existe un pago con solicitud_id: {} o idCorrelacion: {}. Retornando pago existente (ID: {})",
                            idSolicitud, idCorrelacion, doubleCheckPayment.getId());
                    savedPayment = doubleCheckPayment;
                    existingPayment = doubleCheckPayment; // Marcar como duplicado para no enviar evento
                } else {
                    // Crear pago (aquí integrarías con tu lógica de creación de pagos)
                    savedPayment = createPayment(
                            idCorrelacion, idUsuario, idPrestador, idSolicitud,
                            montoSubtotal, impuestos, comisiones, montoTotal, moneda, metodoPreferido,
                            descripcion, descripcionSolicitud,
                            userData, providerData, createdAt);
                }
            }

            // Enviar evento de pago creado al CORE solo si es un pago nuevo (no duplicado)
            if (existingPayment == null) {
                sendPaymentCreatedEvent(savedPayment, idSolicitud, message.getMessageId());
            } else {
                log.info("⏭️ Omitiendo envío de evento al CORE - Pago duplicado ya procesado anteriormente");
            }

            log.info("✅ Solicitud de pago procesada exitosamente - MessageId: {}", message.getMessageId());

            // Preparar respuesta
            Map<String, Object> responseMap = new java.util.HashMap<>();
            if (existingPayment != null) {
                responseMap.put("success", true);
                responseMap.put("message", "Solicitud de pago ya procesada anteriormente (duplicado detectado)");
                responseMap.put("duplicate", true);
            } else {
                responseMap.put("success", true);
                responseMap.put("message", "Solicitud de pago procesada exitosamente");
                responseMap.put("duplicate", false);
            }
            responseMap.put("messageId", message.getMessageId());

            // Preparar paymentData
            Map<String, Object> paymentData = new java.util.HashMap<>();
            paymentData.put("idCorrelacion", idCorrelacion);
            paymentData.put("idUsuario", idUsuario);
            paymentData.put("idPrestador", idPrestador);
            paymentData.put("idSolicitud", idSolicitud);
            paymentData.put("montoSubtotal", montoSubtotal);
            paymentData.put("impuestos", impuestos);
            paymentData.put("comisiones", comisiones);
            paymentData.put("montoTotal", montoTotal);
            paymentData.put("moneda", moneda);
            paymentData.put("metodoPreferido", metodoPreferido);
            paymentData.put("descripcion", descripcion);
            paymentData.put("descripcionSolicitud", descripcionSolicitud);
            paymentData.put("paymentId", savedPayment.getId());
            paymentData.put("status", savedPayment.getStatus());

            responseMap.put("paymentData", paymentData);

            // Agregar datos de usuario si existen
            if (userData != null) {
                responseMap.put("userData", Map.of(
                        "userId", userData.getUserId(),
                        "name", userData.getName(),
                        "email", userData.getEmail()));
            } else {
                responseMap.put("userData", null);
            }

            // Agregar datos de prestador si existen
            if (providerData != null) {
                responseMap.put("providerData", Map.of(
                        "providerId", providerData.getProviderId(),
                        "name", providerData.getName(),
                        "email", providerData.getEmail()));
            } else {
                responseMap.put("providerData", null);
            }

            return responseMap;

        } catch (Exception e) {
            log.error("❌ Error procesando solicitud de pago - MessageId: {}, Error: {}",
                    message.getMessageId(), e.getMessage(), e);
            return Map.of(
                    "success", false,
                    "error", "Error procesando solicitud: " + e.getMessage(),
                    "messageId", message.getMessageId());
        }
    }

    /**
     * Busca un pago existente basado en solicitud_id o idCorrelacion para prevenir
     * duplicados
     * IMPORTANTE: Este método debe ser llamado ANTES de crear un pago para
     * verificar idempotencia
     */
    private Payment findExistingPayment(Long solicitudId, String idCorrelacion) {
        log.debug("🔍 Buscando pago existente - SolicitudId: {}, IdCorrelacion: {}", solicitudId, idCorrelacion);

        // Primero buscar por solicitud_id (más rápido y directo)
        if (solicitudId != null) {
            java.util.List<Payment> paymentsBySolicitud = paymentService.getPaymentsBySolicitudId(solicitudId);
            if (!paymentsBySolicitud.isEmpty()) {
                Payment found = paymentsBySolicitud.get(0);
                log.info("🔍 ✅ Pago existente encontrado por solicitud_id: {} - PaymentId: {}, Status: {}",
                        solicitudId, found.getId(), found.getStatus());
                return found;
            } else {
                log.debug("🔍 ❌ No se encontró pago por solicitud_id: {}", solicitudId);
            }
        }

        // Si no se encontró por solicitud_id, buscar por idCorrelacion en metadata
        if (idCorrelacion != null && !idCorrelacion.isEmpty()) {
            try {
                // Buscar en todos los pagos recientes (últimos 1000) que tengan metadata
                // Nota: Esta es una búsqueda menos eficiente, pero necesaria para idempotencia
                // completa
                log.debug("🔍 Buscando pago por idCorrelacion en metadata: {}", idCorrelacion);
                java.util.List<Payment> recentPayments = paymentService.getAllPayments(0, 1000);
                log.debug("🔍 Revisando {} pagos recientes para buscar idCorrelacion: {}", recentPayments.size(),
                        idCorrelacion);

                for (Payment payment : recentPayments) {
                    if (payment.getMetadata() != null && !payment.getMetadata().isEmpty()) {
                        try {
                            @SuppressWarnings("unchecked")
                            Map<String, Object> metadata = objectMapper.readValue(
                                    payment.getMetadata(),
                                    Map.class);
                            String existingIdCorrelacion = (String) metadata.get("idCorrelacion");
                            if (idCorrelacion.equals(existingIdCorrelacion)) {
                                log.info(
                                        "🔍 ✅ Pago existente encontrado por idCorrelacion: {} - PaymentId: {}, SolicitudId: {}, Status: {}",
                                        idCorrelacion, payment.getId(), payment.getSolicitud_id(), payment.getStatus());
                                return payment;
                            }
                        } catch (Exception e) {
                            // Ignorar errores al parsear metadata de otros pagos
                            log.debug("⚠️ Error parseando metadata del pago {}: {}", payment.getId(), e.getMessage());
                            continue;
                        }
                    }
                }
                log.debug("🔍 ❌ No se encontró pago por idCorrelacion: {}", idCorrelacion);
            } catch (Exception e) {
                log.warn("⚠️ Error buscando pago por idCorrelacion: {}", e.getMessage(), e);
            }
        }

        log.debug("🔍 ❌ No se encontró pago existente - SolicitudId: {}, IdCorrelacion: {}", solicitudId,
                idCorrelacion);
        return null;
    }

    private Payment createPayment(
            String idCorrelacion, Long idUsuario, Long idPrestador, Long idSolicitud,
            BigDecimal montoSubtotal, BigDecimal impuestos, BigDecimal comisiones, BigDecimal montoTotal,
            String moneda, String metodoPreferido, String descripcion, String descripcionSolicitud,
            UserData userData, ProviderData providerData, java.time.LocalDateTime createdAt) {

        log.info("💾 Creando pago en base de datos - Usuario: {}, Prestador: {}, Monto: {}",
                idUsuario, idPrestador, montoTotal);

        // Crear entidad Payment
        Payment payment = new Payment();
        payment.setUser_id(idUsuario);
        payment.setProvider_id(idPrestador);
        payment.setSolicitud_id(idSolicitud);
        payment.setAmount_subtotal(montoSubtotal);
        payment.setAmount_total(montoTotal);
        payment.setTaxes(impuestos);
        payment.setFees(comisiones);
        payment.setCurrency(moneda);
        payment.setStatus(PaymentStatus.PENDING_PAYMENT);
        payment.setCreated_at(createdAt); // Usar fecha del CORE en lugar de now()
        payment.setUpdated_at(java.time.LocalDateTime.now());
        payment.setDescripcion(descripcion);
        payment.setDescripcionSolicitud(descripcionSolicitud);

        // Crear metadata (sin descripcion y descripcionSolicitud, ahora tienen columnas
        // propias)
        Map<String, Object> metadata = new java.util.HashMap<>();
        metadata.put("idCorrelacion", idCorrelacion);
        metadata.put("metodoPreferido", metodoPreferido);
        if (userData != null) {
            metadata.put("userName", userData.getName());
            metadata.put("userEmail", userData.getEmail());
        }
        if (providerData != null) {
            metadata.put("providerName", providerData.getName());
            metadata.put("providerEmail", providerData.getEmail());
        }

        try {
            payment.setMetadata(objectMapper.writeValueAsString(metadata));
        } catch (Exception e) {
            log.error("Error serializando metadata: {}", e.getMessage());
            payment.setMetadata("{}");
        }

        // Guardar en base de datos
        Payment savedPayment = paymentService.createPayment(payment);
        if (savedPayment == null) {
            throw new RuntimeException("No se pudo guardar el pago en la base de datos");
        }
        log.info("✅ Pago guardado exitosamente - ID: {}, Usuario: {}, Prestador: {}",
                savedPayment.getId(), savedPayment.getUser_id(), savedPayment.getProvider_id());

        // Registrar evento en el timeline (solo cuando se crea un pago nuevo, no
        // duplicado)
        // El evento se guarda aquí, no cuando el pago es duplicado
        try {
            paymentEventService.createEvent(
                    savedPayment.getId(),
                    PaymentEventType.PAYMENT_PENDING,
                    String.format(
                            "{\"amount_total\": %s, \"currency\": \"%s\", \"solicitud_id\": %s, \"source\": \"matching\", \"idCorrelacion\": \"%s\"}",
                            savedPayment.getAmount_total(), savedPayment.getCurrency(), idSolicitud, idCorrelacion),
                    "system");
            log.info("📝 Evento PAYMENT_PENDING registrado en timeline - PaymentId: {}, IdCorrelacion: {}",
                    savedPayment.getId(), idCorrelacion);
        } catch (Exception e) {
            log.error("⚠️ Error registrando evento en timeline - PaymentId: {}, Error: {}",
                    savedPayment.getId(), e.getMessage());
            // No lanzar excepción para no interrumpir el flujo
        }

        return savedPayment;
    }

    private Map<String, Object> buildPaymentResponseData(
            String idCorrelacion, Long idUsuario, Long idPrestador, Long idSolicitud,
            BigDecimal montoSubtotal, BigDecimal impuestos, BigDecimal comisiones, BigDecimal montoTotal,
            String moneda, String metodoPreferido, String descripcion, String descripcionSolicitud,
            Payment savedPayment, UserData userData, ProviderData providerData) {

        Map<String, Object> paymentData = new java.util.HashMap<>();
        paymentData.put("paymentId", savedPayment.getId());
        paymentData.put("idCorrelacion", idCorrelacion);
        paymentData.put("idUsuario", idUsuario);
        paymentData.put("idPrestador", idPrestador);
        paymentData.put("idSolicitud", idSolicitud);
        paymentData.put("montoSubtotal", montoSubtotal);
        paymentData.put("impuestos", impuestos);
        paymentData.put("comisiones", comisiones);
        paymentData.put("montoTotal", montoTotal);
        paymentData.put("moneda", moneda);
        paymentData.put("metodoPreferido", metodoPreferido);
        paymentData.put("descripcion", descripcion);
        paymentData.put("descripcionSolicitud", descripcionSolicitud);
        paymentData.put("status", "PENDING");
        paymentData.put("createdAt", savedPayment.getCreated_at().toString());

        // Agregar información del usuario si existe
        if (userData != null) {
            Map<String, Object> userInfo = new java.util.HashMap<>();
            userInfo.put("name", userData.getName());
            userInfo.put("email", userData.getEmail());
            userInfo.put("phone", userData.getPhone());
            paymentData.put("userInfo", userInfo);
        } else {
            paymentData.put("userInfo", null);
        }

        // Agregar información del prestador si existe
        if (providerData != null) {
            Map<String, Object> providerInfo = new java.util.HashMap<>();
            providerInfo.put("name", providerData.getName());
            providerInfo.put("email", providerData.getEmail());
            providerInfo.put("phone", providerData.getPhone());
            paymentData.put("providerInfo", providerInfo);
        } else {
            paymentData.put("providerInfo", null);
        }

        return paymentData;
    }

    private void sendPaymentCreatedEvent(Payment payment, Long solicitudId, String originalMessageId) {
        try {
            log.info("📤 Enviando evento de pago creado al CORE - PaymentId: {}, SolicitudId: {}",
                    payment.getId(), solicitudId);

            Map<String, Object> payload = new java.util.HashMap<>();
            payload.put("paymentId", payment.getId());
            payload.put("solicitudId", solicitudId);
            payload.put("status", payment.getStatus() != null ? payment.getStatus().toString() : "UNKNOWN");
            payload.put("amount", payment.getAmount_total());
            payload.put("currency", payment.getCurrency());
            payload.put("userId", payment.getUser_id());
            payload.put("providerId", payment.getProvider_id());
            payload.put("originalMessageId", originalMessageId);

            CoreResponseMessage confirmation = CoreResponseMessage.builder()
                    .messageId(UUID.randomUUID().toString())
                    .timestamp(Instant.now().toString())
                    .destination(CoreResponseMessage.Destination.builder()
                            .topic("payment")
                            .eventName("created")
                            .build())
                    .payload(payload)
                    .build();

            // Enviar al CORE y obtener la respuesta
            Map<String, Object> coreResponse = coreHubService.publishMessage(confirmation);

            Boolean success = (Boolean) coreResponse.get("success");
            if (Boolean.TRUE.equals(success)) {
                log.info("✅ Evento de pago creado enviado exitosamente al CORE - PaymentId: {}", payment.getId());
                log.info("📋 Respuesta del CORE: {}", coreResponse.get("response"));
                log.info("📊 Status Code: {}", coreResponse.get("statusCode"));
            } else {
                log.error("❌ Error enviando evento al CORE - PaymentId: {}, Error: {}",
                        payment.getId(), coreResponse.get("error"));
            }
        } catch (Exception e) {
            log.error("❌ Error enviando evento de pago creado al CORE - PaymentId: {}, Error: {}",
                    payment.getId(), e.getMessage(), e);
        }
    }

    /**
     * Extrae el campo generatedAt del payload del mensaje y lo convierte a
     * LocalDateTime
     * Si no está presente o falla el parseo, retorna la fecha actual como fallback
     */
    private java.time.LocalDateTime extractGeneratedAt(PaymentRequestMessage message) {
        try {
            String generatedAtStr = message.getPayload().getGeneratedAt();
            if (generatedAtStr != null && !generatedAtStr.isEmpty()) {
                // El formato es ISO 8601: "2025-10-24T23:55:44.896903Z"
                java.time.Instant instant = java.time.Instant.parse(generatedAtStr);
                java.time.LocalDateTime localDateTime = java.time.LocalDateTime.ofInstant(
                        instant,
                        java.time.ZoneId.systemDefault());
                log.debug("✅ GeneratedAt extraído exitosamente: {} → {}", generatedAtStr, localDateTime);
                return localDateTime;
            } else {
                log.warn("⚠️ GeneratedAt no está presente en el payload, usando fecha actual");
                return java.time.LocalDateTime.now();
            }
        } catch (Exception e) {
            log.warn("⚠️ Error parseando generatedAt: {}, usando fecha actual", e.getMessage());
            return java.time.LocalDateTime.now();
        }
    }
}
