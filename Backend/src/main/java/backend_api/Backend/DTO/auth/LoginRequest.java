package backend_api.Backend.DTO.auth;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class LoginRequest {
    
    @NotBlank(message = "Email es requerido")
    @Email(message = "Email debe tener formato válido")
    private String email;
    
    @NotBlank(message = "Password es requerido")
    @Size(min = 6, message = "Password debe tener al menos 6 caracteres")
    private String password;
}
