package backend_api.Backend.Entity.payment;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import lombok.Data;
import java.time.LocalDateTime;

@Table(name = "payment_attempts")
@Data
@Entity
public class PaymentAttempt {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long payment_id;
    private Integer attempt_number; //numero de intento
    private PaymentStatus status;
    private String response_code; //resultado del gateway
    private String gateway_response_code;
    private LocalDateTime created_at;
    
}
