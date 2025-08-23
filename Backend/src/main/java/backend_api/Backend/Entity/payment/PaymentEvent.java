package backend_api.Backend.Entity.payment;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "payment_events")
public class PaymentEvent {
    private Long id;
    private Long paymentId;
    private String type; // creado, aprovado, etc
    private String payload; //detalles tecnicos
    private LocalDateTime created_at;
    private String actor; // usuario que realiza la accion
}
