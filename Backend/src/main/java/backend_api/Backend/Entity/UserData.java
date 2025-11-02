package backend_api.Backend.Entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "user_data")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserData {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "user_id", unique = true, nullable = false)
    private Long userId;
    
    @Column(name = "name")
    private String name;
    
    @Column(name = "first_name")
    private String firstName;
    
    @Column(name = "last_name")
    private String lastName;
    
    @Column(name = "email")
    private String email;
    
    @Column(name = "phone")
    private String phone;
    
    @Column(name = "secondary_id")
    private String secondaryId;
    
    @Column(name = "dni")
    private String dni;
    
    @Column(name = "role")
    private String role;
    
    @Column(name = "active")
    private Boolean active = Boolean.TRUE;
    
    // Dirección
    @Column(name = "state")
    private String state;
    
    @Column(name = "city")
    private String city;
    
    @Column(name = "street")
    private String street;
    
    @Column(name = "number")
    private String number;
    
    @Column(name = "floor")
    private String floor;
    
    @Column(name = "apartment")
    private String apartment;
    
    // Zonas y habilidades
    @ElementCollection
    @CollectionTable(name = "user_zones", joinColumns = @JoinColumn(name = "user_fk"))
    @Column(name = "zone")
    private List<String> zones = new ArrayList<>();
    
    @ElementCollection
    @CollectionTable(name = "user_skills", joinColumns = @JoinColumn(name = "user_fk"))
    @Column(name = "skill")
    private List<String> skills = new ArrayList<>();
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @Column(name = "saldo_disponible", precision = 10, scale = 2)
    private BigDecimal saldoDisponible;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (active == null) {
            active = Boolean.TRUE;
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
