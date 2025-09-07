package backend_api.Backend.DTO.refund;

import backend_api.Backend.Entity.refund.Refund;
import backend_api.Backend.Entity.refund.RefundStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class RefundResponse {

    private Long id;
    private Long paymentId;
    private BigDecimal amount;
    private String reason;
    private RefundStatus status;
    private String gatewayRefundId;
    private LocalDateTime createdAt;
    private String metadata; // si lo agregás a la entidad

    public static RefundResponse fromEntity(Refund refund) {
        RefundResponse r = new RefundResponse();
        r.setId(refund.getRefund_id());
        r.setPaymentId(refund.getPayment_id()); // <- mapea del campo de entidad (typo)
        r.setAmount(refund.getAmount());
        r.setReason(refund.getReason());
        r.setStatus(refund.getStatus());
        r.setGatewayRefundId(refund.getGateway_refund_id());
        r.setCreatedAt(refund.getCreated_at());
        // r.setMetadata(refund.getMetadata()); // si agregás ese campo en la entidad
        return r;
    }
}