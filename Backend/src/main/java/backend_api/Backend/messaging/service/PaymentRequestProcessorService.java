package backend_api.Backend.messaging.service;

import backend_api.Backend.Entity.UserData;
import backend_api.Backend.Entity.ProviderData;
import backend_api.Backend.Repository.UserDataRepository;
import backend_api.Backend.Repository.ProviderDataRepository;
import backend_api.Backend.messaging.dto.PaymentRequestMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class PaymentRequestProcessorService {

    private final UserDataRepository userDataRepository;
    private final ProviderDataRepository providerDataRepository;

    public Map<String, Object> processPaymentRequest(PaymentRequestMessage message) {
        try {
            log.info("🔄 Procesando solicitud de pago de matching - MessageId: {}", message.getMessageId());
            
            // Extraer datos del mensaje
            PaymentRequestMessage.Cuerpo cuerpo = message.getPayload().getCuerpo();
            String idCorrelacion = cuerpo.getIdCorrelacion();
            Long idUsuario = cuerpo.getIdUsuario();
            Long idPrestador = cuerpo.getIdPrestador();
            Long idSolicitud = cuerpo.getIdSolicitud();
            BigDecimal montoSubtotal = cuerpo.getMontoSubtotal();
            BigDecimal impuestos = cuerpo.getImpuestos();
            BigDecimal comisiones = cuerpo.getComisiones();
            String moneda = cuerpo.getMoneda();
            String metodoPreferido = cuerpo.getMetodoPreferido();

            log.info("📋 Datos extraídos - Usuario: {}, Prestador: {}, Solicitud: {}, Monto: {} {}", 
                idUsuario, idPrestador, idSolicitud, montoSubtotal, moneda);

            // Buscar datos del usuario
            Optional<UserData> userDataOpt = userDataRepository.findByUserId(idUsuario);
            if (userDataOpt.isEmpty()) {
                log.error("❌ Usuario no encontrado - ID: {}", idUsuario);
                return Map.of(
                    "success", false,
                    "error", "Usuario no encontrado",
                    "userId", idUsuario,
                    "messageId", message.getMessageId()
                );
            }

            // Buscar datos del prestador
            Optional<ProviderData> providerDataOpt = providerDataRepository.findByProviderId(idPrestador);
            if (providerDataOpt.isEmpty()) {
                log.error("❌ Prestador no encontrado - ID: {}", idPrestador);
                return Map.of(
                    "success", false,
                    "error", "Prestador no encontrado",
                    "providerId", idPrestador,
                    "messageId", message.getMessageId()
                );
            }

            UserData userData = userDataOpt.get();
            ProviderData providerData = providerDataOpt.get();

            log.info("✅ Datos encontrados - Usuario: {} {}, Prestador: {} {}", 
                userData.getName(), userData.getEmail(), 
                providerData.getName(), providerData.getEmail());

            // Calcular monto total
            BigDecimal montoTotal = montoSubtotal.add(impuestos).add(comisiones);

            // Crear pago (aquí integrarías con tu lógica de creación de pagos)
            Map<String, Object> paymentData = createPayment(
                idCorrelacion, idUsuario, idPrestador, idSolicitud,
                montoSubtotal, impuestos, comisiones, montoTotal, moneda, metodoPreferido,
                userData, providerData
            );

            log.info("✅ Solicitud de pago procesada exitosamente - MessageId: {}", message.getMessageId());

            return Map.of(
                "success", true,
                "message", "Solicitud de pago procesada exitosamente",
                "messageId", message.getMessageId(),
                "paymentData", paymentData,
                "userData", Map.of(
                    "userId", userData.getUserId(),
                    "name", userData.getName(),
                    "email", userData.getEmail()
                ),
                "providerData", Map.of(
                    "providerId", providerData.getProviderId(),
                    "name", providerData.getName(),
                    "email", providerData.getEmail()
                )
            );

        } catch (Exception e) {
            log.error("❌ Error procesando solicitud de pago - MessageId: {}, Error: {}", 
                message.getMessageId(), e.getMessage(), e);
            return Map.of(
                "success", false,
                "error", "Error procesando solicitud: " + e.getMessage(),
                "messageId", message.getMessageId()
            );
        }
    }

    private Map<String, Object> createPayment(
            String idCorrelacion, Long idUsuario, Long idPrestador, Long idSolicitud,
            BigDecimal montoSubtotal, BigDecimal impuestos, BigDecimal comisiones, BigDecimal montoTotal,
            String moneda, String metodoPreferido, UserData userData, ProviderData providerData) {
        
        // Aquí integrarías con tu lógica de creación de pagos existente
        // Por ahora retornamos los datos estructurados
        
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
        paymentData.put("status", "PENDING");
        paymentData.put("createdAt", java.time.LocalDateTime.now().toString());
        
        Map<String, Object> userInfo = new java.util.HashMap<>();
        userInfo.put("name", userData.getName());
        userInfo.put("email", userData.getEmail());
        userInfo.put("phone", userData.getPhone());
        paymentData.put("userInfo", userInfo);
        
        Map<String, Object> providerInfo = new java.util.HashMap<>();
        providerInfo.put("name", providerData.getName());
        providerInfo.put("email", providerData.getEmail());
        providerInfo.put("phone", providerData.getPhone());
        paymentData.put("providerInfo", providerInfo);
        
        return paymentData;
    }
}
