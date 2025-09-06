package backend_api.Backend.Controller;

import backend_api.Backend.Entity.payment.Payment;
import backend_api.Backend.Entity.payment.PaymentStatus;
import backend_api.Backend.Service.Interface.PaymentService;
import backend_api.Backend.DTO.payment.PaymentResponse;
import backend_api.Backend.DTO.payment.PagedPaymentResponse;
import backend_api.Backend.Auth.JwtUtil;
import backend_api.Backend.Repository.UserRepository;
import backend_api.Backend.Entity.user.User;
import backend_api.Backend.DTO.payment.CreatePaymentRequest;
import backend_api.Backend.DTO.payment.PaymentSearchRequest;
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

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.ArrayList;
import java.util.stream.Collectors;


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
            
            payment.setStatus(PaymentStatus.PENDING_APPROVAL);
            payment.setCreated_at(LocalDateTime.now());
            payment.setUpdated_at(LocalDateTime.now());

            
            // TODO: Buscar solicitud_id y cotizacion_id por references - Se integra con el módulo Cotizacion
            // TODO: Buscar payment_method_id por payment_method_type
            
            Payment savedPayment = paymentService.createPayment(payment);
            
            paymentEventService.createEvent(
                savedPayment.getId(),
                PaymentEventType.PAYMENT_PENDING,
                String.format("{\"amount_total\": %s, \"currency\": \"%s\"}",
                    savedPayment.getAmount_total(), savedPayment.getCurrency()),
                "user_" + user.getId()
            );
            
            PaymentResponse response = PaymentResponse.fromEntity(savedPayment);
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
            
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
    
    
    @PutMapping("/{paymentId}/confirm")
    public ResponseEntity<PaymentResponse> confirmPayment(
            @PathVariable Long paymentId) {
        try {
            Payment payment = paymentService.getPaymentById(paymentId)
                    .orElseThrow(() -> new RuntimeException("Pago no encontrado"));
            
            if (payment.getStatus() != PaymentStatus.PENDING_PAYMENT) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(null);
            }

            Payment updatedPayment = paymentService.updatePaymentStatus(paymentId, PaymentStatus.APPROVED);
           
            paymentEventService.createEvent(paymentId,PaymentEventType.PAYMENT_APPROVED,  "{\"confirmed_by\": \"user_action\"}",
            "system");
            
            return ResponseEntity.ok(PaymentResponse.fromEntity(updatedPayment));
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

    @GetMapping("/{paymentId}")
    public ResponseEntity<PaymentResponse> getPaymentById(@PathVariable Long paymentId) {
        try {
            Payment payment = paymentService.getPaymentById(paymentId)
                    .orElseThrow(() -> new RuntimeException("Pago no encontrado"));
            return ResponseEntity.ok(PaymentResponse.fromEntity(payment));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    
   // GET /api/payments/my-payments - Obtener MIS pagos usando el token
    @GetMapping("/my-payments")
    public ResponseEntity<List<PaymentResponse>> getMyPayments(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            String token = authHeader.replace("Bearer ", "");
            String email = jwtUtil.getSubject(token);
            
            if (email == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
            
            List<Payment> payments;
            
            if (user.getRole().name().equals("MERCHANT")) {
                payments = paymentService.getPaymentsByProviderId(user.getId());
            } else {
                payments = paymentService.getPaymentsByUserId(user.getId());
            }
            
            List<PaymentResponse> responses = payments.stream()
                    .map(PaymentResponse::fromEntity)
                    .toList();
            
            return ResponseEntity.ok(responses);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    // GET /api/payments/my-payments/status/{status} - MIS pagos por estado
    @GetMapping("/my-payments/status/{status}")
    public ResponseEntity<List<PaymentResponse>> getMyPaymentsByStatus(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable PaymentStatus status) {
        try {
            String token = authHeader.replace("Bearer ", "");
            String email = jwtUtil.getSubject(token);
            
            if (email == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
            
            List<Payment> payments;
            
            if (user.getRole().name().equals("MERCHANT")) {
                payments = paymentService.getPaymentsByProviderAndStatus(user.getId(), status);
            } else {
                payments = paymentService.getPaymentsByUserAndStatus(user.getId(), status);
            }
            
            List<PaymentResponse> responses = payments.stream()
                    .map(PaymentResponse::fromEntity)
                    .toList();
            
            return ResponseEntity.ok(responses);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    // GET /api/payments/my-total - MI total de pagos aprobados
    @GetMapping("/my-total")
    public ResponseEntity<BigDecimal> getMyTotalAmount(
            @RequestHeader("Authorization") String authHeader) {
        try {
            String token = authHeader.replace("Bearer ", "");
            String email = jwtUtil.getSubject(token);
            
            if (email == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
            
            BigDecimal total;
            
            if (user.getRole().name().equals("MERCHANT")) {
                total = paymentService.getTotalAmountByProviderId(user.getId());
            } else {
                total = paymentService.getTotalAmountByUserId(user.getId());
            }
            
            return ResponseEntity.ok(total);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    // POST /api/payments/my-search - Buscar MIS pagos con filtros
    @PostMapping("/my-search")
    public ResponseEntity<PagedPaymentResponse> searchMyPayments(
            @RequestHeader("Authorization") String authHeader,
            @Valid @RequestBody PaymentSearchRequest request,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            String token = authHeader.replace("Bearer ", "");
            String email = jwtUtil.getSubject(token);
            
            if (email == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
            
            List<Payment> userPayments;
            
            if (user.getRole().name().equals("MERCHANT")) {
                userPayments = paymentService.getPaymentsByProviderId(user.getId());
            } else {
                userPayments = paymentService.getPaymentsByUserId(user.getId());
            }
            
            List<Payment> filteredPayments = userPayments.stream()
                .filter(payment -> {
                    boolean matches = true;
                    
                    if (request.getStatus() != null) {
                        matches = matches && payment.getStatus() == request.getStatus();
                    }
                    
                    if (request.getCurrency() != null && !request.getCurrency().isEmpty()) {
                        matches = matches && payment.getCurrency().equals(request.getCurrency());
                    }
                    
                    if (request.getMinAmount() != null) {
                        matches = matches && payment.getAmount_total().compareTo(request.getMinAmount()) >= 0;
                    }
                    
                    if (request.getMaxAmount() != null) {
                        matches = matches && payment.getAmount_total().compareTo(request.getMaxAmount()) <= 0;
                    }
                    
                    if (request.getStartDate() != null) {
                        matches = matches && !payment.getCreated_at().toLocalDate().isBefore(request.getStartDate());
                    }
                    
                    if (request.getEndDate() != null) {
                        matches = matches && !payment.getCreated_at().toLocalDate().isAfter(request.getEndDate());
                    }
                    
                    return matches;
                })
                .sorted((p1, p2) -> p2.getCreated_at().compareTo(p1.getCreated_at()))
                .collect(Collectors.toList());
            
            int start = page * size;
            int end = Math.min(start + size, filteredPayments.size());
            List<Payment> pageContent = start < filteredPayments.size() ? 
                filteredPayments.subList(start, end) : new ArrayList<>();
            
            List<PaymentResponse> responses = pageContent.stream()
                .map(PaymentResponse::fromEntity)
                .collect(Collectors.toList());
            
            PagedPaymentResponse pagedResponse = new PagedPaymentResponse();
            pagedResponse.setContent(responses);
            pagedResponse.setTotalElements(filteredPayments.size());
            pagedResponse.setTotalPages((int) Math.ceil((double) filteredPayments.size() / size));
            pagedResponse.setSize(size);
            pagedResponse.setNumber(page);
            pagedResponse.setFirst(page == 0);
            pagedResponse.setLast(page >= pagedResponse.getTotalPages() - 1);
            pagedResponse.setNumberOfElements(responses.size());
            
            return ResponseEntity.ok(pagedResponse);
        } catch (Exception e) {
            e.printStackTrace(); 
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
