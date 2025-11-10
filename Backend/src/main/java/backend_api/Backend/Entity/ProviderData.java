package backend_api.Backend.Entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "provider_data")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProviderData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "provider_id", unique = true, nullable = false)
    private Long providerId;

    // NUEVO: nombres separados (sin remover name)
    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    // Se conserva el snapshot completo
    @Column(name = "name")
    private String name;

    @Column(name = "email")
    private String email;

    @Column(name = "phone")
    private String phone;

    @Column(name = "secondary_id")
    private String secondaryId;

    @Column(name = "active")
    private Boolean active = Boolean.TRUE;

    @Column(name = "photo")
    private String photo;

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

    @ElementCollection
    @CollectionTable(
            name = "provider_data_zones",
            joinColumns = @JoinColumn(name = "provider_data_id")
    )
    @Column(name = "zone")
    private List<String> zones = new ArrayList<>();

    @ElementCollection
    @CollectionTable(
            name = "provider_data_skills",
            joinColumns = @JoinColumn(name = "provider_data_id")
    )
    @Column(name = "skill")
    private List<String> skills = new ArrayList<>();

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}