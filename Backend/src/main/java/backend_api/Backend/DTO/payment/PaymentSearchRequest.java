package backend_api.Backend.DTO.payment;

import backend_api.Backend.Entity.payment.PaymentStatus;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import lombok.Data;

@Data
public class PaymentSearchRequest {
    
    private PaymentStatus status;
    private String currency;
    private BigDecimal minAmount;
    private BigDecimal maxAmount;
    private LocalDate startDate;
    private LocalDate endDate;
    
    private Long userId;
    private String userName;
    private List<Long> userIds;
    
    private Long providerId;
    private List<Long> providerIds;
    
    private Long solicitudId;
  //  private Long cotizacionId; // Se integra con el módulo Cotizacion
    
    private String metadataKey;
    private String metadataValue;
    
    // Paginación y ordenamiento
    @Min(value = 0, message = "Page number must be non-negative")
    private int page = 0;
    
    @Min(value = 1, message = "Page size must be positive")
    private int size = 10;
    
    @Pattern(regexp = "^(id|user_id|provider_id|amount_total|created_at|updated_at)$", 
             message = "Invalid sort field")
    private String sortBy = "created_at";
    
    @Pattern(regexp = "^(asc|desc)$", message = "Sort direction must be 'asc' or 'desc'")
    private String sortDir = "desc";
    
    public PaymentSearchRequest() {
    }
}

