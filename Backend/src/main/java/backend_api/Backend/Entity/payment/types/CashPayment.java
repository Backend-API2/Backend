package backend_api.Backend.Entity.payment.types;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.time.LocalDateTime;

import backend_api.Backend.Entity.payment.PaymentMethod;

@Entity
@Data
@EqualsAndHashCode(callSuper = true)
public class CashPayment extends PaymentMethod {
    
    private String branchCode;          // Código de sucursal
    private String branchName;          // Nombre de la sucursal
    private String branchAddress;       // Dirección de la sucursal
    private String branchCity;          // Ciudad
    private String branchState;         // Estado/Provincia
    private String branchPostalCode;    // Código postal
    private String branchPhone;         // Teléfono de sucursal
    
    private String paymentCode;         // Código de pago único
    private LocalDateTime expirationDate; // Fecha límite para pagar
    
    private String cashierName;         // Nombre del cajero (cuando se paga)
    private String receiptNumber;       // Número de recibo
    private LocalDateTime paidAt;       // Fecha y hora del pago

    public CashPayment() {
        this.setType(PaymentMethodType.CASH);
    }
}
