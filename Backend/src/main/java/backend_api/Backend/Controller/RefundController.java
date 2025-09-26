// backend_api/Backend/Controller/RefundController.java
package backend_api.Backend.Controller;

import backend_api.Backend.DTO.refund.CreateRefundRequest;
import backend_api.Backend.DTO.refund.RefundResponse;
import backend_api.Backend.DTO.refund.UpdateRefundStatusRequest;
import backend_api.Backend.Entity.refund.Refund;
import backend_api.Backend.Entity.refund.RefundStatus;
import backend_api.Backend.Service.Interface.RefundService;
import backend_api.Backend.Auth.JwtUtil;
import backend_api.Backend.Repository.UserRepository;
import backend_api.Backend.Entity.user.User;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/refunds")
@CrossOrigin(origins = "*")
@Tag(name = "Reembolsos", description = "Endpoints para gestión de reembolsos, incluyendo creación, consulta y actualización de estado")
public class RefundController {

    @Autowired private RefundService refundService;
    @Autowired private JwtUtil jwtUtil;
    @Autowired private UserRepository userRepository;

    @Operation(
        summary = "Crear solicitud de reembolso",
        description = "Crea una nueva solicitud de reembolso. Solo usuarios autenticados pueden crear reembolsos."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201",
            description = "Reembolso creado exitosamente",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = RefundResponse.class),
                examples = @ExampleObject(
                    name = "Reembolso creado",
                    value = "{\"id\": 1, \"amount\": 100.00, \"reason\": \"Producto defectuoso\", \"status\": \"PENDING\"}"
                )
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Datos de entrada inválidos",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "Error de validación",
                    value = "{\"message\": \"El monto debe ser mayor a 0\"}"
                )
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Pago no encontrado",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "Pago no encontrado",
                    value = "{\"message\": \"Pago no encontrado\"}"
                )
            )
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Error interno del servidor",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "Error interno",
                    value = "{\"message\": \"Error interno\"}"
                )
            )
        )
    })
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/create")
    public ResponseEntity<?> createRefund(
            @Valid @RequestBody CreateRefundRequest request,
            @Parameter(description = "Token JWT de autenticación", required = true)
            @RequestHeader("Authorization") String authHeader) {
        try {
            String email = jwtUtil.getSubject(authHeader.replace("Bearer ", ""));
            User user = userRepository.findByEmail(email).orElseThrow();
            Refund created = refundService.createRefund(request, user.getId());
            return new ResponseEntity<>(RefundResponse.fromEntity(created), HttpStatus.CREATED);
        } catch (IllegalArgumentException | IllegalStateException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>("Error interno", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // MERCHANT aprueba (ejecuta y finaliza parcial/total)
    @PostMapping("/{id}/approve")
    public ResponseEntity<?> approveRefund(
            @PathVariable Long id,
            @RequestHeader("Authorization") String authHeader,
            @RequestBody(required = false) UpdateRefundStatusRequest body) {
        try {
            String email = jwtUtil.getSubject(authHeader.replace("Bearer ", ""));
            User merchant = userRepository.findByEmail(email).orElseThrow();
            Refund updated = refundService.approveRefund(id, merchant.getId(), body != null ? body.getMessage() : null);
            return new ResponseEntity<>(RefundResponse.fromEntity(updated), HttpStatus.OK);
        } catch (IllegalArgumentException | IllegalStateException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>("Error interno", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // MERCHANT rechaza
    @PostMapping("/{id}/decline")
    public ResponseEntity<?> declineRefund(
            @PathVariable Long id,
            @RequestHeader("Authorization") String authHeader,
            @Valid @RequestBody UpdateRefundStatusRequest body) {
        try {
            String email = jwtUtil.getSubject(authHeader.replace("Bearer ", ""));
            User merchant = userRepository.findByEmail(email).orElseThrow();
            Refund updated = refundService.declineRefund(id, merchant.getId(), body.getMessage());
            return new ResponseEntity<>(RefundResponse.fromEntity(updated), HttpStatus.OK);
        } catch (IllegalArgumentException | IllegalStateException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>("Error interno", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Los GET que ya tenías:
    @GetMapping("/{id}")
    public ResponseEntity<?> getRefundById(@PathVariable Long id) { /* igual que el tuyo */
        try {
            Optional<Refund> refund = refundService.getRefundById(id);
            return refund.map(value -> new ResponseEntity<>(RefundResponse.fromEntity(value), HttpStatus.OK))
                    .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
        } catch (Exception e) {
            return new ResponseEntity<>("Error interno", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/all")
    public ResponseEntity<List<RefundResponse>> getAllRefunds() { /* igual que el tuyo */
        try {
            List<Refund> refunds = refundService.getAllRefunds();
            if (refunds.isEmpty()) return new ResponseEntity<>(HttpStatus.NO_CONTENT);
            List<RefundResponse> out = refunds.stream().map(RefundResponse::fromEntity).collect(Collectors.toList());
            return new ResponseEntity<>(out, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/payment/{paymentId}")
    public ResponseEntity<List<RefundResponse>> getByPayment(@PathVariable Long paymentId) { /* igual */
        try {
            List<RefundResponse> list = refundService.getRefundsByPaymentId(paymentId).stream()
                    .map(RefundResponse::fromEntity)
                    .collect(Collectors.toList());
            if (list.isEmpty()) return new ResponseEntity<>(HttpStatus.NO_CONTENT);
            return new ResponseEntity<>(list, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<RefundResponse>> getByStatus(@PathVariable RefundStatus status) { /* igual */
        try {
            List<RefundResponse> list = refundService.getRefundsByStatus(status).stream()
                    .map(RefundResponse::fromEntity)
                    .collect(Collectors.toList());
            if (list.isEmpty()) return new ResponseEntity<>(HttpStatus.NO_CONTENT);
            return new ResponseEntity<>(list, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}