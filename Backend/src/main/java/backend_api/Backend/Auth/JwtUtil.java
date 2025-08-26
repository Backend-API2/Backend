package backend_api.Backend.Auth;

import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;

@Component
public class JwtUtil {

    private static final Logger logger = LoggerFactory.getLogger(JwtUtil.class);

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}") 
    private long expirationTime;

    @Value("${jwt.issuer:payment-api}")
    private String issuer;

    public String generateToken(String subject) {
        return generateToken(subject, expirationTime, List.of("USER"));
    }

    public String generateToken(String subject, long duration) {
        return generateToken(subject, duration, List.of("USER"));
    }

    public String generateToken(String subject, long duration, List<String> roles) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(secret);
            return JWT.create()
                    .withSubject(subject)
                    .withIssuer(issuer)
                    .withIssuedAt(new Date())
                    .withExpiresAt(new Date(System.currentTimeMillis() + duration))
                    .withClaim("roles", roles)
                    .withClaim("type", "access")
                    .sign(algorithm);
        } catch (JWTCreationException e) {
            logger.error("Error creating JWT token: {}", e.getMessage());
            throw new RuntimeException("Error creating JWT token", e);
        }
    }

    public String getSubject(String token) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(secret);
            DecodedJWT jwt = JWT.require(algorithm)
                    .withIssuer(issuer)
                    .build()
                    .verify(token);
            return jwt.getSubject();
        } catch (JWTVerificationException e) {
            logger.warn("Invalid JWT token: {}", e.getMessage());
            return null;
        }
    }

    public List<String> getRoles(String token) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(secret);
            DecodedJWT jwt = JWT.require(algorithm)
                    .withIssuer(issuer)
                    .build()
                    .verify(token);
            return jwt.getClaim("roles").asList(String.class);
        } catch (JWTVerificationException e) {
            logger.warn("Cannot extract roles from token: {}", e.getMessage());
            return List.of("USER"); // Rol por defecto
        }
    }

    public boolean isTokenValid(String token) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(secret);
            JWT.require(algorithm)
                    .withIssuer(issuer)
                    .build()
                    .verify(token);
            return true;
        } catch (JWTVerificationException e) {
            logger.debug("JWT token validation failed: {}", e.getMessage());
            return false;
        }
    }

    public boolean isTokenExpired(String token) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(secret);
            DecodedJWT jwt = JWT.require(algorithm)
                    .withIssuer(issuer)
                    .build()
                    .verify(token);
            return jwt.getExpiresAt().before(new Date());
        } catch (JWTVerificationException e) {
            return true;
        }
    }

    public Date getExpirationDate(String token) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(secret);
            DecodedJWT jwt = JWT.require(algorithm)
                    .withIssuer(issuer)
                    .build()
                    .verify(token);
            return jwt.getExpiresAt();
        } catch (JWTVerificationException e) {
            logger.warn("Cannot get expiration date from token: {}", e.getMessage());
            return null;
        }
    }
}