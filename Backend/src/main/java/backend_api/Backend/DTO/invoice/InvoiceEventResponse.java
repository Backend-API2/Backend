package backend_api.Backend.DTO.invoice;

import backend_api.Backend.Entity.invoice.InvoiceEventType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InvoiceEventResponse {
    
    private Long id;
    private Long invoiceId;
    private InvoiceEventType eventType;
    private String description;
    private Long createdBy;
    private LocalDateTime createdAt;
    private String eventData;
    private String ipAddress;
    private String userAgent;
}
