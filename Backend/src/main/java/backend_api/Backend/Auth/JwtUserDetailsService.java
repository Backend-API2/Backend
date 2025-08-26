package backend_api.Backend.Auth;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

public class JwtUserDetailsService implements UserDetailsService {

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        // Como esto es el modulo de pago y facturacion,el UserDetailsService lo vamos a usar solo para validar el token JWT
        // Los datos del usuario vienen del token, no de una base de datos local
        // Este método se llama desde JwtAuthFilter después de validar el token
        
        // Crear un UserDetails básico con el email del token
        // Los roles/authorities se extraerán del token JWT en el futuro
        return User.builder()
        .username(email)
        .password("")
        .authorities("ROLE_USER")
        .accountExpired(false)
        .accountLocked(false)
        .credentialsExpired(false)
        .disabled(false)
        .build();
    }
}
