package backend_api.Backend.Service.Common;

import backend_api.Backend.Auth.JwtUtil;
import backend_api.Backend.Entity.user.User;
import backend_api.Backend.Entity.user.UserRole;
import backend_api.Backend.Entity.UserData;
import backend_api.Backend.Entity.ProviderData;
import backend_api.Backend.Repository.UserRepository;
import backend_api.Backend.Repository.UserDataRepository;
import backend_api.Backend.Repository.ProviderDataRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    private final UserDataRepository userDataRepository;
    private final ProviderDataRepository providerDataRepository;

    public User getUserFromToken(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new SecurityException("Token de autenticación inválido o ausente");
        }

        String token = authHeader.replace("Bearer ", "");
        String email = jwtUtil.getSubject(token);

        if (email == null) {
            throw new SecurityException("Token de autenticación inválido");
        }

        // Buscar primero en UserData (datos sincronizados)
        Optional<UserData> userDataOpt = userDataRepository.findByEmail(email);
        if (userDataOpt.isPresent()) {
            UserData userData = userDataOpt.get();
            // Convertir UserData a User para mantener compatibilidad
            User user = new User();
            user.setId(userData.getUserId());
            user.setEmail(userData.getEmail());
            user.setName(userData.getName());
            user.setPhone(userData.getPhone());
            // Mapear el rol desde UserData
            user.setRole(convertRoleFromUserData(userData.getRole()));
            return user;
        }

        // Buscar en ProviderData (prestadores sincronizados)
        Optional<ProviderData> providerDataOpt = providerDataRepository.findByEmail(email);
        if (providerDataOpt.isPresent()) {
            ProviderData providerData = providerDataOpt.get();
            // Convertir ProviderData a User para mantener compatibilidad
            User user = new User();
            user.setId(providerData.getProviderId());
            user.setEmail(providerData.getEmail());
            user.setName(providerData.getName());
            user.setPhone(providerData.getPhone());
            user.setRole(UserRole.MERCHANT);
            return user;
        }

        // Si no está en UserData ni ProviderData, buscar en User (tabla local)
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new SecurityException("Usuario no encontrado para el token proporcionado"));
    }

    private UserRole convertRoleFromUserData(String userDataRole) {
        if (userDataRole == null) {
            return UserRole.USER;
        }
        
        switch (userDataRole.toUpperCase()) {
            case "CLIENTE":
                return UserRole.USER;
            case "PRESTADOR":
                return UserRole.MERCHANT;
            case "ADMIN":
                return UserRole.USER; // Mapear ADMIN a USER por ahora
            default:
                return UserRole.USER;
        }
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