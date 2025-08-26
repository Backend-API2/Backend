package backend_api.Backend.Auth;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private JwtAuthFilter jwtAuthFilter;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf((csrf) -> csrf.disable())
                .sessionManagement((session) -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests((authorize) -> authorize
                        //para proveedores de pago
                        .requestMatchers("/api/payments/webhook/**").permitAll()
                        .requestMatchers("/api/invoices/webhook/**").permitAll()
                        
                        // públicos de información
                        .requestMatchers(HttpMethod.GET, "/api/payments/methods").permitAll()
                                                
                        // Payment endpoints - requieren autenticación
                        .requestMatchers(HttpMethod.POST, "/api/payments/create").hasAnyRole("USER", "MERCHANT", "ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/payments/{id}").hasAnyRole("USER", "MERCHANT", "ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/payments/{id}/cancel").hasAnyRole("USER", "MERCHANT", "ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/payments/history").hasAnyRole("USER", "MERCHANT", "ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/payments").hasAnyRole("MERCHANT", "ADMIN")
                        
                        // Invoice endpoints - según rol
                        .requestMatchers(HttpMethod.POST, "/api/invoices/create").hasAnyRole("MERCHANT", "ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/invoices/{id}").hasAnyRole("USER", "MERCHANT", "ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/invoices/{id}").hasAnyRole("MERCHANT", "ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/invoices").hasAnyRole("MERCHANT", "ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/invoices/{id}").hasRole("ADMIN")
                        
                        // Refund endpoints - restringidos
                        .requestMatchers(HttpMethod.POST, "/api/refunds/create").hasAnyRole("MERCHANT", "ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/refunds/{id}").hasAnyRole("MERCHANT", "ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/refunds").hasRole("ADMIN")
                        
                        // Dispute endpoints - solo admin
                        .requestMatchers("/api/disputes/**").hasRole("ADMIN")
                        
                        // Reconciliation endpoints - solo admin
                        .requestMatchers("/api/reconciliation/**").hasRole("ADMIN")
                        
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .httpBasic((httpBasic) -> httpBasic.disable());

        return http.build();
    }
}