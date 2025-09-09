package backend_api.Backend.Controller;

import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/health")
@CrossOrigin(origins = "*")
public class HealthController {

    @GetMapping("/check")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("message", "Backend is running successfully!");
        response.put("timestamp", LocalDateTime.now());
        response.put("version", "1.0.0");
        response.put("environment", "production");
        
        return new ResponseEntity<>(response, HttpStatus.OK);
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
