package backend_api.Backend.Controller;

import backend_api.Backend.Entity.payment.Payment;
import backend_api.Backend.Entity.payment.PaymentStatus;
import backend_api.Backend.Service.Interface.PaymentService;
import backend_api.Backend.DTO.payment.PaymentResponse;
import backend_api.Backend.Auth.JwtUtil;
import backend_api.Backend.Repository.UserRepository;
import backend_api.Backend.Entity.user.User;
import backend_api.Backend.DTO.payment.CreatePaymentRequest;
import backend_api.Backend.DTO.payment.PaymentSearchRequest;
import backend_api.Backend.DTO.payment.CreatePaymentIntentRequest;
import backend_api.Backend.DTO.payment.ConfirmPaymentRequest;
import backend_api.Backend.Entity.payment.PaymentEvent;
import backend_api.Backend.Entity.payment.PaymentEventType;
import backend_api.Backend.Entity.payment.PaymentAttempt;
import backend_api.Backend.Service.Interface.PaymentEventService;
import backend_api.Backend.Service.Interface.PaymentAttemptService;

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
    
    @Autowired
    private PaymentEventService paymentEventService;
    
    @Autowired
    private PaymentAttemptService paymentAttemptService;

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
            
            // TODO: Buscar solicitud_id y cotizacion_id por references - Se integra con el módulo Cotizacion
            // TODO: Buscar payment_method_id por payment_method_type
            
            Payment savedPayment = paymentService.createPayment(payment);
            
            paymentEventService.createEvent(
                savedPayment.getId(),
                PaymentEventType.PAYMENT_INTENT_CREATED,
                String.format("{\"amount_total\": %s, \"currency\": \"%s\", \"payment_intent_id\": \"%s\"}", 
                    savedPayment.getAmount_total(), savedPayment.getCurrency(), savedPayment.getPayment_intent_id()),
                "user_" + user.getId()
            );
            
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
    
   
    
    @GetMapping("/{paymentId}/timeline")
    public ResponseEntity<List<PaymentEvent>> getPaymentTimeline(@PathVariable Long paymentId) {
        try {
            List<PaymentEvent> timeline = paymentEventService.getPaymentTimeline(paymentId);
            return ResponseEntity.ok(timeline);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @PostMapping("/intents")
    public ResponseEntity<PaymentResponse> createPaymentIntent(
            @Valid @RequestBody CreatePaymentIntentRequest request,
            @RequestHeader("Authorization") String authHeader) {
        try {
            String token = authHeader.replace("Bearer ", "");
            String email = jwtUtil.getSubject(token);
            
            if (email == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            
            // Se integra con el módulo Cotizacion
            Payment payment = paymentService.createPaymentIntent(
                request.getUserId(),
                request.getProviderId(),
                request.getSolicitudId(),
                null, // cotizacionId - Se integra con el módulo Cotizacion
                request.getAmountSubtotal(),
                request.getTaxes(),
                request.getFees(),
                request.getCurrency(),
                request.getMetadata(),
                request.getExpiresInMinutes()
            );
            
            return ResponseEntity.status(HttpStatus.CREATED).body(PaymentResponse.fromEntity(payment));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @PostMapping("/{paymentId}/confirm")
    public ResponseEntity<PaymentResponse> confirmPayment(
            @PathVariable Long paymentId,
            @Valid @RequestBody ConfirmPaymentRequest request) {
        try {
            Payment payment = paymentService.confirmPayment(
                paymentId,
                request.getPaymentMethodType(),
                request.getPaymentMethodId(),
                request.isCaptureImmediately()
            );
            
            return ResponseEntity.ok(PaymentResponse.fromEntity(payment));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @PostMapping("/{paymentId}/cancel")
    public ResponseEntity<PaymentResponse> cancelPayment(
            @PathVariable Long paymentId,
            @RequestParam(required = false, defaultValue = "user_requested") String reason) {
        try {
            Payment payment = paymentService.cancelPayment(paymentId, reason);
            return ResponseEntity.ok(PaymentResponse.fromEntity(payment));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @GetMapping("/{paymentId}/exists")
    public ResponseEntity<Boolean> paymentExists(@PathVariable Long paymentId) {
        try {
            boolean exists = paymentService.existsById(paymentId);
            return ResponseEntity.ok(exists);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @GetMapping("/{paymentId}/attempts")
    public ResponseEntity<List<PaymentAttempt>> getPaymentAttempts(@PathVariable Long paymentId) {
        try {
            List<PaymentAttempt> attempts = paymentAttemptService.getAttemptsByPaymentId(paymentId);
            return ResponseEntity.ok(attempts);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @PostMapping("/{paymentId}/retry")
    public ResponseEntity<PaymentResponse> retryPayment(
            @PathVariable Long paymentId,
            @RequestParam(defaultValue = "3") int maxAttempts) {
        try {
            Payment payment = paymentService.processPaymentWithRetry(paymentId, maxAttempts);
            return ResponseEntity.ok(PaymentResponse.fromEntity(payment));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
