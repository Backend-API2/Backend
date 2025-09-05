package backend_api.Backend.DTO.payment;

import backend_api.Backend.Entity.payment.PaymentMethod;
import backend_api.Backend.Entity.payment.PaymentStatus;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class PaymentResponse {
    
    
    private Long id;
    private Long user_id;
    private Long provider_id; 
    private Long solicitud_id;
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
    
    // gateway_txn_id - ID interno del gateway (sensible)
    // method - puede ser complejo, mejor endpoint separado si se necesita
    
    public static PaymentResponse fromEntity(backend_api.Backend.Entity.payment.Payment payment) {
        PaymentResponse response = new PaymentResponse();
        response.setId(payment.getId());
        response.setUser_id(payment.getUser_id());
        response.setProvider_id(payment.getProvider_id());
        response.setSolicitud_id(payment.getSolicitud_id());
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
}
