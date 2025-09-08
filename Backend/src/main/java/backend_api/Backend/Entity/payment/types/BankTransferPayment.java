package backend_api.Backend.Entity.payment.types;

import backend_api.Backend.Entity.payment.PaymentMethod;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import lombok.Data;

@Data
@Entity
public class BankTransferPayment extends PaymentMethod {
    private String cbu;
    private String bank_name;
    private String alias;

    // 👇 NUEVO
    @Column(length=3, nullable=false)
    private String currency = "ARS";

    @Column(name="available_balance", precision=14, scale=2, nullable=false)
    private java.math.BigDecimal availableBalance = java.math.BigDecimal.ZERO;
}