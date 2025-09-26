package backend_api.Backend.DTO.payment;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class CreatePaymentRequestTest {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void testValidCreatePaymentRequest() {
        // Given
        CreatePaymentRequest request = new CreatePaymentRequest();
        request.setSolicitud_reference("SOL-2024-001");
        request.setProvider_reference("PROV-STRIPE-001");
        request.setProvider_id(1L);
        request.setAmount_subtotal(BigDecimal.valueOf(100.00));
        request.setTaxes(BigDecimal.valueOf(10.00));
        request.setFees(BigDecimal.valueOf(5.00));
        request.setCurrency("USD");
        request.setMetadata("Test metadata");

        // When
        Set<ConstraintViolation<CreatePaymentRequest>> violations = validator.validate(request);

        // Then
        assertTrue(violations.isEmpty());
    }

    @Test
    void testInvalidProviderReference_Blank() {
        // Given
        CreatePaymentRequest request = new CreatePaymentRequest();
        request.setSolicitud_reference("SOL-2024-001");
        request.setProvider_reference("");
        request.setProvider_id(1L);
        request.setAmount_subtotal(BigDecimal.valueOf(100.00));
        request.setTaxes(BigDecimal.valueOf(10.00));
        request.setFees(BigDecimal.valueOf(5.00));
        request.setCurrency("USD");

        // When
        Set<ConstraintViolation<CreatePaymentRequest>> violations = validator.validate(request);

        // Then
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("provider_reference")));
    }

    @Test
    void testInvalidProviderReference_Null() {
        // Given
        CreatePaymentRequest request = new CreatePaymentRequest();
        request.setSolicitud_reference("SOL-2024-001");
        request.setProvider_reference(null);
        request.setProvider_id(1L);
        request.setAmount_subtotal(BigDecimal.valueOf(100.00));
        request.setTaxes(BigDecimal.valueOf(10.00));
        request.setFees(BigDecimal.valueOf(5.00));
        request.setCurrency("USD");

        // When
        Set<ConstraintViolation<CreatePaymentRequest>> violations = validator.validate(request);

        // Then
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("provider_reference")));
    }

    @Test
    void testInvalidAmountSubtotal_Null() {
        // Given
        CreatePaymentRequest request = new CreatePaymentRequest();
        request.setSolicitud_reference("SOL-2024-001");
        request.setProvider_reference("PROV-STRIPE-001");
        request.setProvider_id(1L);
        request.setAmount_subtotal(null);
        request.setTaxes(BigDecimal.valueOf(10.00));
        request.setFees(BigDecimal.valueOf(5.00));
        request.setCurrency("USD");

        // When
        Set<ConstraintViolation<CreatePaymentRequest>> violations = validator.validate(request);

        // Then
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("amount_subtotal")));
    }

    @Test
    void testInvalidAmountSubtotal_Zero() {
        // Given
        CreatePaymentRequest request = new CreatePaymentRequest();
        request.setSolicitud_reference("SOL-2024-001");
        request.setProvider_reference("PROV-STRIPE-001");
        request.setProvider_id(1L);
        request.setAmount_subtotal(BigDecimal.ZERO);
        request.setTaxes(BigDecimal.valueOf(10.00));
        request.setFees(BigDecimal.valueOf(5.00));
        request.setCurrency("USD");

        // When
        Set<ConstraintViolation<CreatePaymentRequest>> violations = validator.validate(request);

        // Then
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("amount_subtotal")));
    }

    @Test
    void testInvalidAmountSubtotal_Negative() {
        // Given
        CreatePaymentRequest request = new CreatePaymentRequest();
        request.setSolicitud_reference("SOL-2024-001");
        request.setProvider_reference("PROV-STRIPE-001");
        request.setProvider_id(1L);
        request.setAmount_subtotal(BigDecimal.valueOf(-10.00));
        request.setTaxes(BigDecimal.valueOf(10.00));
        request.setFees(BigDecimal.valueOf(5.00));
        request.setCurrency("USD");

        // When
        Set<ConstraintViolation<CreatePaymentRequest>> violations = validator.validate(request);

        // Then
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("amount_subtotal")));
    }

    @Test
    void testValidAmountSubtotal_Minimum() {
        // Given
        CreatePaymentRequest request = new CreatePaymentRequest();
        request.setSolicitud_reference("SOL-2024-001");
        request.setProvider_reference("PROV-STRIPE-001");
        request.setProvider_id(1L);
        request.setAmount_subtotal(BigDecimal.valueOf(0.01)); // Minimum valid amount
        request.setTaxes(BigDecimal.valueOf(10.00));
        request.setFees(BigDecimal.valueOf(5.00));
        request.setCurrency("USD");

        // When
        Set<ConstraintViolation<CreatePaymentRequest>> violations = validator.validate(request);

        // Then
        assertTrue(violations.isEmpty());
    }

    @Test
    void testInvalidTaxes_Null() {
        // Given
        CreatePaymentRequest request = new CreatePaymentRequest();
        request.setSolicitud_reference("SOL-2024-001");
        request.setProvider_reference("PROV-STRIPE-001");
        request.setProvider_id(1L);
        request.setAmount_subtotal(BigDecimal.valueOf(100.00));
        request.setTaxes(null);
        request.setFees(BigDecimal.valueOf(5.00));
        request.setCurrency("USD");

        // When
        Set<ConstraintViolation<CreatePaymentRequest>> violations = validator.validate(request);

        // Then
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("taxes")));
    }

    @Test
    void testInvalidTaxes_Negative() {
        // Given
        CreatePaymentRequest request = new CreatePaymentRequest();
        request.setSolicitud_reference("SOL-2024-001");
        request.setProvider_reference("PROV-STRIPE-001");
        request.setProvider_id(1L);
        request.setAmount_subtotal(BigDecimal.valueOf(100.00));
        request.setTaxes(BigDecimal.valueOf(-5.00));
        request.setFees(BigDecimal.valueOf(5.00));
        request.setCurrency("USD");

        // When
        Set<ConstraintViolation<CreatePaymentRequest>> violations = validator.validate(request);

        // Then
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("taxes")));
    }

    @Test
    void testValidTaxes_Zero() {
        // Given
        CreatePaymentRequest request = new CreatePaymentRequest();
        request.setSolicitud_reference("SOL-2024-001");
        request.setProvider_reference("PROV-STRIPE-001");
        request.setProvider_id(1L);
        request.setAmount_subtotal(BigDecimal.valueOf(100.00));
        request.setTaxes(BigDecimal.ZERO);
        request.setFees(BigDecimal.valueOf(5.00));
        request.setCurrency("USD");

        // When
        Set<ConstraintViolation<CreatePaymentRequest>> violations = validator.validate(request);

        // Then
        assertTrue(violations.isEmpty());
    }

    @Test
    void testInvalidFees_Null() {
        // Given
        CreatePaymentRequest request = new CreatePaymentRequest();
        request.setSolicitud_reference("SOL-2024-001");
        request.setProvider_reference("PROV-STRIPE-001");
        request.setProvider_id(1L);
        request.setAmount_subtotal(BigDecimal.valueOf(100.00));
        request.setTaxes(BigDecimal.valueOf(10.00));
        request.setFees(null);
        request.setCurrency("USD");

        // When
        Set<ConstraintViolation<CreatePaymentRequest>> violations = validator.validate(request);

        // Then
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("fees")));
    }

    @Test
    void testInvalidFees_Negative() {
        // Given
        CreatePaymentRequest request = new CreatePaymentRequest();
        request.setSolicitud_reference("SOL-2024-001");
        request.setProvider_reference("PROV-STRIPE-001");
        request.setProvider_id(1L);
        request.setAmount_subtotal(BigDecimal.valueOf(100.00));
        request.setTaxes(BigDecimal.valueOf(10.00));
        request.setFees(BigDecimal.valueOf(-2.00));
        request.setCurrency("USD");

        // When
        Set<ConstraintViolation<CreatePaymentRequest>> violations = validator.validate(request);

        // Then
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("fees")));
    }

    @Test
    void testValidFees_Zero() {
        // Given
        CreatePaymentRequest request = new CreatePaymentRequest();
        request.setSolicitud_reference("SOL-2024-001");
        request.setProvider_reference("PROV-STRIPE-001");
        request.setProvider_id(1L);
        request.setAmount_subtotal(BigDecimal.valueOf(100.00));
        request.setTaxes(BigDecimal.valueOf(10.00));
        request.setFees(BigDecimal.ZERO);
        request.setCurrency("USD");

        // When
        Set<ConstraintViolation<CreatePaymentRequest>> violations = validator.validate(request);

        // Then
        assertTrue(violations.isEmpty());
    }

    @Test
    void testInvalidCurrency_Null() {
        // Given
        CreatePaymentRequest request = new CreatePaymentRequest();
        request.setSolicitud_reference("SOL-2024-001");
        request.setProvider_reference("PROV-STRIPE-001");
        request.setProvider_id(1L);
        request.setAmount_subtotal(BigDecimal.valueOf(100.00));
        request.setTaxes(BigDecimal.valueOf(10.00));
        request.setFees(BigDecimal.valueOf(5.00));
        request.setCurrency(null);

        // When
        Set<ConstraintViolation<CreatePaymentRequest>> violations = validator.validate(request);

        // Then
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("currency")));
    }

    @Test
    void testInvalidCurrency_TooShort() {
        // Given
        CreatePaymentRequest request = new CreatePaymentRequest();
        request.setSolicitud_reference("SOL-2024-001");
        request.setProvider_reference("PROV-STRIPE-001");
        request.setProvider_id(1L);
        request.setAmount_subtotal(BigDecimal.valueOf(100.00));
        request.setTaxes(BigDecimal.valueOf(10.00));
        request.setFees(BigDecimal.valueOf(5.00));
        request.setCurrency("US"); // Only 2 characters

        // When
        Set<ConstraintViolation<CreatePaymentRequest>> violations = validator.validate(request);

        // Then
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("currency")));
    }

    @Test
    void testInvalidCurrency_TooLong() {
        // Given
        CreatePaymentRequest request = new CreatePaymentRequest();
        request.setSolicitud_reference("SOL-2024-001");
        request.setProvider_reference("PROV-STRIPE-001");
        request.setProvider_id(1L);
        request.setAmount_subtotal(BigDecimal.valueOf(100.00));
        request.setTaxes(BigDecimal.valueOf(10.00));
        request.setFees(BigDecimal.valueOf(5.00));
        request.setCurrency("USDD"); // 4 characters

        // When
        Set<ConstraintViolation<CreatePaymentRequest>> violations = validator.validate(request);

        // Then
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("currency")));
    }

    @Test
    void testInvalidCurrency_Lowercase() {
        // Given
        CreatePaymentRequest request = new CreatePaymentRequest();
        request.setSolicitud_reference("SOL-2024-001");
        request.setProvider_reference("PROV-STRIPE-001");
        request.setProvider_id(1L);
        request.setAmount_subtotal(BigDecimal.valueOf(100.00));
        request.setTaxes(BigDecimal.valueOf(10.00));
        request.setFees(BigDecimal.valueOf(5.00));
        request.setCurrency("usd"); // Lowercase

        // When
        Set<ConstraintViolation<CreatePaymentRequest>> violations = validator.validate(request);

        // Then
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("currency")));
    }

    @Test
    void testValidCurrency_CommonCodes() {
        // Given
        String[] validCurrencies = {"USD", "EUR", "GBP", "JPY", "CAD", "AUD", "CHF", "CNY", "SEK", "NZD"};

        for (String currency : validCurrencies) {
            CreatePaymentRequest request = new CreatePaymentRequest();
            request.setSolicitud_reference("SOL-2024-001");
            request.setProvider_reference("PROV-STRIPE-001");
            request.setProvider_id(1L);
            request.setAmount_subtotal(BigDecimal.valueOf(100.00));
            request.setTaxes(BigDecimal.valueOf(10.00));
            request.setFees(BigDecimal.valueOf(5.00));
            request.setCurrency(currency);

            // When
            Set<ConstraintViolation<CreatePaymentRequest>> violations = validator.validate(request);

            // Then
            assertTrue(violations.isEmpty(), "Currency " + currency + " should be valid");
        }
    }

    @Test
    void testInvalidMetadata_TooLong() {
        // Given
        CreatePaymentRequest request = new CreatePaymentRequest();
        request.setSolicitud_reference("SOL-2024-001");
        request.setProvider_reference("PROV-STRIPE-001");
        request.setProvider_id(1L);
        request.setAmount_subtotal(BigDecimal.valueOf(100.00));
        request.setTaxes(BigDecimal.valueOf(10.00));
        request.setFees(BigDecimal.valueOf(5.00));
        request.setCurrency("USD");
        request.setMetadata("A".repeat(1001)); // More than 1000 characters

        // When
        Set<ConstraintViolation<CreatePaymentRequest>> violations = validator.validate(request);

        // Then
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("metadata")));
    }

    @Test
    void testValidMetadata_MaximumLength() {
        // Given
        CreatePaymentRequest request = new CreatePaymentRequest();
        request.setSolicitud_reference("SOL-2024-001");
        request.setProvider_reference("PROV-STRIPE-001");
        request.setProvider_id(1L);
        request.setAmount_subtotal(BigDecimal.valueOf(100.00));
        request.setTaxes(BigDecimal.valueOf(10.00));
        request.setFees(BigDecimal.valueOf(5.00));
        request.setCurrency("USD");
        request.setMetadata("A".repeat(1000)); // Exactly 1000 characters

        // When
        Set<ConstraintViolation<CreatePaymentRequest>> violations = validator.validate(request);

        // Then
        assertTrue(violations.isEmpty());
    }

    @Test
    void testValidMetadata_Null() {
        // Given
        CreatePaymentRequest request = new CreatePaymentRequest();
        request.setSolicitud_reference("SOL-2024-001");
        request.setProvider_reference("PROV-STRIPE-001");
        request.setProvider_id(1L);
        request.setAmount_subtotal(BigDecimal.valueOf(100.00));
        request.setTaxes(BigDecimal.valueOf(10.00));
        request.setFees(BigDecimal.valueOf(5.00));
        request.setCurrency("USD");
        request.setMetadata(null); // Null is allowed

        // When
        Set<ConstraintViolation<CreatePaymentRequest>> violations = validator.validate(request);

        // Then
        assertTrue(violations.isEmpty());
    }

    @Test
    void testValidSolicitudReference_Null() {
        // Given
        CreatePaymentRequest request = new CreatePaymentRequest();
        request.setSolicitud_reference(null); // Null is allowed
        request.setProvider_reference("PROV-STRIPE-001");
        request.setProvider_id(1L);
        request.setAmount_subtotal(BigDecimal.valueOf(100.00));
        request.setTaxes(BigDecimal.valueOf(10.00));
        request.setFees(BigDecimal.valueOf(5.00));
        request.setCurrency("USD");

        // When
        Set<ConstraintViolation<CreatePaymentRequest>> violations = validator.validate(request);

        // Then
        assertTrue(violations.isEmpty());
    }

    @Test
    void testValidProviderId_Null() {
        // Given
        CreatePaymentRequest request = new CreatePaymentRequest();
        request.setSolicitud_reference("SOL-2024-001");
        request.setProvider_reference("PROV-STRIPE-001");
        request.setProvider_id(null); // Null is allowed
        request.setAmount_subtotal(BigDecimal.valueOf(100.00));
        request.setTaxes(BigDecimal.valueOf(10.00));
        request.setFees(BigDecimal.valueOf(5.00));
        request.setCurrency("USD");

        // When
        Set<ConstraintViolation<CreatePaymentRequest>> violations = validator.validate(request);

        // Then
        assertTrue(violations.isEmpty());
    }

    @Test
    void testMultipleViolations() {
        // Given
        CreatePaymentRequest request = new CreatePaymentRequest();
        request.setSolicitud_reference("SOL-2024-001");
        request.setProvider_reference(""); // Blank
        request.setProvider_id(1L);
        request.setAmount_subtotal(BigDecimal.valueOf(-10.00)); // Negative
        request.setTaxes(BigDecimal.valueOf(-5.00)); // Negative
        request.setFees(BigDecimal.valueOf(-2.00)); // Negative
        request.setCurrency("usd"); // Invalid format
        request.setMetadata("A".repeat(1001)); // Too long

        // When
        Set<ConstraintViolation<CreatePaymentRequest>> violations = validator.validate(request);

        // Then
        assertFalse(violations.isEmpty());
        assertEquals(6, violations.size()); // provider_reference, amount_subtotal, taxes, fees, currency, metadata
    }
}
