package backend_api.Backend.DTO.auth;

import lombok.Data;

@Data
public class AuthResponse {
    
    private String token;
    private String type = "Bearer";
    private Long userId;
    private String email;
    private String name;
    private String role;
    
    public AuthResponse(String token, Long userId, String email, String name, String role) {
        this.token = token;
        this.userId = userId;
        this.email = email;
        this.name = name;
        this.role = role;
    }
}
