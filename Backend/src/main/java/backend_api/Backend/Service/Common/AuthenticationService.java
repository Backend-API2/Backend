package backend_api.Backend.Service.Common;

import backend_api.Backend.Auth.JwtUtil;
import backend_api.Backend.Entity.user.User;
import backend_api.Backend.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;

    public User getUserFromToken(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new SecurityException("Token de autenticación inválido o ausente");
        }

        String token = authHeader.replace("Bearer ", "");
        String email = jwtUtil.getSubject(token);

        if (email == null) {
            throw new SecurityException("Token de autenticación inválido");
        }

        return userRepository.findByEmail(email)
                .orElseThrow(() -> new SecurityException("Usuario no encontrado para el token proporcionado"));

    }

    public Optional<User> getUserFromTokenOptional(String authHeader) {
        try {
            return Optional.of(getUserFromToken(authHeader));
        } catch (SecurityException e) {
            return Optional.empty();
        }
    }

    public boolean isValidToken(String authHeader) {
        return getUserFromTokenOptional(authHeader).isPresent();
    }

    public boolean hasRole(String authHeader, String role) {
       return getUserFromTokenOptional(authHeader)
               .map(user -> user.getRole().name().equals(role))
               .orElse(false);
    }

    public boolean isMerchant(String authHeader) {
        return hasRole(authHeader, "MERCHANT");
    }

    public boolean isUser(String authHeader) {
        return hasRole(authHeader, "USER");
    }
}