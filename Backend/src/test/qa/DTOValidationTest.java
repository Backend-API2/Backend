package qa;

import org.junit.jupiter.api.*;
import jakarta.validation.*;
import java.util.Set;
import java.math.BigDecimal;

import backend_api.Backend.DTO.payment.CreatePaymentRequest;
import backend_api.Backend.DTO.payment.ConfirmPaymentRequest;
import backend_api.Backend.DTO.invoice.CreateInvoiceRequest;
import backend_api.Backend.DTO.refund.CreateRefundRequest;

public class DTOValidationTest {

    private static Validator validator;

    @BeforeAll
    static void init() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void createPaymentRequest_sinProviderReference_daViolacion() {
        CreatePaymentRequest r = new CreatePaymentRequest();
        // NOTA: no seteamos provider_reference a propósito
        Set<ConstraintViolation<CreatePaymentRequest>> v = validator.validate(r);
        Assertions.assertFalse(v.isEmpty(), "Debe haber violaciones cuando falta provider_reference");
    }

    @Test
    void confirmPaymentRequest_tipoInvalido_daViolacion() {
        ConfirmPaymentRequest r = new ConfirmPaymentRequest();
        r.setPaymentMethodType("bitcoin"); // no coincide con el regex permitido
        Set<ConstraintViolation<ConfirmPaymentRequest>> v = validator.validate(r);
        Assertions.assertFalse(v.isEmpty(), "Debe fallar por regex de método de pago");
    }

    @Test
    void createInvoiceRequest_vacio_daViolaciones() {
        CreateInvoiceRequest r = new CreateInvoiceRequest();
        Set<ConstraintViolation<CreateInvoiceRequest>> v = validator.validate(r);
        Assertions.assertFalse(v.isEmpty(), "CreateInvoiceRequest vacío debe tener violaciones");
    }

    @Test
    void createRefundRequest_sinPaymentId_daViolacion() {
        CreateRefundRequest r = new CreateRefundRequest();
        r.setAmount(new BigDecimal("10.00")); // seteamos amount pero NO paymentId
        Set<ConstraintViolation<CreateRefundRequest>> v = validator.validate(r);
        Assertions.assertFalse(v.isEmpty(), "Debe exigir paymentId");
    }

    @Test
    void createRefundRequest_amountNegativo_daViolacion() {
        CreateRefundRequest r = new CreateRefundRequest();
        r.setPaymentId(1L);
        r.setAmount(new BigDecimal("-1.00")); // viola @DecimalMin
        Set<ConstraintViolation<CreateRefundRequest>> v = validator.validate(r);
        Assertions.assertFalse(v.isEmpty(), "Debe rechazar montos negativos");
    }
}
