package backend_api.Backend.DTO.auth;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class RegisterRequest {
    
    @NotBlank(message = "Email es requerido")
    @Email(message = "Email debe tener formato válido")
    private String email;
    
    @NotBlank(message = "Password es requerido")
    @Size(min = 6, message = "Password debe tener al menos 6 caracteres")
    private String password;
    
    @NotBlank(message = "Nombre es requerido")
    @Size(min = 2, max = 100, message = "Nombre debe tener entre 2 y 100 caracteres")
    private String name;
    
    @Pattern(regexp = "^[+]?[0-9]{10,15}$", message = "Teléfono debe ser válido")
    private String phone;
    
    // Por defecto será USER, pero puede especificar MERCHANT
    private String role = "USER";
}
