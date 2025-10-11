package backend_api.Backend.Entity.payment.types;

import backend_api.Backend.Entity.payment.PaymentMethod;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public abstract class CardPayment extends PaymentMethod {
        private String card_network;
        private String last4Digits;
        private String holder_name;
        private Integer expiration_month;
        private Integer expiration_year;
}
