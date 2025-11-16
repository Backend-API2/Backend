package backend_api.Backend.DTO.payment;

import backend_api.Backend.Entity.payment.Payment;
import backend_api.Backend.Entity.payment.PaymentMethod;
import backend_api.Backend.Entity.payment.PaymentStatus;
import backend_api.Backend.Entity.user.User;
import backend_api.Backend.Repository.UserRepository;
import backend_api.Backend.Service.Implementation.UserDataIntegrationService;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Map;

@Data
public class PaymentResponse {
    
    
    private Long id;
    private Long user_id;
    private Long provider_id; 
    private Long solicitud_id;
    private Long cotizacion_id;
    
    private BigDecimal amount_subtotal;
    private BigDecimal taxes;
    private BigDecimal fees; 
    private BigDecimal amount_total;
    private String currency;
    private PaymentMethod method; 

    private PaymentStatus status;
    
    private LocalDateTime created_at;
    private LocalDateTime updated_at;
    private LocalDateTime captured_at;
    private LocalDateTime expired_at;
    
    private String metadata;
    private String descripcion;
    private String descripcion_solicitud;
    private String gateway_txn_id;
    private Boolean rejected_by_balance;
    private Integer retry_attempts;
    
    private String user_name;      
    private String provider_name;
    
    public static PaymentResponse fromEntity(backend_api.Backend.Entity.payment.Payment payment) {
        PaymentResponse response = new PaymentResponse();
        response.setId(payment.getId());
        response.setUser_id(payment.getUser_id());
        response.setProvider_id(payment.getProvider_id());
        response.setSolicitud_id(payment.getSolicitud_id());
        response.setCotizacion_id(payment.getCotizacion_id());
        response.setAmount_subtotal(payment.getAmount_subtotal());
        response.setTaxes(payment.getTaxes());
        response.setFees(payment.getFees());
        response.setAmount_total(payment.getAmount_total());
        response.setCurrency(payment.getCurrency());
        response.setMethod(payment.getMethod());
        response.setStatus(payment.getStatus());
        response.setCreated_at(payment.getCreated_at());
        response.setUpdated_at(payment.getUpdated_at());
        response.setCaptured_at(payment.getCaptured_at());
        response.setExpired_at(payment.getExpired_at());
        response.setMetadata(payment.getMetadata());
        response.setDescripcion(payment.getDescripcion());
        response.setDescripcion_solicitud(payment.getDescripcionSolicitud());
        response.setGateway_txn_id(payment.getGateway_txn_id());
        response.setRejected_by_balance(payment.getRejected_by_balance());
        response.setRetry_attempts(payment.getRetry_attempts());
        return response;
    }
    
    public static PaymentResponse fromEntityWithNames(Payment payment, UserRepository userRepository, String currentUserRole) {
        PaymentResponse response = fromEntity(payment);

        try {
            Optional<User> userOpt = userRepository.findById(payment.getUser_id());
            if (userOpt.isPresent()) {
                response.setUser_name(userOpt.get().getName());
            }

            Optional<User> providerOpt = userRepository.findById(payment.getProvider_id());
            if (providerOpt.isPresent()) {
                response.setProvider_name(providerOpt.get().getName());
            }

            // Lógica de visibilidad según rol:
            // - MERCHANT: solo ve user_name (oculta provider_name porque es él mismo)
            // - USER: solo ve provider_name (oculta user_name porque es él mismo)
            // - ADMIN: ve ambos nombres (necesita ver toda la información)
            // - null/vacío/desconocido: se comporta como USER (oculta user_name)
            if ("MERCHANT".equals(currentUserRole)) {
                response.setProvider_name(null);
            } else if (!"ADMIN".equals(currentUserRole)) {
                // Si no es MERCHANT ni ADMIN, ocultar user_name (comportamiento por defecto para USER y roles desconocidos)
                response.setUser_name(null);
            }
            // ADMIN no oculta ningún nombre - puede ver toda la información

        } catch (Exception e) {
            System.err.println("Error obteniendo nombres para payment " + payment.getId() + ": " + e.getMessage());
        }

        return response;
    }

    public static PaymentResponse fromEntityWithNamesOptimized(Payment payment, Map<Long, User> userMap, String currentUserRole) {
        PaymentResponse response = fromEntity(payment);

        try {
            User user = userMap.get(payment.getUser_id());
            if (user != null) {
                response.setUser_name(user.getName());
            }

            User provider = userMap.get(payment.getProvider_id());
            if (provider != null) {
                response.setProvider_name(provider.getName());
            }

            // Lógica de visibilidad según rol:
            // - MERCHANT: solo ve user_name (oculta provider_name porque es él mismo)
            // - USER: solo ve provider_name (oculta user_name porque es él mismo)
            // - ADMIN: ve ambos nombres (necesita ver toda la información)
            // - null/vacío/desconocido: se comporta como USER (oculta user_name)
            if ("MERCHANT".equals(currentUserRole)) {
                response.setProvider_name(null);
            } else if (!"ADMIN".equals(currentUserRole)) {
                // Si no es MERCHANT ni ADMIN, ocultar user_name (comportamiento por defecto para USER y roles desconocidos)
                response.setUser_name(null);
            }
            // ADMIN no oculta ningún nombre - puede ver toda la información

        } catch (Exception e) {
            System.err.println("Error obteniendo nombres para payment " + payment.getId() + ": " + e.getMessage());
        }

        return response;
    }

    public static PaymentResponse fromEntityWithRealUserData(Payment payment, Map<Long, UserDataIntegrationService.UserInfo> userInfoMap, String currentUserRole) {
        PaymentResponse response = fromEntity(payment);

        try {
            UserDataIntegrationService.UserInfo userInfo = userInfoMap.get(payment.getUser_id());
            if (userInfo != null) {
                response.setUser_name(userInfo.getName());
            }

            UserDataIntegrationService.UserInfo providerInfo = userInfoMap.get(payment.getProvider_id());
            if (providerInfo != null) {
                response.setProvider_name(providerInfo.getName());
            }

            // Lógica de visibilidad según rol:
            // - MERCHANT: solo ve user_name (oculta provider_name porque es él mismo)
            // - USER: solo ve provider_name (oculta user_name porque es él mismo)
            // - ADMIN: ve ambos nombres (necesita ver toda la información)
            // - null/vacío/desconocido: se comporta como USER (oculta user_name)
            if ("MERCHANT".equals(currentUserRole)) {
                response.setProvider_name(null);
            } else if (!"ADMIN".equals(currentUserRole)) {
                // Si no es MERCHANT ni ADMIN, ocultar user_name (comportamiento por defecto para USER y roles desconocidos)
                response.setUser_name(null);
            }
            // ADMIN no oculta ningún nombre - puede ver toda la información
            
        } catch (Exception e) {
            System.err.println("Error obteniendo datos reales para payment " + payment.getId() + ": " + e.getMessage());
        }

        return response;
    }
}
