package backend_api.Backend.Entity.payment;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;


@Table(name = "payment_attempts")
@Data
@Entity
public class PaymentAttempt {
    private Long id;
    private Long payment_id;
    private Integer attempt_number; //numero de intento
    private PaymentStatus status;
    private String response_code; //resultado del gateway
    private String gateway_response_code;
    private LocalDateTime created_at;
    
}
