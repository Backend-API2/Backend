package backend_api.Backend.DTO.refund;

import backend_api.Backend.Entity.refund.RefundStatus;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class RefundSearchRequest {

    // Filtros
    private RefundStatus status;
    private Long paymentId;
    private List<Long> paymentIds;

    private BigDecimal minAmount;
    private BigDecimal maxAmount;

    private LocalDateTime startDate; // filtra por createdAt >=
    private LocalDateTime endDate;   // filtra por createdAt <=

    private String reasonContains;   // búsqueda por texto libre

    // Paginación / orden
    @Min(value = 0, message = "page debe ser >= 0")
    private int page = 0;

    @Min(value = 1, message = "size debe ser >= 1")
    private int size = 10;

    @Pattern(regexp = "^(id|payment_id|amount|status|created_at)$",
            message = "sortBy inválido")
    private String sortBy = "created_at";

    @Pattern(regexp = "^(asc|desc)$",
            message = "sortDir debe ser 'asc' o 'desc'")
    private String sortDir = "desc";
}