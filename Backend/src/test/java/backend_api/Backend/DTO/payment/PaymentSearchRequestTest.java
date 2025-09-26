package backend_api.Backend.DTO.payment;

import backend_api.Backend.Entity.payment.PaymentStatus;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for PaymentSearchRequest DTO
 * 
 * Tests the validation constraints and default values for the payment search request,
 * including pagination, sorting, and filtering parameters.
 */
class PaymentSearchRequestTest {

    private Validator validator;
    private PaymentSearchRequest request;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
        request = new PaymentSearchRequest();
    }

    // ========== DEFAULT VALUES TESTS ==========

    @Test
    void testDefaultValues() {
        // When
        PaymentSearchRequest defaultRequest = new PaymentSearchRequest();

        // Then
        assertNull(defaultRequest.getStatus());
        assertNull(defaultRequest.getCurrency());
        assertNull(defaultRequest.getMinAmount());
        assertNull(defaultRequest.getMaxAmount());
        assertNull(defaultRequest.getStartDate());
        assertNull(defaultRequest.getEndDate());
        assertNull(defaultRequest.getUserId());
        assertNull(defaultRequest.getUserName());
        assertNull(defaultRequest.getUserIds());
        assertNull(defaultRequest.getProviderId());
        assertNull(defaultRequest.getProviderIds());
        assertNull(defaultRequest.getSolicitudId());
        assertNull(defaultRequest.getMetadataKey());
        assertNull(defaultRequest.getMetadataValue());
        assertEquals(0, defaultRequest.getPage());
        assertEquals(10, defaultRequest.getSize());
        assertEquals("created_at", defaultRequest.getSortBy());
        assertEquals("desc", defaultRequest.getSortDir());
    }

    // ========== VALIDATION TESTS ==========

    @Test
    void testValidRequest() {
        // Given
        request.setStatus(PaymentStatus.APPROVED);
        request.setCurrency("USD");
        request.setMinAmount(BigDecimal.valueOf(10.00));
        request.setMaxAmount(BigDecimal.valueOf(100.00));
        request.setStartDate(LocalDate.of(2024, 1, 1));
        request.setEndDate(LocalDate.of(2024, 12, 31));
        request.setUserId(1L);
        request.setUserName("Test User");
        request.setUserIds(Arrays.asList(1L, 2L, 3L));
        request.setProviderId(2L);
        request.setProviderIds(Arrays.asList(2L, 3L));
        request.setSolicitudId(10L);
        request.setMetadataKey("order_id");
        request.setMetadataValue("12345");
        request.setPage(0);
        request.setSize(20);
        request.setSortBy("amount_total");
        request.setSortDir("asc");

        // When
        Set<ConstraintViolation<PaymentSearchRequest>> violations = validator.validate(request);

        // Then
        assertTrue(violations.isEmpty());
    }

    @Test
    void testNegativePageNumber() {
        // Given
        request.setPage(-1);

        // When
        Set<ConstraintViolation<PaymentSearchRequest>> violations = validator.validate(request);

        // Then
        assertEquals(1, violations.size());
        ConstraintViolation<PaymentSearchRequest> violation = violations.iterator().next();
        assertEquals("Page number must be non-negative", violation.getMessage());
        assertEquals("page", violation.getPropertyPath().toString());
    }

    @Test
    void testZeroPageSize() {
        // Given
        request.setSize(0);

        // When
        Set<ConstraintViolation<PaymentSearchRequest>> violations = validator.validate(request);

        // Then
        assertEquals(1, violations.size());
        ConstraintViolation<PaymentSearchRequest> violation = violations.iterator().next();
        assertEquals("Page size must be positive", violation.getMessage());
        assertEquals("size", violation.getPropertyPath().toString());
    }

    @Test
    void testNegativePageSize() {
        // Given
        request.setSize(-5);

        // When
        Set<ConstraintViolation<PaymentSearchRequest>> violations = validator.validate(request);

        // Then
        assertEquals(1, violations.size());
        ConstraintViolation<PaymentSearchRequest> violation = violations.iterator().next();
        assertEquals("Page size must be positive", violation.getMessage());
        assertEquals("size", violation.getPropertyPath().toString());
    }

    @Test
    void testInvalidSortBy() {
        // Given
        request.setSortBy("invalid_field");

        // When
        Set<ConstraintViolation<PaymentSearchRequest>> violations = validator.validate(request);

        // Then
        assertEquals(1, violations.size());
        ConstraintViolation<PaymentSearchRequest> violation = violations.iterator().next();
        assertEquals("Invalid sort field", violation.getMessage());
        assertEquals("sortBy", violation.getPropertyPath().toString());
    }

    @Test
    void testValidSortByFields() {
        // Given
        String[] validSortFields = {"id", "user_id", "provider_id", "amount_total", "created_at", "updated_at"};

        for (String sortField : validSortFields) {
            request.setSortBy(sortField);

            // When
            Set<ConstraintViolation<PaymentSearchRequest>> violations = validator.validate(request);

            // Then
            assertTrue(violations.isEmpty(), "Sort field '" + sortField + "' should be valid");
        }
    }

    @Test
    void testInvalidSortDirection() {
        // Given
        request.setSortDir("invalid");

        // When
        Set<ConstraintViolation<PaymentSearchRequest>> violations = validator.validate(request);

        // Then
        assertEquals(1, violations.size());
        ConstraintViolation<PaymentSearchRequest> violation = violations.iterator().next();
        assertEquals("Sort direction must be 'asc' or 'desc'", violation.getMessage());
        assertEquals("sortDir", violation.getPropertyPath().toString());
    }

    @Test
    void testValidSortDirections() {
        // Given
        String[] validSortDirections = {"asc", "desc"};

        for (String sortDir : validSortDirections) {
            request.setSortDir(sortDir);

            // When
            Set<ConstraintViolation<PaymentSearchRequest>> violations = validator.validate(request);

            // Then
            assertTrue(violations.isEmpty(), "Sort direction '" + sortDir + "' should be valid");
        }
    }

    @Test
    void testMultipleValidationErrors() {
        // Given
        request.setPage(-1);
        request.setSize(0);
        request.setSortBy("invalid_field");
        request.setSortDir("invalid");

        // When
        Set<ConstraintViolation<PaymentSearchRequest>> violations = validator.validate(request);

        // Then
        assertEquals(4, violations.size());
    }

    // ========== SETTER AND GETTER TESTS ==========

    @Test
    void testSetAndGetStatus() {
        // Given
        PaymentStatus status = PaymentStatus.PENDING_PAYMENT;

        // When
        request.setStatus(status);

        // Then
        assertEquals(status, request.getStatus());
    }

    @Test
    void testSetAndGetCurrency() {
        // Given
        String currency = "EUR";

        // When
        request.setCurrency(currency);

        // Then
        assertEquals(currency, request.getCurrency());
    }

    @Test
    void testSetAndGetMinAmount() {
        // Given
        BigDecimal minAmount = BigDecimal.valueOf(50.00);

        // When
        request.setMinAmount(minAmount);

        // Then
        assertEquals(minAmount, request.getMinAmount());
    }

    @Test
    void testSetAndGetMaxAmount() {
        // Given
        BigDecimal maxAmount = BigDecimal.valueOf(500.00);

        // When
        request.setMaxAmount(maxAmount);

        // Then
        assertEquals(maxAmount, request.getMaxAmount());
    }

    @Test
    void testSetAndGetStartDate() {
        // Given
        LocalDate startDate = LocalDate.of(2024, 6, 1);

        // When
        request.setStartDate(startDate);

        // Then
        assertEquals(startDate, request.getStartDate());
    }

    @Test
    void testSetAndGetEndDate() {
        // Given
        LocalDate endDate = LocalDate.of(2024, 6, 30);

        // When
        request.setEndDate(endDate);

        // Then
        assertEquals(endDate, request.getEndDate());
    }

    @Test
    void testSetAndGetUserId() {
        // Given
        Long userId = 123L;

        // When
        request.setUserId(userId);

        // Then
        assertEquals(userId, request.getUserId());
    }

    @Test
    void testSetAndGetUserName() {
        // Given
        String userName = "John Doe";

        // When
        request.setUserName(userName);

        // Then
        assertEquals(userName, request.getUserName());
    }

    @Test
    void testSetAndGetUserIds() {
        // Given
        java.util.List<Long> userIds = Arrays.asList(1L, 2L, 3L);

        // When
        request.setUserIds(userIds);

        // Then
        assertEquals(userIds, request.getUserIds());
        assertEquals(3, request.getUserIds().size());
    }

    @Test
    void testSetAndGetProviderId() {
        // Given
        Long providerId = 456L;

        // When
        request.setProviderId(providerId);

        // Then
        assertEquals(providerId, request.getProviderId());
    }

    @Test
    void testSetAndGetProviderIds() {
        // Given
        java.util.List<Long> providerIds = Arrays.asList(4L, 5L, 6L);

        // When
        request.setProviderIds(providerIds);

        // Then
        assertEquals(providerIds, request.getProviderIds());
        assertEquals(3, request.getProviderIds().size());
    }

    @Test
    void testSetAndGetSolicitudId() {
        // Given
        Long solicitudId = 789L;

        // When
        request.setSolicitudId(solicitudId);

        // Then
        assertEquals(solicitudId, request.getSolicitudId());
    }

    @Test
    void testSetAndGetMetadataKey() {
        // Given
        String metadataKey = "order_reference";

        // When
        request.setMetadataKey(metadataKey);

        // Then
        assertEquals(metadataKey, request.getMetadataKey());
    }

    @Test
    void testSetAndGetMetadataValue() {
        // Given
        String metadataValue = "ORD-12345";

        // When
        request.setMetadataValue(metadataValue);

        // Then
        assertEquals(metadataValue, request.getMetadataValue());
    }

    @Test
    void testSetAndGetPage() {
        // Given
        int page = 2;

        // When
        request.setPage(page);

        // Then
        assertEquals(page, request.getPage());
    }

    @Test
    void testSetAndGetSize() {
        // Given
        int size = 25;

        // When
        request.setSize(size);

        // Then
        assertEquals(size, request.getSize());
    }

    @Test
    void testSetAndGetSortBy() {
        // Given
        String sortBy = "amount_total";

        // When
        request.setSortBy(sortBy);

        // Then
        assertEquals(sortBy, request.getSortBy());
    }

    @Test
    void testSetAndGetSortDir() {
        // Given
        String sortDir = "asc";

        // When
        request.setSortDir(sortDir);

        // Then
        assertEquals(sortDir, request.getSortDir());
    }

    // ========== EDGE CASES ==========

    @Test
    void testNullValues() {
        // When
        request.setStatus(null);
        request.setCurrency(null);
        request.setMinAmount(null);
        request.setMaxAmount(null);
        request.setStartDate(null);
        request.setEndDate(null);
        request.setUserId(null);
        request.setUserName(null);
        request.setUserIds(null);
        request.setProviderId(null);
        request.setProviderIds(null);
        request.setSolicitudId(null);
        request.setMetadataKey(null);
        request.setMetadataValue(null);

        // Then
        assertNull(request.getStatus());
        assertNull(request.getCurrency());
        assertNull(request.getMinAmount());
        assertNull(request.getMaxAmount());
        assertNull(request.getStartDate());
        assertNull(request.getEndDate());
        assertNull(request.getUserId());
        assertNull(request.getUserName());
        assertNull(request.getUserIds());
        assertNull(request.getProviderId());
        assertNull(request.getProviderIds());
        assertNull(request.getSolicitudId());
        assertNull(request.getMetadataKey());
        assertNull(request.getMetadataValue());
    }

    @Test
    void testEmptyStringValues() {
        // Given
        String emptyString = "";

        // When
        request.setCurrency(emptyString);
        request.setUserName(emptyString);
        request.setMetadataKey(emptyString);
        request.setMetadataValue(emptyString);
        request.setSortBy(emptyString);
        request.setSortDir(emptyString);

        // Then
        assertEquals(emptyString, request.getCurrency());
        assertEquals(emptyString, request.getUserName());
        assertEquals(emptyString, request.getMetadataKey());
        assertEquals(emptyString, request.getMetadataValue());
        assertEquals(emptyString, request.getSortBy());
        assertEquals(emptyString, request.getSortDir());
    }

    @Test
    void testEmptyListValues() {
        // Given
        java.util.List<Long> emptyList = Arrays.asList();

        // When
        request.setUserIds(emptyList);
        request.setProviderIds(emptyList);

        // Then
        assertEquals(emptyList, request.getUserIds());
        assertEquals(emptyList, request.getProviderIds());
        assertTrue(request.getUserIds().isEmpty());
        assertTrue(request.getProviderIds().isEmpty());
    }

    @Test
    void testLargePageSize() {
        // Given
        int largeSize = 1000;

        // When
        request.setSize(largeSize);

        // Then
        assertEquals(largeSize, request.getSize());
    }

    @Test
    void testLargePageNumber() {
        // Given
        int largePage = 100;

        // When
        request.setPage(largePage);

        // Then
        assertEquals(largePage, request.getPage());
    }

    @Test
    void testZeroValues() {
        // Given
        BigDecimal zeroAmount = BigDecimal.ZERO;
        LocalDate epochDate = LocalDate.of(1970, 1, 1);

        // When
        request.setMinAmount(zeroAmount);
        request.setMaxAmount(zeroAmount);
        request.setStartDate(epochDate);
        request.setEndDate(epochDate);
        request.setUserId(0L);
        request.setProviderId(0L);
        request.setSolicitudId(0L);

        // Then
        assertEquals(zeroAmount, request.getMinAmount());
        assertEquals(zeroAmount, request.getMaxAmount());
        assertEquals(epochDate, request.getStartDate());
        assertEquals(epochDate, request.getEndDate());
        assertEquals(0L, request.getUserId());
        assertEquals(0L, request.getProviderId());
        assertEquals(0L, request.getSolicitudId());
    }
}
