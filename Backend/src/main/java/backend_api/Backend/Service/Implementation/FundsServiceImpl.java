package backend_api.Backend.Service.Implementation;

import backend_api.Backend.Entity.payment.Payment;
import backend_api.Backend.Entity.payment.PaymentEventType;
import backend_api.Backend.Entity.payment.PaymentStatus;
import backend_api.Backend.Entity.payment.types.CreditCardPayment;
import backend_api.Backend.Entity.payment.types.DebitCardPayment;
import backend_api.Backend.Repository.TestCardRepository;
import backend_api.Backend.Service.Interface.FundsService;
import backend_api.Backend.Service.Interface.PaymentAttemptService;
import backend_api.Backend.Service.Interface.PaymentEventService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Transactional
public class FundsServiceImpl implements FundsService {

    private final TestCardRepository testCardRepo;
    private final PaymentEventService paymentEventService;
    private final PaymentAttemptService paymentAttemptService;

    @Override
    public void debitForPayment(Payment payment) {
        if (payment == null) throw new IllegalArgumentException("payment no puede ser null");
        if (payment.getMethod() == null) throw new IllegalStateException("El pago no tiene método seleccionado");

        BigDecimal amount = nvl(payment.getAmount_total());
        String currency = payment.getCurrency() != null ? payment.getCurrency() : "ARS";

        paymentEventService.createEvent(
                payment.getId(),
                PaymentEventType.AUTHORIZATION_STARTED,
                "{\"amount\":\"" + amount + "\",\"currency\":\"" + currency + "\"}",
                "system"
        );

        // ——— saldo local para TestCard (CREDIT/DEBIT) ———
        if (payment.getMethod() instanceof CreditCardPayment cc && cc.getTestCard() != null) {
            int updated = testCardRepo.debitIfEnough(cc.getTestCard().getId(), amount);
            if (updated <= 0) {
                fail(payment, "insufficient_funds", "Fondos insuficientes en TestCard");
            }
            ok(payment, amount);
            return;
        }

        if (payment.getMethod() instanceof DebitCardPayment dc && dc.getTestCard() != null) {
            int updated = testCardRepo.debitIfEnough(dc.getTestCard().getId(), amount);
            if (updated <= 0) {
                fail(payment, "insufficient_funds", "Fondos insuficientes en TestCard");
            }
            ok(payment, amount);
            return;
        }

        // Otros métodos (transferencia, MP, wallet) - por ahora aprobar sin saldo local
        paymentEventService.createEvent(
                payment.getId(),
                PaymentEventType.AUTHORIZATION_SUCCEEDED,
                "{\"note\":\"method_without_local_balance\"}",
                "gateway"
        );
        paymentAttemptService.createAttempt(
                payment.getId(),
                PaymentStatus.APPROVED,
                "success",
                "approved"
        );
    }

    @Override
    public void creditForRefund(Payment payment, BigDecimal amount) {
        if (payment == null) throw new IllegalArgumentException("payment no puede ser null");
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("amount de refund inválido");
        }
        if (payment.getMethod() == null) return;

        // Solo revertimos sobre TestCard si corresponde
        if (payment.getMethod() instanceof CreditCardPayment cc && cc.getTestCard() != null) {
            testCardRepo.credit(cc.getTestCard().getId(), amount);
        } else if (payment.getMethod() instanceof DebitCardPayment dc && dc.getTestCard() != null) {
            testCardRepo.credit(dc.getTestCard().getId(), amount);
        }

        paymentEventService.createEvent(
                payment.getId(),
                PaymentEventType.REFUND_COMPLETED,
                "{\"amount\":\"" + amount + "\"}",
                "gateway"
        );
    }

    /* ================= Helpers ================= */

    private void fail(Payment p, String code, String message) {
        paymentEventService.createEvent(
                p.getId(),
                PaymentEventType.AUTHORIZATION_FAILED,
                "{\"reason\":\"" + code + "\",\"message\":\"" + escape(message) + "\"}",
                "gateway"
        );
        paymentAttemptService.createAttempt(
                p.getId(),
                PaymentStatus.REJECTED,
                "declined",
                code,
                "Declined by balance check",
                message
        );
        throw new RuntimeException("Payment declined: " + code);
    }

    private void ok(Payment p, BigDecimal amount) {
        paymentEventService.createEvent(
                p.getId(),
                PaymentEventType.AUTHORIZATION_SUCCEEDED,
                "{\"amount\":\"" + amount + "\"}",
                "gateway"
        );
        paymentAttemptService.createAttempt(
                p.getId(),
                PaymentStatus.APPROVED,
                "success",
                "approved"
        );
    }

    private BigDecimal nvl(BigDecimal v) { return v == null ? BigDecimal.ZERO : v; }

    private String escape(String s) { return s == null ? "" : s.replace("\"", "\\\""); }
}