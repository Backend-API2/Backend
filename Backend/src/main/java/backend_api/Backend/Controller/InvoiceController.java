package backend_api.Backend.Controller;

import backend_api.Backend.DTO.invoice.*;
import backend_api.Backend.Entity.invoice.InvoiceStatus;
import backend_api.Backend.Service.Interface.InvoiceService;
import backend_api.Backend.Service.Common.AuthenticationService;
import backend_api.Backend.Service.Common.EntityValidationService;
import backend_api.Backend.Entity.user.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
@RequestMapping("/api/invoices")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
@Tag(name = "Facturas", description = "Endpoints para gestión de facturas, incluyendo creación, consulta, actualización y generación de PDFs")
public class InvoiceController {
    
    private final InvoiceService invoiceService;
    private final AuthenticationService authenticationService;
    
    
    @Operation(
        summary = "Crear nueva factura",
        description = "Crea una nueva factura en el sistema. Solo usuarios con rol MERCHANT pueden crear facturas."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201",
            description = "Factura creada exitosamente",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = InvoiceResponse.class),
                examples = @ExampleObject(
                    name = "Factura creada",
                    value = """
                    {
                        "id": 1,
                        "invoiceNumber": "INV-001",
                        "amount": 100.00,
                        "status": "PENDING",
                        "createdAt": "2024-01-15T10:30:00",
                        "dueDate": "2024-02-15T10:30:00"
                    }
                    """
                )
            )
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Acceso denegado - Se requiere rol MERCHANT",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = String.class),
                examples = @ExampleObject(
                    name = "Acceso denegado",
                    value = "Forbidden"
                )
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Datos de entrada inválidos",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = String.class),
                examples = @ExampleObject(
                    name = "Validación fallida",
                    value = "Bad Request"
                )
            )
        )
    })
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping
    @PreAuthorize("hasRole('MERCHANT')")
    public ResponseEntity<InvoiceResponse> createInvoice(
        @Parameter(
            description = "Datos de la nueva factura",
            required = true,
            schema = @Schema(implementation = CreateInvoiceRequest.class)
        )
        @Valid @RequestBody CreateInvoiceRequest request) {
        log.info("Creando nueva factura para el pago: {}", request.getPaymentId());
        InvoiceResponse response = invoiceService.createInvoice(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    @Operation(
        summary = "Obtener factura por ID",
        description = "Retorna los detalles de una factura específica por su ID. Accesible para usuarios MERCHANT y USER."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Factura encontrada",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = InvoiceResponse.class),
                examples = @ExampleObject(
                    name = "Factura encontrada",
                    value = """
                    {
                        "id": 1,
                        "invoiceNumber": "INV-001",
                        "amount": 100.00,
                        "status": "PENDING",
                        "createdAt": "2024-01-15T10:30:00",
                        "dueDate": "2024-02-15T10:30:00"
                    }
                    """
                )
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Factura no encontrada",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = String.class),
                examples = @ExampleObject(
                    name = "Factura no encontrada",
                    value = "Not Found"
                )
            )
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Acceso denegado",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = String.class),
                examples = @ExampleObject(
                    name = "Acceso denegado",
                    value = "Forbidden"
                )
            )
        )
    })
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('MERCHANT') or hasRole('USER')")
    public ResponseEntity<InvoiceResponse> getInvoiceById(
        @Parameter(
            description = "ID de la factura",
            required = true,
            example = "1"
        )
        @PathVariable Long id) {
        log.info("Obteniendo factura con ID: {}", id);
        InvoiceResponse response = invoiceService.getInvoiceById(id);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/number/{invoiceNumber}")
    @PreAuthorize("hasRole('MERCHANT') or hasRole('USER')")
    public ResponseEntity<InvoiceResponse> getInvoiceByNumber(@PathVariable String invoiceNumber) {
        log.info("Obteniendo factura con número: {}", invoiceNumber);
        InvoiceResponse response = invoiceService.getInvoiceByNumber(invoiceNumber);
        return ResponseEntity.ok(response);
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('MERCHANT')")
    public ResponseEntity<InvoiceResponse> updateInvoice(
            @PathVariable Long id, 
            @Valid @RequestBody UpdateInvoiceRequest request) {
        log.info("Actualizando factura con ID: {}", id);
        InvoiceResponse response = invoiceService.updateInvoice(id, request);
        return ResponseEntity.ok(response);
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('MERCHANT')")
    public ResponseEntity<Map<String, String>> deleteInvoice(@PathVariable Long id) {
        log.info("Eliminando factura con ID: {}", id);
        invoiceService.deleteInvoice(id);
        
        Map<String, String> response = new HashMap<>();
        response.put("message", "Factura eliminada exitosamente");
        return ResponseEntity.ok(response);
    }
    
    
    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('MERCHANT')")
    public ResponseEntity<InvoiceResponse> updateInvoiceStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateInvoiceStatusRequest request) {
        log.info("Actualizando estado de la factura ID: {} a: {}", id, request.getStatus());
        InvoiceResponse response = invoiceService.updateInvoiceStatus(id, request);
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/{id}/send")
    @PreAuthorize("hasRole('MERCHANT')")
    public ResponseEntity<InvoiceResponse> sendInvoice(@PathVariable Long id) {
        log.info("Enviando factura con ID: {}", id);
        InvoiceResponse response = invoiceService.markAsSent(id);
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/{id}/pay")
    @PreAuthorize("hasRole('MERCHANT')")
    public ResponseEntity<InvoiceResponse> markInvoiceAsPaid(@PathVariable Long id) {
        log.info("Marcando factura como pagada con ID: {}", id);
        InvoiceResponse response = invoiceService.markAsPaid(id);
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasRole('MERCHANT')")
    public ResponseEntity<InvoiceResponse> cancelInvoice(@PathVariable Long id) {
        log.info("Cancelando factura con ID: {}", id);
        InvoiceResponse response = invoiceService.cancelInvoice(id);
        return ResponseEntity.ok(response);
    }
    
    
    @PostMapping("/search")
    @PreAuthorize("hasRole('MERCHANT') or hasRole('USER')")
    public ResponseEntity<Page<InvoiceResponse>> searchInvoices(@Valid @RequestBody InvoiceSearchRequest request) {
        log.info("Buscando facturas con filtros");
        Page<InvoiceResponse> response = invoiceService.searchInvoices(request);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/user/{userId}")
    @PreAuthorize("hasRole('MERCHANT') or hasRole('USER')")
    public ResponseEntity<Page<InvoiceResponse>> getInvoicesByUserId(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("Obteniendo facturas para el ID de usuario: {}", userId);
        Page<InvoiceResponse> response = invoiceService.getInvoicesByUserId(userId, page, size);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/provider/{providerId}")
    @PreAuthorize("hasRole('MERCHANT')")
    public ResponseEntity<Page<InvoiceResponse>> getInvoicesByProviderId(
            @PathVariable Long providerId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("Obteniendo facturas para el ID de proveedor: {}", providerId);
        Page<InvoiceResponse> response = invoiceService.getInvoicesByProviderId(providerId, page, size);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/status/{status}")
    @PreAuthorize("hasRole('MERCHANT')")
    public ResponseEntity<Page<InvoiceResponse>> getInvoicesByStatus(
            @PathVariable String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("Buscando facturas con estado: {}", status);
        InvoiceStatus invoiceStatus = InvoiceStatus.valueOf(status.toUpperCase());
        Page<InvoiceResponse> response = invoiceService.getInvoicesByStatus(invoiceStatus, page, size);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/payment/{paymentId}")
    @PreAuthorize("hasRole('MERCHANT') or hasRole('USER')")
    public ResponseEntity<List<InvoiceResponse>> getInvoicesByPaymentId(@PathVariable Long paymentId) {
        log.info("Obteniendo facturas para el ID de pago: {}", paymentId);
        List<InvoiceResponse> response = invoiceService.getInvoicesByPaymentId(paymentId);
        return ResponseEntity.ok(response);
    }
    
    
    @PostMapping("/{id}/pdf/generate")
    @PreAuthorize("hasRole('MERCHANT')")
    public ResponseEntity<Map<String, String>> generatePdf(@PathVariable Long id) {
        log.info("Generando PDF para la factura ID: {}", id);
        String pdfUrl = invoiceService.generatePdf(id);
        
        Map<String, String> response = new HashMap<>();
        response.put("pdfUrl", pdfUrl);
        response.put("message", "PDF generado exitosamente");
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/{id}/pdf/regenerate")
    @PreAuthorize("hasRole('MERCHANT')")
    public ResponseEntity<Map<String, String>> regeneratePdf(@PathVariable Long id) {
        log.info("Regenerando PDF para la factura ID: {}", id);
        String pdfUrl = invoiceService.regeneratePdf(id);
        
        Map<String, String> response = new HashMap<>();
        response.put("pdfUrl", pdfUrl);
        response.put("message", "PDF regenerado exitosamente");
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/{id}/pdf/download")
    @PreAuthorize("hasRole('MERCHANT') or hasRole('USER')")
    public ResponseEntity<byte[]> downloadPdf(@PathVariable Long id) {
        log.info("Descargando PDF para la factura ID: {}", id);
        byte[] pdfContent = invoiceService.downloadPdf(id);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "invoice_" + id + ".pdf");
        headers.setContentLength(pdfContent.length);
        
        return ResponseEntity.ok()
                .headers(headers)
                .body(pdfContent);
    }
    
    
    @GetMapping("/{id}/timeline")
    @PreAuthorize("hasRole('MERCHANT') or hasRole('USER')")
    public ResponseEntity<List<InvoiceEventResponse>> getInvoiceTimeline(@PathVariable Long id) {
        log.info("Obteniendo la línea de tiempo para la factura ID: {}", id);
        List<InvoiceEventResponse> response = invoiceService.getInvoiceTimeline(id);
        return ResponseEntity.ok(response);
    }
    
    
    @GetMapping("/provider/{providerId}/summary")
    @PreAuthorize("hasRole('MERCHANT')")
    public ResponseEntity<InvoiceSummaryResponse> getInvoiceSummary(@PathVariable Long providerId) {
        log.info("Obteniendo resumen de facturas para el ID de proveedor: {}", providerId);
        InvoiceSummaryResponse response = invoiceService.getInvoiceSummary(providerId);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/user/{userId}/summary")
    @PreAuthorize("hasRole('MERCHANT') or hasRole('USER')")
    public ResponseEntity<InvoiceSummaryResponse> getInvoiceSummaryByUser(@PathVariable Long userId) {
        log.info("Obteniendo resumen de facturas para el ID de usuario: {}", userId);
        InvoiceSummaryResponse response = invoiceService.getInvoiceSummaryByUser(userId);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/due-soon")
    @PreAuthorize("hasRole('MERCHANT')")
    public ResponseEntity<List<InvoiceResponse>> getInvoicesDueSoon(
            @RequestParam(defaultValue = "7") int days) {
        log.info("Obteniendo facturas que vencen en {} días", days);
        List<InvoiceResponse> response = invoiceService.getInvoicesDueSoon(days);
        return ResponseEntity.ok(response);
    }
    
    
    
    @PostMapping("/create-from-payment/{paymentId}")
    @PreAuthorize("hasRole('MERCHANT')")
    public ResponseEntity<InvoiceResponse> createInvoiceFromPayment(@PathVariable Long paymentId) {
        log.info("Creando factura a partir del ID de pago: {}", paymentId);
        InvoiceResponse response = invoiceService.createInvoiceFromPayment(paymentId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    
    // GET /api/invoices/my-invoices - Obtener MIS facturas usando el token
    @GetMapping("/my-invoices")
    public ResponseEntity<Page<InvoiceResponse>> getMyInvoices(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestHeader("Authorization") String authHeader) {
        try {
            User user = authenticationService.getUserFromToken(authHeader);
            if (user == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            Page<InvoiceResponse> response = invoiceService.getInvoicesByUserId(user.getId(), page, size);
            return ResponseEntity.ok(response);
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } catch (Exception e) {
            log.error("Error obteniendo facturas del usuario", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    // GET /api/invoices/my-summary - MI resumen de facturas
    @GetMapping("/my-summary")
    public ResponseEntity<InvoiceSummaryResponse> getMySummary(
            @RequestHeader("Authorization") String authHeader) {
        try {
            User user = authenticationService.getUserFromToken(authHeader);
            if (user == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            InvoiceSummaryResponse response = invoiceService.getInvoiceSummaryByUser(user.getId());
            return ResponseEntity.ok(response);
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } catch (Exception e) {
            log.error("Error obteniendo resumen de facturas del usuario", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
