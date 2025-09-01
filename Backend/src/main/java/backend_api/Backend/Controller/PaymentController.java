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
import backend_api.Backend.DTO.payment.PaymentSearchRequest;
import backend_api.Backend.DTO.payment.PaymentStatsRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

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
            
            // TODO: Buscar provider_id por provider_reference
            // Temporalmente usar provider_reference como string para pruebas
            // Long providerId = providerService.findIdByReference(request.getProvider_reference());
            // payment.setProvider_id(providerId);
            payment.setProvider_id(1L); 
            
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
            
            PaymentResponse response = PaymentResponse.fromEntity(savedPayment);
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Obtener todos los pagos
    @GetMapping("/all")
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
    
    
    // Buscar pagos con filtros avanzados y paginación (POST + JSON)
    @PostMapping("/search")
    public ResponseEntity<Page<Payment>> searchPaymentsWithFilters(
            @Valid @RequestBody PaymentSearchRequest request) {
        try {
            Sort sort = request.getSortDir().equalsIgnoreCase("desc") ? 
                       Sort.by(request.getSortBy()).descending() : 
                       Sort.by(request.getSortBy()).ascending();
            
            Pageable pageable = PageRequest.of(request.getPage(), request.getSize(), sort);
            
            Page<Payment> payments = paymentService.findWithFilters(
                request.getStatus(), 
                request.getCurrency(), 
                request.getMinAmount(), 
                request.getMaxAmount(), 
                request.getStartDate(), 
                request.getEndDate(), 
                pageable);
                
            return ResponseEntity.ok(payments);
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    // Buscar pagos por nombre de usuario (POST + JSON)
    @PostMapping("/search/user")
    public ResponseEntity<Page<Payment>> searchPaymentsByUserName(
            @Valid @RequestBody PaymentSearchRequest request) {
        try {
            if (request.getUserName() == null || request.getUserName().trim().isEmpty()) {
                return ResponseEntity.badRequest().build();
            }
            
            Pageable pageable = PageRequest.of(
                request.getPage(), 
                request.getSize(), 
                Sort.by("created_at").descending());
            Page<Payment> payments = paymentService.findByUserNameContaining(request.getUserName(), pageable);
            
            return ResponseEntity.ok(payments);
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    // Buscar pagos por rango de monto (POST + JSON)
    @PostMapping("/search/amount")
    public ResponseEntity<Page<Payment>> searchPaymentsByAmountRange(
            @Valid @RequestBody PaymentSearchRequest request) {
        try {
            if (request.getMinAmount() == null || request.getMaxAmount() == null) {
                return ResponseEntity.badRequest().build();
            }
            
            Sort sort = request.getSortDir().equalsIgnoreCase("desc") ? 
                       Sort.by(request.getSortBy()).descending() : 
                       Sort.by(request.getSortBy()).ascending();
                       
            Pageable pageable = PageRequest.of(request.getPage(), request.getSize(), sort);
            Page<Payment> payments = paymentService.findByAmountTotalBetween(
                request.getMinAmount(), 
                request.getMaxAmount(), 
                pageable);
            
            return ResponseEntity.ok(payments);
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    // Obtener estadísticas de pagos por usuario (POST + JSON)
    @PostMapping("/stats/user")
    public ResponseEntity<Object> getPaymentStatsByUser(
            @Valid @RequestBody PaymentStatsRequest request) {
        try {
            Long userId = request.getUserId();
            
            BigDecimal totalApproved = paymentService.getTotalAmountByUserIdAndStatus(userId, PaymentStatus.APPROVED);
            BigDecimal totalPending = paymentService.getTotalAmountByUserIdAndStatus(userId, PaymentStatus.PENDING);
            BigDecimal totalRejected = paymentService.getTotalAmountByUserIdAndStatus(userId, PaymentStatus.REJECTED);
            BigDecimal totalRefunded = paymentService.getTotalAmountByUserIdAndStatus(userId, PaymentStatus.REFUNDED);
            
            java.util.Map<String, Object> stats = new java.util.HashMap<>();
            stats.put("userId", userId);
            stats.put("currency", request.getCurrency() != null ? request.getCurrency() : "ALL");
            
            // Totales por status
            java.util.Map<String, Object> totals = new java.util.HashMap<>();
            totals.put("approved", totalApproved != null ? totalApproved : BigDecimal.ZERO);
            totals.put("pending", totalPending != null ? totalPending : BigDecimal.ZERO);
            totals.put("rejected", totalRejected != null ? totalRejected : BigDecimal.ZERO);
            totals.put("refunded", totalRefunded != null ? totalRefunded : BigDecimal.ZERO);
            stats.put("totals", totals);
            
            // Conteos por status
            java.util.Map<String, Object> counts = new java.util.HashMap<>();
            counts.put("approved", paymentService.countPaymentsByStatus(PaymentStatus.APPROVED));
            counts.put("pending", paymentService.countPaymentsByStatus(PaymentStatus.PENDING));
            counts.put("rejected", paymentService.countPaymentsByStatus(PaymentStatus.REJECTED));
            counts.put("refunded", paymentService.countPaymentsByStatus(PaymentStatus.REFUNDED));
            stats.put("counts", counts);
            
            // Información del período
            if (request.getStartDate() != null && request.getEndDate() != null) {
                java.util.Map<String, String> period = new java.util.HashMap<>();
                period.put("startDate", request.getStartDate());
                period.put("endDate", request.getEndDate());
                stats.put("period", period);
            }
            
            return ResponseEntity.ok(stats);
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
