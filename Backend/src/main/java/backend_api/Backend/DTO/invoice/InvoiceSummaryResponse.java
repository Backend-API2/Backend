package backend_api.Backend.DTO.invoice;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InvoiceSummaryResponse {
    
    private Long totalInvoices;
    private Long paidInvoices;
    private Long pendingInvoices;
    private Long overdueInvoices;
    private BigDecimal totalAmount;
    private BigDecimal paidAmount;
    private BigDecimal pendingAmount;
    private BigDecimal overdueAmount;
}
