package backend_api.Backend.Controller;

import backend_api.Backend.Entity.payment.Payment;
import backend_api.Backend.Entity.payment.PaymentStatus;
import backend_api.Backend.Service.Interface.PaymentService;
import backend_api.Backend.DTO.payment.PaymentResponse;
import backend_api.Backend.DTO.payment.UpdatePaymentRequest;
import backend_api.Backend.Auth.JwtUtil;
import backend_api.Backend.Repository.UserRepository;
import backend_api.Backend.Entity.user.User;
import backend_api.Backend.DTO.payment.CreatePaymentRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/payments")
@CrossOrigin(origins = "*")
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    @Autowired 
    private JwtUtil jwtUtil;
    
    @Autowired
    private UserRepository userRepository;

    //  CREAR NUEVO PAGO 
    @PostMapping
    public ResponseEntity<PaymentResponse> createPayment(
            @Valid @RequestBody CreatePaymentRequest request,
            @RequestHeader("Authorization") String authHeader) {
        try {
            String token = authHeader.replace("Bearer ", "");
            String email = jwtUtil.getSubject(token);
            
            if (email == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            
            Optional<User> userOpt = userRepository.findByEmail(email);
            if (!userOpt.isPresent()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            
            User user = userOpt.get();
            
            Payment payment = new Payment();
            
            payment.setUser_id(user.getId());
            
            payment.setProvider_id(request.getProvider_id());
            payment.setAmount_subtotal(request.getAmount_subtotal());
            payment.setTaxes(request.getTaxes());
            payment.setFees(request.getFees());
            payment.setCurrency(request.getCurrency());
            payment.setMetadata(request.getMetadata());
            
            // TODO: Convertir payment_method_type a PaymentMethod entity
            // Por ahora guardamos como string en un campo si existe, o manejar como corresponda
            
            BigDecimal total = request.getAmount_subtotal()
                                .add(request.getTaxes())
                                .add(request.getFees());
            payment.setAmount_total(total);
            
            payment.setStatus(PaymentStatus.PENDING);
            payment.setCreated_at(LocalDateTime.now());
            payment.setUpdated_at(LocalDateTime.now());
            payment.setPayment_intent_id("pi_" + UUID.randomUUID().toString().replace("-", ""));
            
            // TODO: Buscar solicitud_id y cotizacion_id por references
            // TODO: Buscar payment_method_id por payment_method_type
            
            Payment savedPayment = paymentService.createPayment(payment);
            
            PaymentResponse response = new PaymentResponse();
            response.setId(savedPayment.getId());
            response.setUser_id(savedPayment.getUser_id());
            response.setProvider_id(savedPayment.getProvider_id());
            response.setAmount_total(savedPayment.getAmount_total());
            response.setCurrency(savedPayment.getCurrency());
            response.setStatus(savedPayment.getStatus());
            response.setPayment_intent_id(savedPayment.getPayment_intent_id());
            response.setCreated_at(savedPayment.getCreated_at());
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Obtener todos los pagos
    @GetMapping
    public ResponseEntity<List<Payment>> getAllPayments() {
        try {
            List<Payment> payments = paymentService.getAllPayments();
            if (payments.isEmpty()) {
                return ResponseEntity.noContent().build();
            }
            return ResponseEntity.ok(payments);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Obtener pago por ID
    @GetMapping("/{paymentId}")
    public ResponseEntity<Payment> getPaymentById(@PathVariable Long paymentId) {
        try {
            Optional<Payment> payment = paymentService.getPaymentById(paymentId);
            return payment.map(ResponseEntity::ok)
                         .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Obtener pago por Payment Intent ID
    @GetMapping("/intent/{paymentIntentId}")
    public ResponseEntity<Payment> getPaymentByIntentId(@PathVariable String paymentIntentId) {
        try {
            Optional<Payment> payment = paymentService.getPaymentByIntentId(paymentIntentId);
            return payment.map(ResponseEntity::ok)
                         .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Obtener pagos por User_Id
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Payment>> getPaymentsByUserId(@PathVariable Long userId) {
        try {
            List<Payment> payments = paymentService.getPaymentsByUserId(userId);
            if (payments.isEmpty()) {
                return ResponseEntity.noContent().build();
            }
            return ResponseEntity.ok(payments);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Obtener pagos por Provider_ID
    @GetMapping("/provider/{providerId}")
    public ResponseEntity<List<Payment>> getPaymentsByProviderId(@PathVariable Long providerId) {
        try {
            List<Payment> payments = paymentService.getPaymentsByProviderId(providerId);
            if (payments.isEmpty()) {
                return ResponseEntity.noContent().build();
            }
            return ResponseEntity.ok(payments);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Obtener pagos por Status
    @GetMapping("/status/{status}")
    public ResponseEntity<List<Payment>> getPaymentsByStatus(@PathVariable PaymentStatus status) {
        try {
            List<Payment> payments = paymentService.getPaymentsByStatus(status);
            if (payments.isEmpty()) {
                return ResponseEntity.noContent().build();
            }
            return ResponseEntity.ok(payments);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Actualizar pago
    @PutMapping("/{paymentId}")
    public ResponseEntity<Payment> updatePayment(@PathVariable Long paymentId, 
                                               @RequestBody UpdatePaymentRequest request) {
        try {
            Optional<Payment> paymentOpt = paymentService.getPaymentById(paymentId);
            if (!paymentOpt.isPresent()) {
                return ResponseEntity.notFound().build();
            }
            
            Payment payment = paymentOpt.get();
            payment.setUpdated_at(LocalDateTime.now());
            
            Payment updatedPayment = paymentService.updatePayment(paymentId, payment);
            return ResponseEntity.ok(updatedPayment);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Eliminar pago
    @DeleteMapping("/{paymentId}")
    public ResponseEntity<Void> deletePayment(@PathVariable Long paymentId) {
        try {
            Optional<Payment> payment = paymentService.getPaymentById(paymentId);
            if (!payment.isPresent()) {
                return ResponseEntity.notFound().build();
            }
            
            paymentService.deletePayment(paymentId);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
