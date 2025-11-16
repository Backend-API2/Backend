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
        
        String requestURI = request.getRequestURI();
        logger.info("🎯 JWT FILTER EJECUTÁNDOSE para: {}", requestURI);
        
        // Saltar el filtro para endpoints públicos
        if (isPublicEndpoint(requestURI)) {
            logger.trace("🔓 Endpoint público: {} - sin autenticación", requestURI);
            filterChain.doFilter(request, response);
            return;
        }
        
        try {
            String authHeader = request.getHeader("Authorization");
            
            logger.info("🔐 Procesando request autenticado: {}", requestURI);

            if (StringUtils.hasText(authHeader) && authHeader.startsWith("Bearer ")) {
                String jwtToken = authHeader.substring(7);
                
                logger.info("🔑 Validando token JWT para: {}", requestURI);

                if (jwtUtil.isTokenValid(jwtToken)) {
                    String email = jwtUtil.getSubject(jwtToken);
                    
                    logger.info("✅ Token válido, email extraído: {}", email);
                    
                    if (email != null) {
                        try {
                            List<String> roles = jwtUtil.getRoles(jwtToken);
                            
                            logger.info("📋 Roles extraídos del token para {}: {}", email, roles);
                            
                            // Normalizar roles a mayúsculas para asegurar consistencia
                            List<SimpleGrantedAuthority> authorities = roles.stream()
                                    .map(role -> role != null ? role.toUpperCase() : "USER")
                                    .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                                    .collect(Collectors.toList());
                            
                            UsernamePasswordAuthenticationToken authenticationToken = 
                                new UsernamePasswordAuthenticationToken(email, null, authorities);
                            
                            authenticationToken.setDetails(
                                new WebAuthenticationDetailsSource().buildDetails(request)
                            );
                            
                            SecurityContextHolder.getContext().setAuthentication(authenticationToken);
                            
                            logger.info("==================== AUTENTICACIÓN ====================");
                            logger.info("✅ Usuario: {}", email);
                            logger.info("✅ Roles del token (raw): {}", roles);
                            logger.info("✅ Authorities creadas:");
                            authorities.forEach(auth -> {
                                logger.info("   - {}", auth.getAuthority());
                            });
                            logger.info("✅ Request URI: {}", requestURI);
                            logger.info("✅ SecurityContext establecido correctamente");
                            logger.info("✅ Verificando SecurityContext después de establecer:");
                            if (SecurityContextHolder.getContext().getAuthentication() != null) {
                                logger.info("   - Authentication: {}", SecurityContextHolder.getContext().getAuthentication().getName());
                                logger.info("   - Authorities: {}", SecurityContextHolder.getContext().getAuthentication().getAuthorities());
                            } else {
                                logger.error("   - ❌ SecurityContext está NULL después de establecer!");
                            }
                            logger.info("================================================================");

                        } catch (Exception e) {
                            logger.error("❌ Error procesando el token JWT para el usuario: {}, error: {}", 
                                email != null ? email : "unknown", e.getMessage(), e);
                            // Clear any partial authentication
                            SecurityContextHolder.clearContext();
                        }
                    } else {
                        if (email == null) {
                            logger.warn("⚠️ Email es null del token");
                        }
                        if (SecurityContextHolder.getContext().getAuthentication() != null) {
                            logger.info("ℹ️ Ya existe autenticación en contexto");
                        }
                    }
                } else {
                    logger.warn("❌ Token JWT inválido o expirado para: {}", requestURI);
                    // Clear any existing authentication
                    SecurityContextHolder.clearContext();
                }
            } else {
                logger.warn("⚠️ No hay token válido en el header Authorization para: {}", requestURI);
            }
        } catch (Exception e) {
            logger.error("❌ Excepción procesando autenticación para {}: {}", requestURI, e.getMessage(), e);
        }

        filterChain.doFilter(request, response);
    }
    
    private boolean isPublicEndpoint(String requestURI) {
        return requestURI.startsWith("/api/core/") ||
               requestURI.startsWith("/api/data/") ||
               requestURI.equals("/api/auth/register") ||
               requestURI.equals("/api/auth/login") ||
               requestURI.startsWith("/actuator/") ||
               requestURI.startsWith("/health") ||
               requestURI.startsWith("/v3/api-docs/") ||
               requestURI.startsWith("/swagger-ui/") ||
               requestURI.startsWith("/api/payments/webhook/") ||
               requestURI.startsWith("/api/invoices/webhook/") ||
               requestURI.equals("/api/payments/methods") ||
               requestURI.startsWith("/api/providers/subscriptions/")||
               requestURI.equals("/api/core/integration/status");
    }
}