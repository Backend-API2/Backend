package backend_api.Backend.Entity.payment.types;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Entity
@Table(name = "test_cards")
public class TestCard {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // VISA, MASTERCARD, AMERICAN_EXPRESS, etc.
    @Column(nullable = false, length = 20)
    private String brand;

    // Máscara para debugging/logs: 411111******1111
    @Column(name = "pan_masked", nullable = false, length = 25)
    private String panMasked;

    @Column(name = "pan_sha256", nullable = false, length = 64, unique = true)
    private String panSha256;

    @Column(name = "cvv_sha256", nullable = false, length = 64)
    private String cvvSha256;

    @Column(name = "bin3", nullable = false, length = 3)
    private String bin3;

    @Column(name = "last4", nullable = false, length = 4)
    private String last4;

    @Column(nullable = false)
    private Boolean isActive = true;

    // ✅ Saldo local para pruebas
    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal balance = BigDecimal.ZERO;

    @Column(nullable = false, length = 3)
    private String currency = "ARS";
}