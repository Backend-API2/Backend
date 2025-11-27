package backend_api.Backend.Service.Implementation;

import backend_api.Backend.Entity.payment.Payment;
import backend_api.Backend.Entity.payment.PaymentStatus;
import backend_api.Backend.Entity.payment.PaymentEventType;
import backend_api.Backend.Entity.payment.types.PaymentMethodType;
import backend_api.Backend.Entity.UserData;
import backend_api.Backend.Entity.user.User;
import backend_api.Backend.Service.Interface.PaymentService;
import backend_api.Backend.Service.Interface.PaymentEventService;
import backend_api.Backend.Service.Interface.BalanceService;
import backend_api.Backend.Repository.UserDataRepository;
import backend_api.Backend.Repository.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class PaymentApprovalScheduler {

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private PaymentEventService paymentEventService;

    @Autowired
    private BalanceService balanceService;

    @Autowired
    private UserDataRepository userDataRepository;

    @Autowired
    private UserRepository userRepository;

    private static final int APPROVAL_DELAY_SECONDS = 60;

    /**
     * Ejecuta cada 30 segundos para revisar pagos pendientes de aprobación
     * que ya han pasado el tiempo de espera simulado
     * Disabled during tests to prevent stack overflow
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

            System.out.println(
                    "DEBUG - PaymentApprovalScheduler: Encontrados " + pendingPayments.size() + " pagos pendientes");

            LocalDateTime now = LocalDateTime.now();

            for (Payment payment : pendingPayments) {
                if (requiresBankApproval(payment)) {
                    LocalDateTime approvalDeadline = payment.getUpdated_at().plusSeconds(APPROVAL_DELAY_SECONDS);
                    long secondsUntilApproval = java.time.Duration.between(now, approvalDeadline).getSeconds();

                    System.out.println("DEBUG - PaymentApprovalScheduler: Payment ID " + payment.getId() +
                            " - Updated at: " + payment.getUpdated_at() +
                            " - Deadline: " + approvalDeadline +
                            " - Seconds until approval: " + secondsUntilApproval);

                    if (now.isAfter(approvalDeadline) || now.isEqual(approvalDeadline)) {
                        System.out.println("DEBUG - PaymentApprovalScheduler: Aprobando automáticamente payment ID: "
                                + payment.getId());

                        // Aprobar pago automáticamente (sin porcentaje de rechazo)
                        approvePayment(payment);
                    } else {
                        System.out.println("DEBUG - PaymentApprovalScheduler: Payment ID " + payment.getId() +
                                " aún no cumple el delay. Esperando " + secondsUntilApproval + " segundos más.");
                    }
                } else {
                    System.out.println("DEBUG - PaymentApprovalScheduler: Payment ID " + payment.getId() +
                            " no requiere aprobación bancaria (método: " +
                            (payment.getMethod() != null ? payment.getMethod().getType() : "null") + ")");
                }
            }

        } catch (Exception e) {
            System.err.println("ERROR en PaymentApprovalScheduler: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private boolean requiresBankApproval(Payment payment) {
        if (payment.getMethod() == null)
            return false;

        PaymentMethodType type = payment.getMethod().getType();
        return type == PaymentMethodType.CREDIT_CARD ||
                type == PaymentMethodType.DEBIT_CARD ||
                type == PaymentMethodType.BANK_TRANSFER;
    }

    private void approvePayment(Payment payment) {
        try {
            // IMPORTANTE: Las tarjetas de crédito/débito y transferencias bancarias NO usan
            // saldo disponible
            // Solo MercadoPago y CASH requieren saldo disponible
            if (payment.getMethod() != null) {
                PaymentMethodType methodType = payment.getMethod().getType();

                // Solo descontar saldo para MercadoPago y CASH
                if (methodType == PaymentMethodType.MERCADO_PAGO || methodType == PaymentMethodType.CASH) {
                    // Descontar balance si el usuario es CLIENTE o USER
                    Optional<UserData> userDataOpt = userDataRepository.findByUserId(payment.getUser_id());
                    String userRole = null;

                    if (userDataOpt.isPresent()) {
                        userRole = userDataOpt.get().getRole();
                    } else {
                        // Fallback a users si no existe en user_data
                        Optional<User> userOpt = userRepository.findById(payment.getUser_id());
                        if (userOpt.isPresent()) {
                            userRole = userOpt.get().getRole().name();
                        }
                    }

                    // Verificar si es CLIENTE o USER (ambos deben descontar balance)
                    if (userRole != null
                            && (userRole.equalsIgnoreCase("USER") || userRole.equalsIgnoreCase("CLIENTE"))) {
                        try {
                            balanceService.deductBalance(payment.getUser_id(), payment.getAmount_total());
                            System.out.println("✅ Balance descontado exitosamente - UserId: " + payment.getUser_id()
                                    + ", Amount: " + payment.getAmount_total());
                        } catch (IllegalStateException e) {
                            // Saldo insuficiente - rechazar pago
                            paymentService.updatePaymentStatus(payment.getId(), PaymentStatus.REJECTED);

                            paymentEventService.createEvent(
                                    payment.getId(),
                                    PaymentEventType.PAYMENT_REJECTED,
                                    String.format("{\"status\": \"rejected_insufficient_balance\", \"method\": \"%s\"}",
                                            payment.getMethod().getType()),
                                    "bank_simulator");

                            System.out.println("⚠️ Pago rechazado por saldo insuficiente - PaymentId: "
                                    + payment.getId() + ", UserId: " + payment.getUser_id());
                            return;
                        }
                    }
                } else {
                    // Para tarjetas y transferencias bancarias, NO descontar saldo
                    System.out.println("💳 Pago con tarjeta/transferencia - NO se descuenta saldo - PaymentId: "
                            + payment.getId() + ", Method: " + methodType);
                }
            }

            paymentService.updatePaymentStatus(payment.getId(), PaymentStatus.APPROVED);

            paymentEventService.createEvent(
                    payment.getId(),
                    PaymentEventType.PAYMENT_APPROVED,
                    String.format(
                            "{\"status\": \"auto_approved_by_bank\", \"method\": \"%s\", \"approval_time\": \"%s\"}",
                            payment.getMethod().getType(), LocalDateTime.now()),
                    "bank_simulator");

            System.out.println("✅ Payment ID " + payment.getId() + " aprobado automáticamente por el banco");

        } catch (Exception e) {
            System.err.println("ERROR aprobando payment ID " + payment.getId() + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void rejectPayment(Payment payment) {
        try {
            paymentService.updatePaymentStatus(payment.getId(), PaymentStatus.REJECTED);

            paymentEventService.createEvent(
                    payment.getId(),
                    PaymentEventType.PAYMENT_REJECTED,
                    String.format(
                            "{\"status\": \"rejected_by_bank\", \"method\": \"%s\", \"rejection_reason\": \"Insufficient funds\", \"rejection_time\": \"%s\"}",
                            payment.getMethod().getType(), LocalDateTime.now()),
                    "bank_simulator");

            System.out.println("❌ Payment ID " + payment.getId() + " rechazado automáticamente por el banco");

        } catch (Exception e) {
            System.err.println("ERROR rechazando payment ID " + payment.getId() + ": " + e.getMessage());
        }
    }
}
