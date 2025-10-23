package backend_api.Backend.Controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

@RestController
@Tag(name = "Health Check", description = "Endpoint para verificar el estado del servicio")
public class HealthController {

    @Operation(
        summary = "Verificar estado del servicio",
        description = "Endpoint simple para verificar que el servicio está funcionando correctamente"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Servicio funcionando correctamente",
            content = @io.swagger.v3.oas.annotations.media.Content(
                mediaType = "text/plain",
                examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
                    name = "Servicio activo",
                    value = "UP"
                )
            )
        )
    })
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("UP");
    }

    @Operation(
        summary = "Ver logs de la aplicación",
        description = "Endpoint para ver los logs recientes de la aplicación"
    )
    @GetMapping("/logs")
    public ResponseEntity<String> logs() {
        try {
            // Intentar leer el archivo de logs
            List<String> logLines = Files.readAllLines(Paths.get("/app/logs/application.log"));
            
            // Tomar las últimas 50 líneas
            int startIndex = Math.max(0, logLines.size() - 50);
            List<String> recentLogs = logLines.subList(startIndex, logLines.size());
            
            String logs = String.join("\n", recentLogs);
            return ResponseEntity.ok("=== ÚLTIMOS LOGS ===\n" + logs);
            
        } catch (IOException e) {
            return ResponseEntity.ok("Error leyendo logs: " + e.getMessage() + 
                "\n\n=== LOGS DE CONSOLA ===\n" +
                "Los logs están en la consola del contenedor Docker");
        }
    }
}


