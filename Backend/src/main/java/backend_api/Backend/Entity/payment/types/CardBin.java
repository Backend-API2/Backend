package backend_api.Backend.Entity.payment.types;

import jakarta.persistence.*;
import lombok.Data;

@Table(name = "card_bins")
@Data
@Entity
public class CardBin {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(length = 3, nullable = false, unique = true)
    private String bin;
    
    @Column(nullable = false)
    private String bankName; 
    
    @Column(nullable = false) 
    private String cardType; 
    
    @Column(nullable = false)
    private Boolean isActive; 
    
    public CardBin() {
        this.isActive = true;
    }
    
    public CardBin(String bin, String bankName, String cardType) {
        this.bin = bin;
        this.bankName = bankName;
        this.cardType = cardType;
        this.isActive = true;
    }
}
