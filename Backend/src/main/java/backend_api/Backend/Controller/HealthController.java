package backend_api.Backend.Controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

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
                    value = "UP Lucas"
                )
            )
        )
    })
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("UP Lucas");
    }
}


