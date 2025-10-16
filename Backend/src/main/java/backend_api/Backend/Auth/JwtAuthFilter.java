package backend_api.Backend.Auth;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthFilter.class);

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        // Saltar el filtro para endpoints públicos
        String requestURI = request.getRequestURI();
        if (isPublicEndpoint(requestURI)) {
            filterChain.doFilter(request, response);
            return;
        }
        
        try {
            String authHeader = request.getHeader("Authorization");

            if (StringUtils.hasText(authHeader) && authHeader.startsWith("Bearer ")) {
                String jwtToken = authHeader.substring(7);

                if (jwtUtil.isTokenValid(jwtToken)) {
                    String email = jwtUtil.getSubject(jwtToken);
                    
                    if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                        try {
                            List<String> roles = jwtUtil.getRoles(jwtToken);
                            
                            List<SimpleGrantedAuthority> authorities = roles.stream()
                                    .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                                    .collect(Collectors.toList());
                            
                            UsernamePasswordAuthenticationToken authenticationToken = 
                                new UsernamePasswordAuthenticationToken(email, null, authorities);
                            
                            authenticationToken.setDetails(
                                new WebAuthenticationDetailsSource().buildDetails(request)
                            );
                            
                            SecurityContextHolder.getContext().setAuthentication(authenticationToken);

                            logger.debug("Autenticacion exitosa para el usuario: {} con roles: {}", email, roles);

                        } catch (Exception e) {
                            logger.warn("Error procesando el token JWT para el usuario: {}, error: {}", email, e.getMessage());
                            // Clear any partial authentication
                            SecurityContextHolder.clearContext();
                        }
                    }
                } else {
                    logger.debug("Token JWT inválido o expirado");
                    // Clear any existing authentication
                    SecurityContextHolder.clearContext();
                }
            }
        } catch (Exception e) {
            logger.error("No se puede establecer la autenticación del usuario: {}", e.getMessage());
        }

        filterChain.doFilter(request, response);
    }
    
    private boolean isPublicEndpoint(String requestURI) {
        return requestURI.startsWith("/api/core/") ||
               requestURI.startsWith("/api/data/") ||
               requestURI.startsWith("/api/auth/") ||
               requestURI.startsWith("/actuator/") ||
               requestURI.startsWith("/health") ||
               requestURI.startsWith("/v3/api-docs/") ||
               requestURI.startsWith("/swagger-ui/") ||
               requestURI.startsWith("/api/payments/webhook/") ||
               requestURI.startsWith("/api/invoices/webhook/") ||
               requestURI.equals("/api/payments/methods");
    }
}