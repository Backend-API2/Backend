package backend_api.Backend.DTO.payment;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class ConfirmPaymentRequest {
    
    @NotBlank(message = "Payment method type es requerido")
    @Pattern(regexp = "^(credit_card|debit_card|bank_transfer|paypal|cash)$", 
             message = "Payment method debe ser: credit_card, debit_card, bank_transfer, paypal, cash")
    private String paymentMethodType;
    
    private String paymentMethodId;
    
    private boolean captureImmediately = true;
    
    private String metadata;
}
