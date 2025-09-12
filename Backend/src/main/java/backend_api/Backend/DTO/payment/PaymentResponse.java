package backend_api.Backend.DTO.payment;

import backend_api.Backend.Entity.payment.Payment;
import backend_api.Backend.Entity.payment.PaymentMethod;
import backend_api.Backend.Entity.payment.PaymentStatus;
import backend_api.Backend.Entity.user.User;
import backend_api.Backend.Repository.UserRepository;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

@Data
public class PaymentResponse {
    
    
    private Long id;
    private Long user_id;
    private Long provider_id; 
    private Long solicitud_id;
    private Long refund_id;
  //  private Long cotizacion_id; // Se integra con el módulo Cotizaciono
  //  private Long cotizacion_id; // Se integra con el módulo Cotizacion
    
    private BigDecimal amount_subtotal;
    private BigDecimal taxes;
    private BigDecimal fees; 
    private BigDecimal amount_total;
    private String currency;
    private PaymentMethod method; 

    private PaymentStatus status;
    
    private LocalDateTime created_at;
    private LocalDateTime updated_at;
    private LocalDateTime captured_at; // cuando se completó el pago
    private LocalDateTime expired_at;   // cuando expira (si aplica)
    
    private String metadata;
    
    private String user_name;      
    private String provider_name;  
    
    // gateway_txn_id - ID interno del gateway (sensible)
    // method - puede ser complejo, mejor endpoint separado si se necesita
    
    public static PaymentResponse fromEntity(backend_api.Backend.Entity.payment.Payment payment) {
        PaymentResponse response = new PaymentResponse();
        response.setId(payment.getId());
        response.setUser_id(payment.getUser_id());
        response.setProvider_id(payment.getProvider_id());
        response.setSolicitud_id(payment.getSolicitud_id());
        response.setRefund_id(payment.getRefund_id());
      //  response.setCotizacion_id(payment.getCotizacion_id());
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
}
