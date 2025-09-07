package backend_api.Backend.DTO.payment;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class SelectPaymentMethodRequest {
    
    @NotBlank(message = "Payment method type es requerido")
    @Pattern(regexp = "^(CREDIT_CARD|DEBIT_CARD|BANK_TRANSFER|PAYPAL|CASH|MERCADO_PAGO)$", 
             message = "Payment method debe ser: CREDIT_CARD, DEBIT_CARD, BANK_TRANSFER, PAYPAL, CASH, MERCADO_PAGO")
    private String paymentMethodType;
    
    private String mercadoPagoUserId;
    private String accessToken;
    private String branchCode;
    private String branchName;
    private String branchAddress;
    private String paypalEmail;
    private String bankAccount;
    
    private String cardNumber;
    private String cardHolderName;
    private Integer expirationMonth;
    private Integer expirationYear;
    private String cvv;
    
    private String bankName;
    private String cbu;
}
