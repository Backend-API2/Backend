package backend_api.Backend.Controller;

import backend_api.Backend.DTO.invoice.*;
import backend_api.Backend.Entity.invoice.InvoiceStatus;
import backend_api.Backend.Service.Interface.InvoiceService;
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

@RestController
@RequestMapping("/api/invoices")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class InvoiceController {
    
    private final InvoiceService invoiceService;
    
    
    @PostMapping
    @PreAuthorize("hasRole('MERCHANT')")
    public ResponseEntity<InvoiceResponse> createInvoice(@Valid @RequestBody CreateInvoiceRequest request) {
        log.info("Creating new invoice for payment: {}", request.getPaymentId());
        InvoiceResponse response = invoiceService.createInvoice(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('MERCHANT') or hasRole('CLIENT')")
    public ResponseEntity<InvoiceResponse> getInvoiceById(@PathVariable Long id) {
        log.info("Fetching invoice with ID: {}", id);
        InvoiceResponse response = invoiceService.getInvoiceById(id);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/number/{invoiceNumber}")
    @PreAuthorize("hasRole('MERCHANT') or hasRole('CLIENT')")
    public ResponseEntity<InvoiceResponse> getInvoiceByNumber(@PathVariable String invoiceNumber) {
        log.info("Fetching invoice with number: {}", invoiceNumber);
        InvoiceResponse response = invoiceService.getInvoiceByNumber(invoiceNumber);
        return ResponseEntity.ok(response);
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('MERCHANT')")
    public ResponseEntity<InvoiceResponse> updateInvoice(
            @PathVariable Long id, 
            @Valid @RequestBody UpdateInvoiceRequest request) {
        log.info("Updating invoice with ID: {}", id);
        InvoiceResponse response = invoiceService.updateInvoice(id, request);
        return ResponseEntity.ok(response);
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('MERCHANT')")
    public ResponseEntity<Map<String, String>> deleteInvoice(@PathVariable Long id) {
        log.info("Deleting invoice with ID: {}", id);
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
        log.info("Updating status for invoice ID: {} to: {}", id, request.getStatus());
        InvoiceResponse response = invoiceService.updateInvoiceStatus(id, request);
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/{id}/send")
    @PreAuthorize("hasRole('MERCHANT')")
    public ResponseEntity<InvoiceResponse> sendInvoice(@PathVariable Long id) {
        log.info("Sending invoice with ID: {}", id);
        InvoiceResponse response = invoiceService.markAsSent(id);
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/{id}/pay")
    @PreAuthorize("hasRole('MERCHANT')")
    public ResponseEntity<InvoiceResponse> markInvoiceAsPaid(@PathVariable Long id) {
        log.info("Marking invoice as paid with ID: {}", id);
        InvoiceResponse response = invoiceService.markAsPaid(id);
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasRole('MERCHANT')")
    public ResponseEntity<InvoiceResponse> cancelInvoice(@PathVariable Long id) {
        log.info("Canceling invoice with ID: {}", id);
        InvoiceResponse response = invoiceService.cancelInvoice(id);
        return ResponseEntity.ok(response);
    }
    
    
    @PostMapping("/search")
    @PreAuthorize("hasRole('MERCHANT') or hasRole('CLIENT')")
    public ResponseEntity<Page<InvoiceResponse>> searchInvoices(@Valid @RequestBody InvoiceSearchRequest request) {
        log.info("Searching invoices with filters");
        Page<InvoiceResponse> response = invoiceService.searchInvoices(request);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/user/{userId}")
    @PreAuthorize("hasRole('MERCHANT') or hasRole('CLIENT')")
    public ResponseEntity<Page<InvoiceResponse>> getInvoicesByUserId(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("Fetching invoices for user ID: {}", userId);
        Page<InvoiceResponse> response = invoiceService.getInvoicesByUserId(userId, page, size);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/provider/{providerId}")
    @PreAuthorize("hasRole('MERCHANT')")
    public ResponseEntity<Page<InvoiceResponse>> getInvoicesByProviderId(
            @PathVariable Long providerId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("Fetching invoices for provider ID: {}", providerId);
        Page<InvoiceResponse> response = invoiceService.getInvoicesByProviderId(providerId, page, size);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/status/{status}")
    @PreAuthorize("hasRole('MERCHANT')")
    public ResponseEntity<Page<InvoiceResponse>> getInvoicesByStatus(
            @PathVariable String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("Fetching invoices with status: {}", status);
        InvoiceStatus invoiceStatus = InvoiceStatus.valueOf(status.toUpperCase());
        Page<InvoiceResponse> response = invoiceService.getInvoicesByStatus(invoiceStatus, page, size);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/payment/{paymentId}")
    @PreAuthorize("hasRole('MERCHANT') or hasRole('CLIENT')")
    public ResponseEntity<List<InvoiceResponse>> getInvoicesByPaymentId(@PathVariable Long paymentId) {
        log.info("Fetching invoices for payment ID: {}", paymentId);
        List<InvoiceResponse> response = invoiceService.getInvoicesByPaymentId(paymentId);
        return ResponseEntity.ok(response);
    }
    
    
    @PostMapping("/{id}/pdf/generate")
    @PreAuthorize("hasRole('MERCHANT')")
    public ResponseEntity<Map<String, String>> generatePdf(@PathVariable Long id) {
        log.info("Generating PDF for invoice ID: {}", id);
        String pdfUrl = invoiceService.generatePdf(id);
        
        Map<String, String> response = new HashMap<>();
        response.put("pdfUrl", pdfUrl);
        response.put("message", "PDF generado exitosamente");
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/{id}/pdf/regenerate")
    @PreAuthorize("hasRole('MERCHANT')")
    public ResponseEntity<Map<String, String>> regeneratePdf(@PathVariable Long id) {
        log.info("Regenerating PDF for invoice ID: {}", id);
        String pdfUrl = invoiceService.regeneratePdf(id);
        
        Map<String, String> response = new HashMap<>();
        response.put("pdfUrl", pdfUrl);
        response.put("message", "PDF regenerado exitosamente");
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/{id}/pdf/download")
    @PreAuthorize("hasRole('MERCHANT') or hasRole('CLIENT')")
    public ResponseEntity<byte[]> downloadPdf(@PathVariable Long id) {
        log.info("Downloading PDF for invoice ID: {}", id);
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
    @PreAuthorize("hasRole('MERCHANT') or hasRole('CLIENT')")
    public ResponseEntity<List<InvoiceEventResponse>> getInvoiceTimeline(@PathVariable Long id) {
        log.info("Fetching timeline for invoice ID: {}", id);
        List<InvoiceEventResponse> response = invoiceService.getInvoiceTimeline(id);
        return ResponseEntity.ok(response);
    }
    
    
    @GetMapping("/provider/{providerId}/summary")
    @PreAuthorize("hasRole('MERCHANT')")
    public ResponseEntity<InvoiceSummaryResponse> getInvoiceSummary(@PathVariable Long providerId) {
        log.info("Fetching invoice summary for provider ID: {}", providerId);
        InvoiceSummaryResponse response = invoiceService.getInvoiceSummary(providerId);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/user/{userId}/summary")
    @PreAuthorize("hasRole('MERCHANT') or hasRole('CLIENT')")
    public ResponseEntity<InvoiceSummaryResponse> getInvoiceSummaryByUser(@PathVariable Long userId) {
        log.info("Fetching invoice summary for user ID: {}", userId);
        InvoiceSummaryResponse response = invoiceService.getInvoiceSummaryByUser(userId);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/due-soon")
    @PreAuthorize("hasRole('MERCHANT')")
    public ResponseEntity<List<InvoiceResponse>> getInvoicesDueSoon(
            @RequestParam(defaultValue = "7") int days) {
        log.info("Fetching invoices due in {} days", days);
        List<InvoiceResponse> response = invoiceService.getInvoicesDueSoon(days);
        return ResponseEntity.ok(response);
    }
    
    
    @PostMapping("/process-overdue")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> processOverdueInvoices() {
        log.info("Processing overdue invoices");
        invoiceService.processOverdueInvoices();
        
        Map<String, String> response = new HashMap<>();
        response.put("message", "Facturas vencidas procesadas exitosamente");
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/send-reminders")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> sendDueReminders() {
        log.info("Sending due reminders");
        invoiceService.sendDueReminders();
        
        Map<String, String> response = new HashMap<>();
        response.put("message", "Recordatorios enviados exitosamente");
        return ResponseEntity.ok(response);
    }
    
    
    @PostMapping("/create-from-payment/{paymentId}")
    @PreAuthorize("hasRole('MERCHANT')")
    public ResponseEntity<InvoiceResponse> createInvoiceFromPayment(@PathVariable Long paymentId) {
        log.info("Creating invoice from payment ID: {}", paymentId);
        InvoiceResponse response = invoiceService.createInvoiceFromPayment(paymentId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
