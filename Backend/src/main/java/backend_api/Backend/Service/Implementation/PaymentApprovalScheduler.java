package backend_api.Backend.Service.Implementation;

import backend_api.Backend.Entity.payment.Payment;
import backend_api.Backend.Entity.payment.PaymentStatus;
import backend_api.Backend.Entity.payment.PaymentEventType;
import backend_api.Backend.Entity.payment.types.PaymentMethodType;
import backend_api.Backend.Service.Interface.PaymentService;
import backend_api.Backend.Service.Interface.PaymentEventService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class PaymentApprovalScheduler {
    
    @Autowired
    private PaymentService paymentService;
    
    @Autowired
    private PaymentEventService paymentEventService;
    
  
    private static final int APPROVAL_DELAY_SECONDS = 60; 
    
    /**
     * Ejecuta cada 30 segundos para revisar pagos pendientes de aprobación
     * que ya han pasado el tiempo de espera simulado
     */
    @Scheduled(fixedDelay = 30000) 
    public void processAutomaticApprovals() {
        try {
            System.out.println("DEBUG - PaymentApprovalScheduler: Revisando pagos pendientes de aprobación...");
            
            List<Payment> pendingPayments = paymentService.getPaymentsByStatus(PaymentStatus.PENDING_APPROVAL);
            
            if (pendingPayments.isEmpty()) {
                System.out.println("DEBUG - PaymentApprovalScheduler: No hay pagos pendientes");
                return;
            }
            
            System.out.println("DEBUG - PaymentApprovalScheduler: Encontrados " + pendingPayments.size() + " pagos pendientes");
            
            LocalDateTime now = LocalDateTime.now();
            
            for (Payment payment : pendingPayments) {
                if (requiresBankApproval(payment)) {
                    
                    LocalDateTime approvalDeadline = payment.getUpdated_at().plusSeconds(APPROVAL_DELAY_SECONDS);
                    
                    if (now.isAfter(approvalDeadline)) {
                        System.out.println("DEBUG - PaymentApprovalScheduler: Aprobando automáticamente payment ID: " + payment.getId());
                        
                        // Simular aprobación bancaria (90% aprobado, 10% rechazado para realismo)
                        boolean approved = Math.random() > 0.1; 
                        
                        if (approved) {
                            approvePayment(payment);
                        } else {
                            rejectPayment(payment);
                        }
                    }
                }
            }
            
        } catch (Exception e) {
            System.err.println("ERROR en PaymentApprovalScheduler: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private boolean requiresBankApproval(Payment payment) {
        if (payment.getMethod() == null) return false;
        
        PaymentMethodType type = payment.getMethod().getType();
        return type == PaymentMethodType.CREDIT_CARD || 
               type == PaymentMethodType.DEBIT_CARD || 
               type == PaymentMethodType.BANK_TRANSFER;
    }
    
    private void approvePayment(Payment payment) {
        try {
            paymentService.updatePaymentStatus(payment.getId(), PaymentStatus.APPROVED);
            
            paymentEventService.createEvent(
                payment.getId(),
                PaymentEventType.PAYMENT_APPROVED,
                String.format("{\"status\": \"auto_approved_by_bank\", \"method\": \"%s\", \"approval_time\": \"%s\"}", 
                    payment.getMethod().getType(), LocalDateTime.now()),
                "bank_simulator"
            );
            
            System.out.println("✅ Payment ID " + payment.getId() + " aprobado automáticamente por el banco");
            
        } catch (Exception e) {
            System.err.println("ERROR aprobando payment ID " + payment.getId() + ": " + e.getMessage());
        }
    }
    
    private void rejectPayment(Payment payment) {
        try {
            paymentService.updatePaymentStatus(payment.getId(), PaymentStatus.REJECTED);
            
            paymentEventService.createEvent(
                payment.getId(),
                PaymentEventType.PAYMENT_REJECTED,
                String.format("{\"status\": \"rejected_by_bank\", \"method\": \"%s\", \"rejection_reason\": \"Insufficient funds\", \"rejection_time\": \"%s\"}", 
                    payment.getMethod().getType(), LocalDateTime.now()),
                "bank_simulator"
            );
            
            System.out.println("❌ Payment ID " + payment.getId() + " rechazado automáticamente por el banco");
            
        } catch (Exception e) {
            System.err.println("ERROR rechazando payment ID " + payment.getId() + ": " + e.getMessage());
        }
    }
}
