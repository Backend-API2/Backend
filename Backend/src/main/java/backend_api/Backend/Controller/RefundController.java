package backend_api.Backend.Controller;

import backend_api.Backend.DTO.refund.CreateRefundRequest;
import backend_api.Backend.DTO.refund.RefundResponse;
import backend_api.Backend.DTO.refund.UpdateRefundStatusRequest;
import backend_api.Backend.Entity.refund.Refund;
import backend_api.Backend.Entity.refund.RefundStatus;
import backend_api.Backend.Service.Interface.RefundService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/refunds")
@CrossOrigin(origins = "*")
public class RefundController {

    @Autowired
    private RefundService refundService;

    // POST /api/refunds/create
    @PostMapping("/create")
    public ResponseEntity<?> createRefund(@Valid @RequestBody CreateRefundRequest request) {
        try {
            Refund created = refundService.createRefund(request);
            return new ResponseEntity<>(RefundResponse.fromEntity(created), HttpStatus.CREATED);
        } catch (IllegalArgumentException | IllegalStateException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>("Error interno", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // GET /api/refunds/{id}
    @GetMapping("/{id}")
    public ResponseEntity<?> getRefundById(@PathVariable Long id) {
        try {
            Optional<Refund> refund = refundService.getRefundById(id);
            return refund.map(value -> new ResponseEntity<>(RefundResponse.fromEntity(value), HttpStatus.OK))
                    .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
        } catch (Exception e) {
            return new ResponseEntity<>("Error interno", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // GET /api/refunds/all
    @GetMapping("/all")
    public ResponseEntity<List<RefundResponse>> getAllRefunds() {
        try {
            List<Refund> refunds = refundService.getAllRefunds();
            if (refunds.isEmpty()) return new ResponseEntity<>(HttpStatus.NO_CONTENT);

            List<RefundResponse> out = refunds.stream()
                    .map(RefundResponse::fromEntity)
                    .collect(Collectors.toList());
            return new ResponseEntity<>(out, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // PATCH /api/refunds/{id}/status
    @PatchMapping("/{id}/status")
    public ResponseEntity<?> updateRefundStatus(@PathVariable Long id,
                                                @Valid @RequestBody UpdateRefundStatusRequest body) {
        try {
            Refund updated = refundService.updateRefundStatus(id, body.getStatus());
            return new ResponseEntity<>(RefundResponse.fromEntity(updated), HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>("Error interno", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // GET /api/refunds/payment/{paymentId}
    @GetMapping("/payment/{paymentId}")
    public ResponseEntity<List<RefundResponse>> getByPayment(@PathVariable Long paymentId) {
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

    // GET /api/refunds/status/{status}
    @GetMapping("/status/{status}")
    public ResponseEntity<List<RefundResponse>> getByStatus(@PathVariable RefundStatus status) {
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