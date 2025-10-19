package backend_api.Backend.messaging.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ProviderCreatedMessage extends BaseMessage {
    private Long id;                 // providerId
    private String nombre;
    private String apellido;
    private String email;
    private String telefono;
    private String dni;
    private Integer activo;          // 1/0
    private String foto;

    // Dirección
    private String estado;
    private String ciudad;
    private String calle;
    private String numero;
    private String piso;
    private String departamento;

    private List<Long> habilidades;
    private List<Long> zonas;
}