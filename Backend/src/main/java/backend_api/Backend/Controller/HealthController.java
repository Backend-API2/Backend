package backend_api.Backend.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.actuate.health.Status;
import javax.sql.DataSource;
import java.sql.Connection;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/health")
@CrossOrigin(origins = "*")
public class HealthController {

    @Autowired
    private DataSource dataSource;

    @GetMapping("/check")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Verificar conectividad a la base de datos
            boolean dbHealthy = checkDatabaseHealth();
            
            response.put("status", dbHealthy ? "UP" : "DOWN");
            response.put("message", dbHealthy ? "Backend is running successfully!" : "Backend is running but database connection failed");
            response.put("timestamp", LocalDateTime.now());
            response.put("version", "1.0.0");
            response.put("environment", "production");
            response.put("database", dbHealthy ? "connected" : "disconnected");
            response.put("uptime", "running");
            
            HttpStatus status = dbHealthy ? HttpStatus.OK : HttpStatus.SERVICE_UNAVAILABLE;
            return new ResponseEntity<>(response, status);
            
        } catch (Exception e) {
            response.put("status", "DOWN");
            response.put("message", "Health check failed: " + e.getMessage());
            response.put("timestamp", LocalDateTime.now());
            response.put("error", e.getClass().getSimpleName());
            
            return new ResponseEntity<>(response, HttpStatus.SERVICE_UNAVAILABLE);
        }
    }
    
    private boolean checkDatabaseHealth() {
        try (Connection connection = dataSource.getConnection()) {
            return connection.isValid(5); // 5 segundos timeout
        } catch (Exception e) {
            return false;
        }
    }

    @GetMapping("/ping")
    public ResponseEntity<String> ping() {
        return new ResponseEntity<>("pong", HttpStatus.OK);
    }

    @GetMapping("/info")
    public ResponseEntity<Map<String, Object>> getInfo() {
        Map<String, Object> info = new HashMap<>();
        info.put("application", "Backend API");
        info.put("description", "Sistema de gestión de pagos e invoices");
        info.put("status", "operational");
        info.put("uptime", "running");
        info.put("timestamp", LocalDateTime.now());
        
        return new ResponseEntity<>(info, HttpStatus.OK);
    }
}
