package backend_api.Backend.DTO.payment;

import backend_api.Backend.Entity.payment.Payment;
import backend_api.Backend.Entity.payment.PaymentMethod;
import backend_api.Backend.Entity.payment.PaymentStatus;
import backend_api.Backend.Entity.user.User;
import backend_api.Backend.Repository.UserRepository;
import backend_api.Backend.Repository.UserDataRepository;
import backend_api.Backend.Repository.ProviderDataRepository;
import backend_api.Backend.Service.Implementation.UserDataIntegrationService;
import backend_api.Backend.Entity.UserData;
import backend_api.Backend.Entity.ProviderData;
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
    
    private String user_name;      
    private String provider_name;
    
    public static PaymentResponse fromEntity(backend_api.Backend.Entity.payment.Payment payment) {
        PaymentResponse response = new PaymentResponse();
        response.setId(payment.getId());
        response.setUser_id(payment.getUser_id());
        response.setProvider_id(payment.getProvider_id());
        response.setSolicitud_id(payment.getSolicitud_id());
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

            if ("MERCHANT".equals(currentUserRole)) {
                response.setProvider_name(null);
            } else {
                response.setUser_name(null);
            }

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

            if ("MERCHANT".equals(currentUserRole)) {
                response.setProvider_name(null);
            } else {
                response.setUser_name(null);
            }

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

            if ("MERCHANT".equals(currentUserRole)) {
                response.setProvider_name(null);
            } else {
                response.setUser_name(null);
            }

        } catch (Exception e) {
            System.err.println("Error obteniendo datos reales para payment " + payment.getId() + ": " + e.getMessage());
        }

        return response;
    }
    
    /**
     * Método mejorado que busca primero en user_data/provider_data y luego en users
     */
    public static PaymentResponse fromEntityWithUnifiedNames(
            Payment payment, 
            UserRepository userRepository,
            UserDataRepository userDataRepository,
            ProviderDataRepository providerDataRepository,
            String currentUserRole) {
        
        PaymentResponse response = fromEntity(payment);

        try {
            // Buscar nombre del usuario - primero en user_data, luego en users
            if (payment.getUser_id() != null) {
                Optional<UserData> userDataOpt = userDataRepository.findByUserId(payment.getUser_id());
                if (userDataOpt.isPresent() && userDataOpt.get().getName() != null) {
                    response.setUser_name(userDataOpt.get().getName());
                } else {
                    // Fallback a users
                    Optional<User> userOpt = userRepository.findById(payment.getUser_id());
                    if (userOpt.isPresent()) {
                        response.setUser_name(userOpt.get().getName());
                    }
                }
            }

            // Buscar nombre del prestador - primero en provider_data, luego en users
            if (payment.getProvider_id() != null) {
                Optional<ProviderData> providerDataOpt = providerDataRepository.findByProviderId(payment.getProvider_id());
                if (providerDataOpt.isPresent() && providerDataOpt.get().getName() != null) {
                    response.setProvider_name(providerDataOpt.get().getName());
                } else {
                    // Fallback a users
                    Optional<User> providerOpt = userRepository.findById(payment.getProvider_id());
                    if (providerOpt.isPresent()) {
                        response.setProvider_name(providerOpt.get().getName());
                    }
                }
            }

            // Ocultar nombre según el rol
            if ("MERCHANT".equals(currentUserRole)) {
                response.setProvider_name(null);
            } else {
                response.setUser_name(null);
            }

        } catch (Exception e) {
            System.err.println("Error obteniendo nombres unificados para payment " + payment.getId() + ": " + e.getMessage());
        }

        return response;
    }
}
