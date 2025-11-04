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

    // ID del Prestador en el módulo de Catalogue (dueño del dato)
    @Column(name = "provider_id", unique = true, nullable = false)
    private Long providerId;

    // Snapshot de identidad/contacto
    @Column(name = "name")
    private String name;

    @Column(name = "email")
    private String email;

    @Column(name = "phone")
    private String phone;

    // DNI / CUIT / lo que venga como identificador secundario
    @Column(name = "secondary_id")
    private String secondaryId;

    // Estado del prestador (snapshot). true = activo
    @Column(name = "active")
    private Boolean active = Boolean.TRUE;

    // Foto / avatar
    @Column(name = "photo")
    private String photo;

    // Dirección (snapshot “flat”)
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

    // Timestamps
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