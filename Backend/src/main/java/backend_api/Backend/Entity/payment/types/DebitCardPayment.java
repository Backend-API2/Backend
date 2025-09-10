package backend_api.Backend.Entity.payment.types;

import backend_api.Backend.Entity.payment.PaymentMethod;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@Entity
@EqualsAndHashCode(callSuper = true)
public class DebitCardPayment extends PaymentMethod {
    private String card_network;
    private String last4Digits;
    private String holder_name;
    private Integer expiration_month;
    private Integer expiration_year;
    private String bank_name;
    private String cbu;

    // vínculo a TestCard con saldo de prueba
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "test_card_id")
    private TestCard testCard;

    public DebitCardPayment() {
        setType(PaymentMethodType.DEBIT_CARD);
    }
}