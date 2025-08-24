package backend_api.Backend.Controller;

import backend_api.Backend.Entity.payment.Payment;
import backend_api.Backend.Entity.payment.PaymentStatus;
import backend_api.Backend.Service.Interface.PaymentService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/payments")
@CrossOrigin(origins = "*")
public class PaymentController {

    @Autowired
    private PaymentService paymentService;


    //Crear nuevo pago
    @PostMapping
    public ResponseEntity<?> createPayment(@RequestBody Payment payment) {
        try {
            Payment createdPayment = paymentService.createPayment(payment);
            return new ResponseEntity<>(createdPayment, HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    //Obtener todos los pagos
    @GetMapping("/all")
    public ResponseEntity<List<Payment>> getAllPayments() {
        try {
            List<Payment> payments = paymentService.getAllPayments();
            if (payments.isEmpty()){
                return new ResponseEntity<>(HttpStatus.NO_CONTENT);
            }
            return new ResponseEntity<>(payments, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    

    //Obtener pago por id
    @GetMapping("/{id}")
    public ResponseEntity<?> getPaymentById(@PathVariable Long id) {
        try {
            Optional<Payment> payment = paymentService.getPaymentById(id);
            if (payment.isPresent()){
                return new ResponseEntity<>(payment.get(), HttpStatus.OK);
            } else {
                return new ResponseEntity <>(HttpStatus.NOT_FOUND);
            }
        }catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
 

       //Obtener pago por Payment_ID
       @GetMapping("/intent/{paymentIntentId}")
       public ResponseEntity<Payment> getPaymentByIntentId(@PathVariable String paymentIntentId) {
           try {
               Optional<Payment> payment = paymentService.getPaymentByIntentId(paymentIntentId);
               if (payment.isPresent()) {
                   return new ResponseEntity<>(payment.get(), HttpStatus.OK);
               } else {
                   return new ResponseEntity<>(HttpStatus.NOT_FOUND);
               }
           } catch (Exception e) {
               return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
           }
       }
   
       //Obtener pagos por User_Id
       @GetMapping("/user/{userId}")
       public ResponseEntity<List<Payment>> getPaymentsByUserId(@PathVariable Long userId) {
           try {
               List<Payment> payments = paymentService.getPaymentsByUserId(userId);
               if (payments.isEmpty()) {
                   return new ResponseEntity<>(HttpStatus.NO_CONTENT);
               }
               return new ResponseEntity<>(payments, HttpStatus.OK);
           } catch (Exception e) {
               return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
           }
       }
   
       //Obtener pagos por Provider_ID
       @GetMapping("/provider/{providerId}")
       public ResponseEntity<List<Payment>> getPaymentsByProviderId(@PathVariable Long providerId) {
           try {
               List<Payment> payments = paymentService.getPaymentsByProviderId(providerId);
               if (payments.isEmpty()) {
                   return new ResponseEntity<>(HttpStatus.NO_CONTENT);
               }
               return new ResponseEntity<>(payments, HttpStatus.OK);
           } catch (Exception e) {
               return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
           }
       }
   
       // Obtener pagos por Status
       @GetMapping("/status/{status}")
       public ResponseEntity<List<Payment>> getPaymentsByStatus(@PathVariable PaymentStatus status) {
           try {
               List<Payment> payments = paymentService.getPaymentsByStatus(status);
               if (payments.isEmpty()) {
                   return new ResponseEntity<>(HttpStatus.NO_CONTENT);
               }
               return new ResponseEntity<>(payments, HttpStatus.OK);
           } catch (Exception e) {
               return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
           }
       }
   
       //Obtener pagos por Gateway_ID
       @GetMapping("/gateway/{gatewayTxnId}")
       public ResponseEntity<Payment> getPaymentByGatewayTxnId(@PathVariable String gatewayTxnId) {
           try {
               Optional<Payment> payment = paymentService.getPaymentsByGatewayTxnId(gatewayTxnId);
               if (payment.isPresent()) {
                   return new ResponseEntity<>(payment.get(), HttpStatus.OK);
               } else {
                   return new ResponseEntity<>(HttpStatus.NOT_FOUND);
               }
           } catch (Exception e) {
               return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
           }
       }
   
       //Obtener pagos por Solicitud_ID
       @GetMapping("/solicitud/{solicitudId}")
       public ResponseEntity<List<Payment>> getPaymentsBySolicitudId(@PathVariable Long solicitudId) {
           try {
               List<Payment> payments = paymentService.getPaymentsBySolicitudId(solicitudId);
               if (payments.isEmpty()) {
                   return new ResponseEntity<>(HttpStatus.NO_CONTENT);
               }
               return new ResponseEntity<>(payments, HttpStatus.OK);
           } catch (Exception e) {
               return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
           }
       }
   
       //Obtener pagos por Cotización_ID
       @GetMapping("/cotizacion/{cotizacionId}")
       public ResponseEntity<List<Payment>> getPaymentsByCotizacionId(@PathVariable Long cotizacionId) {
           try {
               List<Payment> payments = paymentService.getPaymentsByCotizacionId(cotizacionId);
               if (payments.isEmpty()) {
                   return new ResponseEntity<>(HttpStatus.NO_CONTENT);
               }
               return new ResponseEntity<>(payments, HttpStatus.OK);
           } catch (Exception e) {
               return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
           }
       }
   
       //Obtener pagos por monto mínimo
       @GetMapping("/amount/{minAmount}")
       public ResponseEntity<List<Payment>> getPaymentsByAmountGreaterThan(@PathVariable BigDecimal minAmount) {
           try {
               List<Payment> payments = paymentService.getPaymentsByAmountGreaterThan(minAmount);
               if (payments.isEmpty()) {
                   return new ResponseEntity<>(HttpStatus.NO_CONTENT);
               }
               return new ResponseEntity<>(payments, HttpStatus.OK);
           } catch (Exception e) {
               return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
           }
       }
   
       //Obtener pagos por rango de fechas
       @GetMapping("/date-range")
       public ResponseEntity<List<Payment>> getPaymentsByDateRange(
               @RequestParam String startDate, 
               @RequestParam String endDate) {
           try {
               LocalDateTime start = LocalDateTime.parse(startDate);
               LocalDateTime end = LocalDateTime.parse(endDate);
               List<Payment> payments = paymentService.getPaymentsByDateRange(start, end);
               if (payments.isEmpty()) {
                   return new ResponseEntity<>(HttpStatus.NO_CONTENT);
               }
               return new ResponseEntity<>(payments, HttpStatus.OK);
           } catch (Exception e) {
               return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
           }
       }
   
       //Obtener pagos por user_id y status
       @GetMapping("/user/{userId}/status/{status}")
       public ResponseEntity<List<Payment>> getPaymentsByUserAndStatus(
               @PathVariable Long userId, 
               @PathVariable PaymentStatus status) {
           try {
               List<Payment> payments = paymentService.getPaymentsByUserAndStatus(userId, status);
               if (payments.isEmpty()) {
                   return new ResponseEntity<>(HttpStatus.NO_CONTENT);
               }
               return new ResponseEntity<>(payments, HttpStatus.OK);
           } catch (Exception e) {
               return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
           }
       }
   
       //Obtener pagos por moneda
       @GetMapping("/currency/{currency}")
       public ResponseEntity<List<Payment>> getPaymentsByCurrency(@PathVariable String currency) {
           try {
               List<Payment> payments = paymentService.getPaymentsByCurrency(currency);
               if (payments.isEmpty()) {
                   return new ResponseEntity<>(HttpStatus.NO_CONTENT);
               }
               return new ResponseEntity<>(payments, HttpStatus.OK);
           } catch (Exception e) {
               return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
           }
       }
   
       //Actualizar pago completo
       @PutMapping("/{id}")
       public ResponseEntity<Payment> updatePayment(@PathVariable Long id, @RequestBody Payment payment) {
           try {
               Payment updatedPayment = paymentService.updatePayment(id, payment);
               return new ResponseEntity<>(updatedPayment, HttpStatus.OK);
           } catch (RuntimeException e) {
               return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
           } catch (Exception e) {
               return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
           }
       }
   
       //Actualizar solo el status del pago
       @PatchMapping("/{id}/status")
       public ResponseEntity<Payment> updatePaymentStatus(@PathVariable Long id, @RequestBody PaymentStatus status) {
           try {
               Payment updatedPayment = paymentService.updatePaymentStatus(id, status);
               return new ResponseEntity<>(updatedPayment, HttpStatus.OK);
           } catch (RuntimeException e) {
               return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
           } catch (Exception e) {
               return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
           }
       }
   
       //Eliminar pago
       @DeleteMapping("/{id}")
       public ResponseEntity<HttpStatus> deletePayment(@PathVariable Long id) {
           try {
               paymentService.deletePayment(id);
               return new ResponseEntity<>(HttpStatus.NO_CONTENT);
           } catch (RuntimeException e) {
               return new ResponseEntity<>(HttpStatus.NOT_FOUND);
           } catch (Exception e) {
               return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
           }
       }
   
       //Verificar si existe un pago
       @GetMapping("/{id}/exists")
       public ResponseEntity<Boolean> existsById(@PathVariable Long id) {
           try {
               boolean exists = paymentService.existsById(id);
               return new ResponseEntity<>(exists, HttpStatus.OK);
           } catch (Exception e) {
               return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
           }
       }
   
       //Contar pagos por status
       @GetMapping("/count/status/{status}")
       public ResponseEntity<Long> countPaymentsByStatus(@PathVariable PaymentStatus status) {
           try {
               long count = paymentService.countPaymentsByStatus(status);
               return new ResponseEntity<>(count, HttpStatus.OK);
           } catch (Exception e) {
               return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
           }
       }
   
       //Obtener total amount por usuario
       @GetMapping("/user/{userId}/total")
       public ResponseEntity<BigDecimal> getTotalAmountByUserId(@PathVariable Long userId) {
           try {
               BigDecimal total = paymentService.getTotalAmountByUserId(userId);
               return new ResponseEntity<>(total, HttpStatus.OK);
           } catch (Exception e) {
               return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
           }
       }
   }