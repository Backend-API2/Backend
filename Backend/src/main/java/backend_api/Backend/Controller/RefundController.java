package backend_api.Backend.Controller;

import backend_api.Backend.Entity.refund.Refund;
import backend_api.Backend.Entity.refund.RefundStatus;
import backend_api.Backend.Service.Interface.RefundService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/refunds")
@CrossOrigin(origins = "*")
public class RefundController {

    @Autowired
    private RefundService refundService;

    // POST /api/refunds  (MERCHANT, ADMIN)
    @PostMapping
    public ResponseEntity<?> createRefund(@RequestBody Refund refund) {
        try {
            Refund created = refundService.createRefund(refund);
            return new ResponseEntity<>(created, HttpStatus.CREATED);
        } catch (IllegalArgumentException | IllegalStateException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>("Error interno", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // GET /api/refunds/{id}  (MERCHANT, ADMIN)
    @GetMapping("/{id}")
    public ResponseEntity<?> getRefundById(@PathVariable Long id) {
        try {
            Optional<Refund> refund = refundService.getRefundById(id);
            return refund.map(value -> new ResponseEntity<>(value, HttpStatus.OK))
                    .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
        } catch (Exception e) {
            return new ResponseEntity<>("Error interno", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // GET /api/refunds  (ADMIN)
    @GetMapping
    public ResponseEntity<List<Refund>> getAllRefunds() {
        try {
            List<Refund> refunds = refundService.getAllRefunds();
            if (refunds.isEmpty()) return new ResponseEntity<>(HttpStatus.NO_CONTENT);
            return new ResponseEntity<>(refunds, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // PATCH /api/refunds/{id}/status  (MERCHANT, ADMIN)
    @PatchMapping("/{id}/status")
    public ResponseEntity<?> updateRefundStatus(@PathVariable Long id, @RequestBody RefundStatus status) {
        try {
            Refund updated = refundService.updateRefundStatus(id, status);
            return new ResponseEntity<>(updated, HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>("Error interno", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // --- Opcionales útiles (si querés usarlos, agregá en SecurityConfig matchers abajo) ---

    @GetMapping("/payment/{paymentId}")
    public ResponseEntity<List<Refund>> getByPayment(@PathVariable Long paymentId) {
        try {
            List<Refund> list = refundService.getRefundsByPaymentId(paymentId);
            if (list.isEmpty()) return new ResponseEntity<>(HttpStatus.NO_CONTENT);
            return new ResponseEntity<>(list, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<Refund>> getByStatus(@PathVariable RefundStatus status) {
        try {
            List<Refund> list = refundService.getRefundsByStatus(status);
            if (list.isEmpty()) return new ResponseEntity<>(HttpStatus.NO_CONTENT);
            return new ResponseEntity<>(list, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}