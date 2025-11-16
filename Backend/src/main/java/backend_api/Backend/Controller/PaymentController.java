package backend_api.Backend.Controller;

import lombok.extern.slf4j.Slf4j;
import backend_api.Backend.Entity.payment.Payment;
import backend_api.Backend.Entity.payment.PaymentStatus;
import backend_api.Backend.Entity.payment.PaymentMethod;
import backend_api.Backend.Entity.payment.types.PaymentMethodType;
import backend_api.Backend.Service.Interface.PaymentService;
import backend_api.Backend.Service.Interface.PaymentMethodService;
import backend_api.Backend.Service.Interface.BalanceService;
import backend_api.Backend.DTO.payment.PaymentResponse;
import backend_api.Backend.DTO.payment.PagedPaymentResponse;
import backend_api.Backend.Service.Common.AuthenticationService;
import backend_api.Backend.Service.Common.EntityValidationService;
import backend_api.Backend.Service.Common.ResponseMapperService;
import backend_api.Backend.Entity.user.User;
import backend_api.Backend.Entity.UserData;
import backend_api.Backend.Repository.UserDataRepository;
import backend_api.Backend.Repository.UserRepository;
import backend_api.Backend.DTO.payment.CreatePaymentRequest;
import backend_api.Backend.DTO.payment.PaymentSearchRequest;
import backend_api.Backend.DTO.payment.SelectPaymentMethodRequest;
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
import java.util.stream.Collectors;
import java.util.ArrayList;
import jakarta.persistence.EntityNotFoundException;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@Slf4j
@RestController
@RequestMapping("/api/payments")
@CrossOrigin(origins = "*")
@Tag(name = "Pagos", description = "Endpoints para gestión de pagos, incluyendo creación, consulta, confirmación y selección de métodos de pago")
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    
    @Autowired
    private PaymentEventService paymentEventService;
    
    @Autowired
    private PaymentAttemptService paymentAttemptService;
    
    @Autowired
    private PaymentMethodService paymentMethodService;
    
    @Autowired
    private BalanceService balanceService;

    @Autowired
    private AuthenticationService authenticationService;

    @Autowired
    private EntityValidationService entityValidationService;

    @Autowired
    private ResponseMapperService responseMapperService;
    
    @Autowired
    private UserDataRepository userDataRepository;
    
    @Autowired
    private UserRepository userRepository;

    //  CREAR NUEVO PAGO 
    @Operation(
        summary = "Crear nuevo pago",
        description = "Crea un nuevo pago en el sistema. Requiere autenticación JWT válida.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @PostMapping
    public ResponseEntity<PaymentResponse> createPayment(
            @Valid @RequestBody CreatePaymentRequest request,
            @RequestHeader("Authorization") String authHeader) {
        try {
            User user = authenticationService.getUserFromToken(authHeader);
            if (user == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            
            BigDecimal total = request.getAmount_subtotal()
                                .add(request.getTaxes())
                                .add(request.getFees());
            
            if (user.getRole().name().equals("USER")) {
                if (!balanceService.hasSufficientBalance(user.getId(), total)) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                            .header("Error-Message", "Saldo insuficiente. Saldo disponible: " + 
                                    balanceService.getCurrentBalance(user.getId()) + 
                                    ", Monto requerido: " + total)
                            .build();
                }
            }
            
            Payment payment = new Payment();
            
            payment.setUser_id(user.getId());
            
            if (request.getProvider_id() != null) {
                payment.setProvider_id(request.getProvider_id());
            } else {
                // TODO: Buscar provider_id por provider_reference
                payment.setProvider_id(1L);
            } 
            
            payment.setAmount_subtotal(request.getAmount_subtotal());
            payment.setTaxes(request.getTaxes());
            payment.setFees(request.getFees());
            payment.setCurrency(request.getCurrency());
            payment.setMetadata(request.getMetadata());
            
            payment.setAmount_total(total);
            
            payment.setStatus(PaymentStatus.PENDING_PAYMENT);
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

        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }


    
   
    
    @GetMapping("/{paymentId}/timeline")
    public ResponseEntity<List<PaymentEvent>> getPaymentTimeline(
            @PathVariable Long paymentId,
            @RequestHeader("Authorization") String authHeader) {
        try {
            User currentUser = authenticationService.getUserFromToken(authHeader);
            if (currentUser == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            
            // Validar ownership (ADMIN tiene acceso a todos)
            entityValidationService.validatePaymentOwnership(paymentId, currentUser.getId(), currentUser.getRole().name());
            
            List<PaymentEvent> timeline = paymentEventService.getPaymentTimeline(paymentId);
            return ResponseEntity.ok(timeline);
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    // PUT /api/payments/{paymentId}/payment-method - Seleccionar método de pago
    @PutMapping("/{paymentId}/payment-method")
    public ResponseEntity<PaymentResponse> selectPaymentMethod(
            @PathVariable Long paymentId,
            @Valid @RequestBody SelectPaymentMethodRequest request) {
        try {
            Payment payment = entityValidationService.getPaymentOrThrow(paymentId);

            // Permitir cambiar método de pago si está en PENDING_PAYMENT o REJECTED (para reintentar con otro método)
            if (payment.getStatus() != PaymentStatus.PENDING_PAYMENT && 
                payment.getStatus() != PaymentStatus.REJECTED) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .header("Error-Message", "El pago no está en estado PENDING_PAYMENT o REJECTED")
                    .build();
            }
            
            // Si está REJECTED, resetear el estado a PENDING_PAYMENT para permitir reintentar
            if (payment.getStatus() == PaymentStatus.REJECTED) {
                log.info("🔄 Reintentando pago rechazado con nuevo método - PaymentId: {}", paymentId);
                payment.setStatus(PaymentStatus.PENDING_PAYMENT);
                payment.setRejected_by_balance(false);
                payment.setUpdated_at(LocalDateTime.now());
                // Actualizar el estado antes de cambiar el método de pago
                paymentService.updatePaymentStatus(paymentId, PaymentStatus.PENDING_PAYMENT);
            }

            PaymentMethod paymentMethod = paymentMethodService.createPaymentMethod(request);

            // Actualizar el método de pago (esto puede aprobar automáticamente para MercadoPago/Cash)
            paymentService.updatePaymentMethod(paymentId, paymentMethod);

            // Refrescar el pago desde la base de datos para asegurar que tenemos el estado más reciente
            Payment refreshedPayment = paymentService.getPaymentById(paymentId)
                .orElseThrow(() -> new RuntimeException("Pago no encontrado después de actualización"));

            log.info("✅ Método de pago seleccionado - PaymentId: {}, Status: {}, Method: {}", 
                paymentId, refreshedPayment.getStatus(), 
                refreshedPayment.getMethod() != null ? refreshedPayment.getMethod().getType() : "null");

            // Usar responseMapperService para obtener nombres de usuario/prestador y estado actualizado
            PaymentResponse response = responseMapperService.mapPaymentToResponse(refreshedPayment, "ADMIN");

            return ResponseEntity.ok(response);

        } catch (EntityNotFoundException e) {
            log.error("❌ Pago no encontrado - PaymentId: {}", paymentId);
            return ResponseEntity.notFound().build();
        } catch (RuntimeException e) {
            log.error("❌ Error al seleccionar método de pago - PaymentId: {}, Error: {}", paymentId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .header("Error-Message", "Error al seleccionar método de pago: " + e.getMessage())
                .build();
        } catch (Exception e) {
            log.error("❌ Error inesperado al seleccionar método de pago - PaymentId: {}, Error: {}", paymentId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .header("Error-Message", "Error inesperado al seleccionar método de pago")
                .build();
        }
    }
    
    
    @PutMapping("/{paymentId}/confirm")
    public ResponseEntity<PaymentResponse> confirmPayment(@PathVariable Long paymentId) {
        try {
            Payment payment = entityValidationService.getPaymentOrThrow(paymentId);
            
            // Permitir confirmar pagos que están en PENDING_PAYMENT o ya están APPROVED
            // (puede estar aprobado si se seleccionó MercadoPago/Cash que se aprueba automáticamente)
            if (payment.getStatus() == PaymentStatus.REJECTED) {
                // Si fue rechazado por saldo insuficiente, mostrar mensaje específico
                if (payment.getRejected_by_balance() != null && payment.getRejected_by_balance()) {
                    log.warn("⚠️ Intento de confirmar pago rechazado por saldo insuficiente - PaymentId: {}", paymentId);
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .header("Error-Message", "Saldo insuficiente para completar el pago")
                        .build();
                } else {
                    log.warn("⚠️ Intento de confirmar pago rechazado - PaymentId: {}, Status: {}", paymentId, payment.getStatus());
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .header("Error-Message", "El pago no puede ser confirmado. Estado actual: " + payment.getStatus())
                        .build();
                }
            }
            
            if (payment.getStatus() == PaymentStatus.PENDING_APPROVAL) {
                log.info("ℹ️ Pago en espera de aprobación bancaria - PaymentId: {}", paymentId);
                Payment refreshedPayment = paymentService.getPaymentById(paymentId)
                    .orElseThrow(() -> new RuntimeException("Pago no encontrado"));
                PaymentResponse response = responseMapperService.mapPaymentToResponse(refreshedPayment, "ADMIN");
                return ResponseEntity.status(HttpStatus.ACCEPTED)
                    .header("Info-Message", "Aguarde la aprobación pendiente del banco")
                    .body(response);
            }
            
            if (payment.getStatus() != PaymentStatus.PENDING_PAYMENT && 
                payment.getStatus() != PaymentStatus.APPROVED) {
                log.warn("⚠️ Intento de confirmar pago con estado inválido - PaymentId: {}, Status: {}", 
                    paymentId, payment.getStatus());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .header("Error-Message", "El pago no puede ser confirmado. Estado actual: " + payment.getStatus())
                    .build();
            }
            
            // Si ya está aprobado, solo retornar el estado actual sin procesar nuevamente
            if (payment.getStatus() == PaymentStatus.APPROVED) {
                log.info("✅ Pago ya está aprobado - PaymentId: {}, retornando estado actual", paymentId);
                Payment refreshedPayment = paymentService.getPaymentById(paymentId)
                    .orElseThrow(() -> new RuntimeException("Pago no encontrado"));
                PaymentResponse response = responseMapperService.mapPaymentToResponse(refreshedPayment, "ADMIN");
                return ResponseEntity.ok(response);
            }
            
            if (payment.getMethod() == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .header("Error-Message", "Debe seleccionar un método de pago primero")
                        .build();
            }
            
            Payment updatedPayment;
            PaymentEventType eventType;
            
            if (payment.getMethod().getType() == PaymentMethodType.CREDIT_CARD || 
                payment.getMethod().getType() == PaymentMethodType.DEBIT_CARD ||
                payment.getMethod().getType() == PaymentMethodType.BANK_TRANSFER) {
                // Para tarjetas, NO verificar saldo (las tarjetas no usan saldo disponible)
                // Solo cambiar a PENDING_APPROVAL y esperar aprobación bancaria
                updatedPayment = paymentService.updatePaymentStatus(paymentId, PaymentStatus.PENDING_APPROVAL);
                eventType = PaymentEventType.PAYMENT_PENDING;
                
                paymentEventService.createEvent(
                    paymentId,
                    eventType,
                    "{\"status\": \"pending_bank_approval\", \"method\": \"" + payment.getMethod().getType() + "\"}",
                    "system"
                );
                
                log.info("✅ Pago con tarjeta enviado a aprobación bancaria - PaymentId: {}, Method: {}", 
                    paymentId, payment.getMethod().getType());
            } else {
                // Solo MercadoPago y CASH verifican saldo disponible
                // Consultar primero en user_data para verificar el rol
                java.util.Optional<UserData> userDataOpt = userDataRepository.findByUserId(payment.getUser_id());
                String userRole = null;
                
                if (userDataOpt.isPresent()) {
                    userRole = userDataOpt.get().getRole();
                    log.info("🔍 Rol obtenido desde user_data - UserId: {}, Role: {}", payment.getUser_id(), userRole);
                } else {
                    // Fallback a users si no existe en user_data
                    User user = userRepository.findById(payment.getUser_id())
                        .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
                    userRole = user.getRole().name();
                    log.info("🔍 Rol obtenido desde users (fallback) - UserId: {}, Role: {}", payment.getUser_id(), userRole);
                }
                
                // Verificar si es CLIENTE o USER (ambos deben descontar balance)
                if (userRole != null && (userRole.equalsIgnoreCase("USER") || userRole.equalsIgnoreCase("CLIENTE"))) {
                    try {
                        balanceService.deductBalance(payment.getUser_id(), payment.getAmount_total());
                        log.info("✅ Balance descontado exitosamente - UserId: {}, Amount: {}", 
                            payment.getUser_id(), payment.getAmount_total());
                    } catch (IllegalStateException e) {
                        payment.setStatus(PaymentStatus.REJECTED);
                        payment.setRejected_by_balance(true);
                        payment.setUpdated_at(LocalDateTime.now());
                        paymentService.createPayment(payment); // Actualizar
                        
                        paymentEventService.createEvent(
                            paymentId,
                            PaymentEventType.PAYMENT_REJECTED,
                            "{\"status\": \"rejected_insufficient_balance\", \"method\": \"" + payment.getMethod().getType() + "\"}",
                            "system"
                        );
                        
                        log.warn("⚠️ Pago rechazado por saldo insuficiente - PaymentId: {}, UserId: {}", 
                            paymentId, payment.getUser_id());
                        
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                .header("Error-Message", "Saldo insuficiente para completar el pago")
                                .build();
                    }
                }
                
                updatedPayment = paymentService.updatePaymentStatus(paymentId, PaymentStatus.APPROVED);
                eventType = PaymentEventType.PAYMENT_APPROVED;
                
                paymentEventService.createEvent(
                    paymentId,
                    eventType,
                    "{\"status\": \"approved_directly\", \"method\": \"" + payment.getMethod().getType() + "\"}",
                    "system"
                );
            }
            
            // Refrescar el pago desde la base de datos para asegurar que tenemos el estado más reciente
            Payment refreshedPayment = paymentService.getPaymentById(paymentId)
                .orElseThrow(() -> new RuntimeException("Pago no encontrado después de actualización"));
            
            log.info("✅ Pago confirmado exitosamente - PaymentId: {}, Status: {}, Method: {}", 
                paymentId, refreshedPayment.getStatus(), 
                refreshedPayment.getMethod() != null ? refreshedPayment.getMethod().getType() : "null");
            
            // Usar responseMapperService para obtener nombres de usuario/prestador
            PaymentResponse response = responseMapperService.mapPaymentToResponse(refreshedPayment, "ADMIN");
            
            return ResponseEntity.ok(response);
            
        } catch (EntityNotFoundException e) {
            log.error("❌ Pago no encontrado - PaymentId: {}", paymentId);
            return ResponseEntity.notFound().build();
        } catch (RuntimeException e) {
            log.error("❌ Error al confirmar pago - PaymentId: {}, Error: {}", paymentId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .header("Error-Message", "Error al confirmar el pago: " + e.getMessage())
                .build();
        } catch (Exception e) {
            log.error("❌ Error inesperado al confirmar pago - PaymentId: {}, Error: {}", paymentId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .header("Error-Message", "Error inesperado al confirmar el pago")
                .build();
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
    
    
    // Reintentar pago rechazado por saldo insuficiente
    @PostMapping("/{paymentId}/retry-balance")
    public ResponseEntity<PaymentResponse> retryPaymentByBalance(
            @PathVariable Long paymentId,
            @RequestHeader("Authorization") String authHeader) {
        try {
            User user = authenticationService.getUserFromToken(authHeader);
            if (user == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            Payment payment = entityValidationService.getPaymentOrThrow(paymentId);

            if (!payment.getUser_id().equals(user.getId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            
            // Verificar que puede ser reintentado (solo por saldo insuficiente)
            if (!balanceService.canRetryPayment(paymentId)) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .header("Error-Message", "Este pago no puede ser reintentado. Solo pagos rechazados por saldo insuficiente con menos de 3 intentos.")
                        .build();
            }
            
            // Verificar saldo actual
            if (!balanceService.hasSufficientBalance(user.getId(), payment.getAmount_total())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .header("Error-Message", "Saldo insuficiente para reintentar el pago. Saldo disponible: " + 
                                balanceService.getCurrentBalance(user.getId()))
                        .build();
            }
            
            // Reintentar el pago
            payment.setStatus(PaymentStatus.PENDING_PAYMENT);
            payment.setRejected_by_balance(false);
            payment.setRetry_attempts(payment.getRetry_attempts() + 1);
            payment.setUpdated_at(LocalDateTime.now());
            
            Payment updatedPayment = paymentService.createPayment(payment);
            
            paymentEventService.createEvent(
                paymentId,
                PaymentEventType.PAYMENT_PENDING,
                "{\"status\": \"retry_attempt\", \"attempt\": " + payment.getRetry_attempts() + ", \"reason\": \"balance_retry\"}",
                "user_" + user.getId()
            );
            
            return ResponseEntity.ok(PaymentResponse.fromEntity(updatedPayment));

        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{paymentId}")
    public ResponseEntity<PaymentResponse> getPaymentById(
            @PathVariable Long paymentId,
            @RequestHeader("Authorization") String authHeader) {
        try {
            User currentUser = authenticationService.getUserFromToken(authHeader);
            if (currentUser == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            Payment payment = entityValidationService.getPaymentOrThrow(paymentId);

            entityValidationService.validatePaymentOwnership(paymentId, currentUser.getId(), currentUser.getRole().name());
            
            PaymentResponse response = responseMapperService.mapPaymentToResponse(payment, currentUser.getRole().name());

            return ResponseEntity.ok(response);
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    
   // GET /api/payments/my-payments - Obtener MIS pagos usando el token
    @GetMapping("/my-payments")
    public ResponseEntity<List<PaymentResponse>> getMyPayments(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) { // Aumentado a 50 por defecto
        try {
            User user = authenticationService.getUserFromToken(authHeader);
            if (user == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            log.info("🔍 Buscando pagos para usuario - ID: {}, Email: {}, Role: {}, page: {}, size: {}", 
                user.getId(), user.getEmail(), user.getRole().name(), page, size);

            List<Payment> payments;

            if (user.getRole().name().equals("ADMIN")) {
                // Para ADMIN, traer todos los pagos con paginación
                payments = paymentService.getAllPayments(page, size);
                log.info("📊 Pagos encontrados para ADMIN: {} (mostrando página {})", payments.size(), page);
            } else if (user.getRole().name().equals("MERCHANT")) {
                payments = paymentService.getPaymentsByProviderId(user.getId(), page, size);
                log.info("📊 Pagos encontrados para MERCHANT (providerId: {}): {}", user.getId(), payments.size());
            } else {
                payments = paymentService.getPaymentsByUserId(user.getId(), page, size);
                log.info("📊 Pagos encontrados para USER (userId: {}): {}", user.getId(), payments.size());
            }

            List<PaymentResponse> responses = responseMapperService.mapPaymentsToResponses(payments, user.getRole().name());

            return ResponseEntity.ok(responses);
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Error en getMyPayments: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    // GET /api/payments/my-payments/status/{status} - MIS pagos por estado
    @GetMapping("/my-payments/status/{status}")
    public ResponseEntity<List<PaymentResponse>> getMyPaymentsByStatus(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable PaymentStatus status) {
        try {
            User user = authenticationService.getUserFromToken(authHeader);
            if (user == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            List<Payment> payments;

            if (user.getRole().name().equals("ADMIN")) {
                payments = paymentService.getPaymentsByStatus(status);
            } else if (user.getRole().name().equals("MERCHANT")) {
                payments = paymentService.getPaymentsByProviderAndStatus(user.getId(), status);
            } else {
                payments = paymentService.getPaymentsByUserAndStatus(user.getId(), status);
            }

            List<PaymentResponse> responses = responseMapperService.mapPaymentsToResponses(payments, user.getRole().name());

            return ResponseEntity.ok(responses);
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    // GET /api/payments/my-total - MI total de pagos aprobados
    @GetMapping("/my-total")
    public ResponseEntity<BigDecimal> getMyTotalAmount(
            @RequestHeader("Authorization") String authHeader) {
        try {
            User user = authenticationService.getUserFromToken(authHeader);
            if (user == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            BigDecimal total;

            if (user.getRole().name().equals("ADMIN")) {
                total = paymentService.getTotalAmountAllApprovedPayments();
            } else if (user.getRole().name().equals("MERCHANT")) {
                total = paymentService.getTotalAmountByProviderId(user.getId());
            } else {
                total = paymentService.getTotalAmountByUserId(user.getId());
            }

            return ResponseEntity.ok(total);
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    // GET /api/payments/my-balance - MI saldo disponible (solo usuarios)
    @GetMapping("/my-balance")
    public ResponseEntity<BigDecimal> getMyBalance(
            @RequestHeader("Authorization") String authHeader) {
        try {
            User user = authenticationService.getUserFromToken(authHeader);
            if (user == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            // Solo usuarios tienen saldo, merchants no
            if (user.getRole().name().equals("MERCHANT")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .header("Error-Message", "Los merchants no tienen saldo disponible")
                        .build();
            }

            BigDecimal balance = balanceService.getCurrentBalance(user.getId());
            return ResponseEntity.ok(balance);
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
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
            User user = authenticationService.getUserFromToken(authHeader);
            if (user == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            
            List<Payment> userPayments;
            
            if (user.getRole().name().equals("ADMIN")) {
                userPayments = paymentService.getAllPayments();
            } else if (user.getRole().name().equals("MERCHANT")) {
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
            
            List<PaymentResponse> responses = responseMapperService.mapPaymentsToResponses(pageContent, user.getRole().name());
            
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
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
