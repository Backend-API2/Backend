package backend_api.Backend.Entity.payment.types;

import backend_api.Backend.Entity.payment.PaymentMethod;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Entity
@Data
@EqualsAndHashCode(callSuper = true)
public class MercadoPagoPayment extends PaymentMethod {
    private String mercadoPagoUserId;
    private String accessToken;
    private String preferenceId;
    private String installments;
    private String issuer;

    // 👇 NUEVO
    @Column(length=3, nullable=false)
    private String currency = "ARS";

    @Column(name="available_balance", precision=14, scale=2, nullable=false)
    private java.math.BigDecimal availableBalance = java.math.BigDecimal.ZERO;

    public MercadoPagoPayment() {
        this.setType(PaymentMethodType.MERCADO_PAGO);
    }
}