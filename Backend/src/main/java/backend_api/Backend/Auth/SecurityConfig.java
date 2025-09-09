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
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import java.util.Arrays;

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
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(Arrays.asList("*"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf((csrf) -> csrf.disable())
                .sessionManagement((session) -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests((authorize) -> authorize
                        // Health check endpoints - públicos
                        .requestMatchers("/api/health/**").permitAll()
                        
                        .requestMatchers("/api/auth/register").permitAll()
                        .requestMatchers("/api/auth/login").permitAll()
                        // Endpoints públicos (sin autenticación)
                        .requestMatchers("/api/payments/webhook/**").permitAll()
                        .requestMatchers("/api/invoices/webhook/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/payments/methods").permitAll()
                        
                        // Payment endpoints - requieren autenticación
                        .requestMatchers(HttpMethod.POST, "/api/payments").hasAnyRole("USER", "MERCHANT")
                        .requestMatchers(HttpMethod.GET, "/api/payments/all").hasAnyRole("USER", "MERCHANT")
                        .requestMatchers(HttpMethod.GET, "/api/payments/{id}").hasAnyRole("USER", "MERCHANT")
                        .requestMatchers(HttpMethod.GET, "/api/payments/user/{userId}").hasAnyRole("USER", "MERCHANT")
                        .requestMatchers(HttpMethod.GET, "/api/payments/provider/{providerId}").hasAnyRole("MERCHANT")
                        .requestMatchers(HttpMethod.GET, "/api/payments/status/{status}").hasAnyRole("MERCHANT")
                        .requestMatchers(HttpMethod.GET, "/api/payments/gateway/{gatewayTxnId}").hasAnyRole("MERCHANT")
                        .requestMatchers(HttpMethod.GET, "/api/payments/solicitud/{solicitudId}").hasAnyRole("USER", "MERCHANT")
                        .requestMatchers(HttpMethod.GET, "/api/payments/amount/{minAmount}").hasAnyRole("MERCHANT")
                        .requestMatchers(HttpMethod.GET, "/api/payments/date-range").hasAnyRole("MERCHANT")
                        .requestMatchers(HttpMethod.GET, "/api/payments/user/{userId}/status/{status}").hasAnyRole("USER", "MERCHANT")
                        .requestMatchers(HttpMethod.GET, "/api/payments/currency/{currency}").hasAnyRole("MERCHANT")
                        .requestMatchers(HttpMethod.GET, "/api/payments/{id}/exists").hasAnyRole("USER", "MERCHANT")
                        .requestMatchers(HttpMethod.GET, "/api/payments/{id}/timeline").hasAnyRole("USER", "MERCHANT")
                        
                        // Payment intents and confirmation
                        .requestMatchers(HttpMethod.POST, "/api/payments/{id}/confirm").hasAnyRole("USER", "MERCHANT")
                        .requestMatchers(HttpMethod.POST, "/api/payments/{id}/cancel").hasAnyRole("USER", "MERCHANT")
                        .requestMatchers(HttpMethod.GET, "/api/payments/{id}/attempts").hasAnyRole("MERCHANT")
                        .requestMatchers(HttpMethod.POST, "/api/payments/{id}/retry").hasAnyRole("MERCHANT")

                        // Payment filtering and pagination endpoints
                        .requestMatchers(HttpMethod.POST, "/api/payments/search").hasAnyRole("USER", "MERCHANT")
                        .requestMatchers(HttpMethod.POST, "/api/payments/search/user").hasAnyRole("MERCHANT")
                        .requestMatchers(HttpMethod.POST, "/api/payments/search/amount").hasAnyRole("MERCHANT")
                        
                        .requestMatchers(HttpMethod.GET, "/api/payments/my-payments").hasAnyRole("USER", "MERCHANT")
                        .requestMatchers(HttpMethod.GET, "/api/payments/my-payments/status/*").hasAnyRole("USER", "MERCHANT")
                        .requestMatchers(HttpMethod.GET, "/api/payments/my-total").hasAnyRole("USER", "MERCHANT")
                        .requestMatchers(HttpMethod.POST, "/api/payments/my-search").hasAnyRole("USER", "MERCHANT")

                        // Invoice endpoints - según rol (cuando se implementen)
                        .requestMatchers(HttpMethod.POST, "/api/invoices").hasAnyRole("MERCHANT")
                        .requestMatchers(HttpMethod.GET, "/api/invoices/{id}").hasAnyRole("USER", "MERCHANT")
                        .requestMatchers(HttpMethod.PUT, "/api/invoices/{id}").hasAnyRole("MERCHANT")
                        .requestMatchers(HttpMethod.GET, "/api/invoices").hasAnyRole("MERCHANT")
                        
                        // ✅ NUEVOS ENDPOINTS SEGUROS DE FACTURAS CON TOKEN
                        .requestMatchers(HttpMethod.GET, "/api/invoices/my-invoices").hasAnyRole("USER", "MERCHANT")
                        .requestMatchers(HttpMethod.GET, "/api/invoices/my-summary").hasAnyRole("USER", "MERCHANT")
                        
                        // Refund endpoints - restringidos (cuando se implementen)
                        .requestMatchers(HttpMethod.POST, "/api/refunds").hasAnyRole("MERCHANT")
                        .requestMatchers(HttpMethod.GET, "/api/refunds/{id}").hasAnyRole("MERCHANT")

                    

                
                        
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .httpBasic((httpBasic) -> httpBasic.disable());

        return http.build();
    }
}