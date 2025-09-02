package backend_api.Backend.DTO.invoice;

import backend_api.Backend.Entity.invoice.InvoiceStatus;
import backend_api.Backend.Entity.invoice.InvoiceType;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class InvoiceSearchRequest {
    
    private Long userId;
    
    private Long providerId;
    
    private InvoiceStatus status;
    
    private InvoiceType type;
    
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime startDate;
    
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime endDate;
    
    private BigDecimal minAmount;
    
    private BigDecimal maxAmount;
    
    private String invoiceNumber;
    
    private String currency;
    
    private Integer page = 0;
    private Integer size = 10;
    private String sortBy = "createdAt";
    private String sortDirection = "desc";
}
