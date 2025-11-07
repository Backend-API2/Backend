package backend_api.Backend.Auth;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import java.util.Arrays;

@Configuration
@EnableWebSecurity
public class SecurityConfig {



    private static final String[] SWAGGER_WHITELIST = {
            "/v3/api-docs/**",
            "/swagger-ui/**",
            "/swagger-ui.html"
    };

    private static final String ROLE_USER = "USER";
    private static final String ROLE_MERCHANT = "MERCHANT";
    private static final String ROLE_ADMIN = "ADMIN";


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
    @Order(0)
    public SecurityFilterChain swaggerSecurityFilterChain(HttpSecurity http) throws Exception {
        http
            .securityMatcher("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html")
            .csrf(AbstractHttpConfigurer::disable)
            .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());

        return http.build();
    }


    @Bean
    @Order(2)
    public SecurityFilterChain actuatorSecurityFilterChain(HttpSecurity http) throws Exception {
        http
            .securityMatcher("/actuator/**")
            .csrf(AbstractHttpConfigurer::disable)
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/actuator/health").permitAll()
                .anyRequest().permitAll()); // Permitir acceso a todos los endpoints de actuator temporalmente

        return http.build();
    }

    @Bean
    @Order(3)
    public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtAuthFilter jwtAuthFilter) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf((csrf) -> csrf.disable())
                .sessionManagement((session) -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests((authorize) -> authorize

                        .requestMatchers(SWAGGER_WHITELIST).permitAll()

                        .requestMatchers("/api/auth/register").permitAll()
                        .requestMatchers("/api/auth/login").permitAll()
                        .requestMatchers("/api/auth/profile").hasAnyRole(ROLE_USER, ROLE_MERCHANT, ROLE_ADMIN)
                        // Asegurar acceso público a Actuator también en la cadena principal
                        .requestMatchers("/actuator/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/health").permitAll()
                        // Endpoints de prueba para eventos (solo para desarrollo)
                        .requestMatchers("/api/test/events/**").permitAll()
                        // CORE HUB Integration endpoints - públicos
                        .requestMatchers("/api/core/**").permitAll()
                        .requestMatchers("/api/rabbitmq/**").permitAll()
                        // Data subscription endpoints - públicos
                        .requestMatchers("/api/data/**").permitAll()
                        // Webhook endpoints - públicos
                        .requestMatchers("/api/core/webhook/**").permitAll()
                        // User subscription endpoints - públicos
                        .requestMatchers("/api/users/subscriptions/**").permitAll()
                        // Endpoints públicos (sin autenticación)
                        .requestMatchers("/api/payments/webhook/**").permitAll()
                        .requestMatchers("/api/invoices/webhook/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/payments/methods").permitAll()
                        .requestMatchers("/api/providers/subscriptions/**").permitAll()
                        
                        // Payment endpoints - requieren autenticación
                        // IMPORTANTE: Los patrones más específicos deben ir ANTES de los genéricos
                        // Endpoints "my-*" deben ir antes de los patrones con {id}
                        .requestMatchers(HttpMethod.GET, "/api/payments/my-payments").hasAnyRole(ROLE_USER, ROLE_MERCHANT, ROLE_ADMIN)
                        .requestMatchers(HttpMethod.GET, "/api/payments/my-payments/status/*").hasAnyRole(ROLE_USER, ROLE_MERCHANT, ROLE_ADMIN)
                        .requestMatchers(HttpMethod.GET, "/api/payments/my-total").hasAnyRole(ROLE_USER, ROLE_MERCHANT, ROLE_ADMIN)
                        .requestMatchers(HttpMethod.POST, "/api/payments/my-search").hasAnyRole(ROLE_USER, ROLE_MERCHANT, ROLE_ADMIN)
                        
                        // Payment filtering and pagination endpoints
                        .requestMatchers(HttpMethod.POST, "/api/payments/search").hasAnyRole(ROLE_USER, ROLE_MERCHANT)
                        .requestMatchers(HttpMethod.POST, "/api/payments/search/user").hasAnyRole(ROLE_MERCHANT)
                        .requestMatchers(HttpMethod.POST, "/api/payments/search/amount").hasAnyRole(ROLE_MERCHANT)
                        
                        // Payment intents and confirmation (patrones específicos con /{id}/)
                        .requestMatchers(HttpMethod.POST, "/api/payments/{id}/confirm").hasAnyRole(ROLE_USER, ROLE_MERCHANT)
                        .requestMatchers(HttpMethod.POST, "/api/payments/{id}/cancel").hasAnyRole(ROLE_USER, ROLE_MERCHANT)
                        .requestMatchers(HttpMethod.GET, "/api/payments/{id}/attempts").hasAnyRole(ROLE_MERCHANT)
                        .requestMatchers(HttpMethod.POST, "/api/payments/{id}/retry-balance").hasAnyRole(ROLE_USER)
                        .requestMatchers(HttpMethod.GET, "/api/payments/{id}/exists").hasAnyRole(ROLE_USER, ROLE_MERCHANT, ROLE_ADMIN)
                        .requestMatchers(HttpMethod.GET, "/api/payments/{id}/timeline").hasAnyRole(ROLE_USER, ROLE_MERCHANT, ROLE_ADMIN)
                        
                        // Otros endpoints específicos
                        .requestMatchers(HttpMethod.POST, "/api/payments").hasAnyRole(ROLE_USER, ROLE_MERCHANT)
                        .requestMatchers(HttpMethod.GET, "/api/payments/all").hasAnyRole(ROLE_USER, ROLE_MERCHANT, ROLE_ADMIN)
                        .requestMatchers(HttpMethod.GET, "/api/payments/user/{userId}").hasAnyRole(ROLE_USER, ROLE_MERCHANT)
                        .requestMatchers(HttpMethod.GET, "/api/payments/provider/{providerId}").hasAnyRole(ROLE_MERCHANT)
                        .requestMatchers(HttpMethod.GET, "/api/payments/status/{status}").hasAnyRole(ROLE_MERCHANT)
                        .requestMatchers(HttpMethod.GET, "/api/payments/gateway/{gatewayTxnId}").hasAnyRole(ROLE_MERCHANT)
                        .requestMatchers(HttpMethod.GET, "/api/payments/solicitud/{solicitudId}").hasAnyRole(ROLE_USER, ROLE_MERCHANT)
                        .requestMatchers(HttpMethod.GET, "/api/payments/amount/{minAmount}").hasAnyRole(ROLE_MERCHANT)
                        .requestMatchers(HttpMethod.GET, "/api/payments/date-range").hasAnyRole(ROLE_MERCHANT)
                        .requestMatchers(HttpMethod.GET, "/api/payments/user/{userId}/status/{status}").hasAnyRole(ROLE_USER, ROLE_MERCHANT)
                        .requestMatchers(HttpMethod.GET, "/api/payments/currency/{currency}").hasAnyRole(ROLE_MERCHANT)
                        
                        // Patrón genérico {id} debe ir AL FINAL - ADMIN puede ver cualquier pago
                        .requestMatchers(HttpMethod.GET, "/api/payments/{id}").hasAnyRole(ROLE_USER, ROLE_MERCHANT, ROLE_ADMIN)

                        // Invoice endpoints - según rol (cuando se implementen)
                        .requestMatchers(HttpMethod.POST, "/api/invoices").hasAnyRole(ROLE_MERCHANT)
                        .requestMatchers(HttpMethod.GET, "/api/invoices/{id}").hasAnyRole(ROLE_USER, ROLE_MERCHANT)
                        .requestMatchers(HttpMethod.PUT, "/api/invoices/{id}").hasAnyRole(ROLE_MERCHANT)
                        .requestMatchers(HttpMethod.GET, "/api/invoices").hasAnyRole(ROLE_MERCHANT)

                        // ✅ NUEVOS ENDPOINTS SEGUROS DE FACTURAS CON TOKEN
                        .requestMatchers(HttpMethod.GET, "/api/invoices/my-invoices").hasAnyRole(ROLE_USER, ROLE_MERCHANT)
                        .requestMatchers(HttpMethod.GET, "/api/invoices/my-summary").hasAnyRole(ROLE_USER, ROLE_MERCHANT)


                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .exceptionHandling((exceptions) -> exceptions
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(SecurityConfig.class);
                            logger.error("❌ Access Denied para: {}", request.getRequestURI());
                            logger.error("❌ Usuario autenticado: {}", 
                                org.springframework.security.core.context.SecurityContextHolder.getContext()
                                    .getAuthentication() != null ? 
                                    org.springframework.security.core.context.SecurityContextHolder.getContext()
                                        .getAuthentication().getName() : "NO AUTENTICADO");
                            if (org.springframework.security.core.context.SecurityContextHolder.getContext()
                                    .getAuthentication() != null) {
                                logger.error("❌ Authorities: {}", 
                                    org.springframework.security.core.context.SecurityContextHolder.getContext()
                                        .getAuthentication().getAuthorities());
                            }
                            logger.error("❌ Error: {}", accessDeniedException.getMessage());
                            response.setStatus(HttpStatus.FORBIDDEN.value());
                            response.setContentType("application/json");
                            response.getWriter().write("{\"error\":\"Forbidden\",\"message\":\"" + 
                                accessDeniedException.getMessage() + "\"}");
                        })
                )
                .httpBasic((httpBasic) -> httpBasic.disable());

        return http.build();
    }
}