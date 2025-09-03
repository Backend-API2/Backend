package backend_api.Backend.DTO.payment;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class CreatePaymentRequest {
        
    private String solicitud_reference; // ej: "SOL-2024-001"
    
   // private String cotizacion_reference; // ej: "COT-2024-005" - Se integra con el módulo Cotizacion
    
    @NotBlank(message = "Provider reference es requerido")
    private String provider_reference; // ej: "PROV-STRIPE-001", "PROV-PAYPAL-002"
    
    // Montos
    @NotNull(message = "Amount subtotal es requerido")
    @DecimalMin(value = "0.01", message = "Amount subtotal debe ser mayor a 0")
    private BigDecimal amount_subtotal;
    
    @NotNull(message = "Taxes es requerido")
    @DecimalMin(value = "0.00", message = "Taxes no puede ser negativo")
    private BigDecimal taxes;
    
    @NotNull(message = "Fees es requerido")
    @DecimalMin(value = "0.00", message = "Fees no puede ser negativo")
    private BigDecimal fees;
    
    @NotNull(message = "Currency es requerida")
    @Pattern(regexp = "^[A-Z]{3}$", message = "Currency debe ser código ISO de 3 letras (ej: USD)")
    private String currency;
    
    @NotBlank(message = "Payment method es requerido")
    @Pattern(regexp = "^(credit_card|debit_card|bank_transfer|paypal|cash)$", 
             message = "Payment method debe ser: credit_card, debit_card, bank_transfer, paypal, cash")
    private String payment_method_type;
    
    @Size(max = 1000, message = "Metadata no puede exceder 1000 caracteres")
    private String metadata;
    
    // user_id = extraído del JWT token automáticamente
    // solicitud_id = buscado por solicitud_reference  
    // cotizacion_id = buscado por cotizacion_reference - Se integra con el módulo Cotizacion
    // provider_id = buscado por provider_reference
    // payment_method_id = buscado por payment_method_type
    // amount_total = amount_subtotal + taxes + fees
    // payment_intent_id = generado por el sistema
    // gateway_txn_id = generado por el gateway 
    // status = PENDING por defecto
    // created_at, updated_at = timestamps automáticos
}

