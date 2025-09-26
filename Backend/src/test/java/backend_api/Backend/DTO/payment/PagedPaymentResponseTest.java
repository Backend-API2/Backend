package backend_api.Backend.DTO.payment;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for PagedPaymentResponse DTO
 * 
 * Tests the pagination response structure and constructor with Page object,
 * including edge cases with empty content and various page configurations.
 */
class PagedPaymentResponseTest {

    private PaymentResponse paymentResponse1;
    private PaymentResponse paymentResponse2;
    private List<PaymentResponse> paymentResponses;

    @BeforeEach
    void setUp() {
        // Setup test payment responses
        paymentResponse1 = new PaymentResponse();
        paymentResponse1.setId(1L);
        paymentResponse1.setAmount_total(java.math.BigDecimal.valueOf(100.00));
        paymentResponse1.setCurrency("USD");

        paymentResponse2 = new PaymentResponse();
        paymentResponse2.setId(2L);
        paymentResponse2.setAmount_total(java.math.BigDecimal.valueOf(200.00));
        paymentResponse2.setCurrency("EUR");

        paymentResponses = Arrays.asList(paymentResponse1, paymentResponse2);
    }

    // ========== DEFAULT CONSTRUCTOR TESTS ==========

    @Test
    void testDefaultConstructor() {
        // When
        PagedPaymentResponse response = new PagedPaymentResponse();

        // Then
        assertNotNull(response);
        assertNull(response.getContent());
        assertEquals(0, response.getTotalPages());
        assertEquals(0, response.getTotalElements());
        assertEquals(0, response.getSize());
        assertEquals(0, response.getNumber());
        assertFalse(response.isFirst());
        assertFalse(response.isLast());
        assertEquals(0, response.getNumberOfElements());
    }

    // ========== PAGE CONSTRUCTOR TESTS ==========

    @Test
    void testPageConstructor_WithContent() {
        // Given
        Pageable pageable = PageRequest.of(0, 2);
        Page<PaymentResponse> page = new PageImpl<>(paymentResponses, pageable, 2);

        // When
        PagedPaymentResponse response = new PagedPaymentResponse(page);

        // Then
        assertNotNull(response);
        assertNotNull(response.getContent());
        assertEquals(2, response.getContent().size());
        assertEquals(paymentResponse1.getId(), response.getContent().get(0).getId());
        assertEquals(paymentResponse2.getId(), response.getContent().get(1).getId());
        assertEquals(1, response.getTotalPages());
        assertEquals(2, response.getTotalElements());
        assertEquals(2, response.getSize());
        assertEquals(0, response.getNumber());
        assertTrue(response.isFirst());
        assertTrue(response.isLast());
        assertEquals(2, response.getNumberOfElements());
    }

    @Test
    void testPageConstructor_EmptyContent() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        Page<PaymentResponse> page = new PageImpl<>(Collections.emptyList(), pageable, 0);

        // When
        PagedPaymentResponse response = new PagedPaymentResponse(page);

        // Then
        assertNotNull(response);
        assertNotNull(response.getContent());
        assertTrue(response.getContent().isEmpty());
        assertEquals(0, response.getTotalPages());
        assertEquals(0, response.getTotalElements());
        assertEquals(10, response.getSize());
        assertEquals(0, response.getNumber());
        assertTrue(response.isFirst());
        assertTrue(response.isLast());
        assertEquals(0, response.getNumberOfElements());
    }

    @Test
    void testPageConstructor_FirstPage() {
        // Given
        Pageable pageable = PageRequest.of(0, 1);
        Page<PaymentResponse> page = new PageImpl<>(Arrays.asList(paymentResponse1), pageable, 2);

        // When
        PagedPaymentResponse response = new PagedPaymentResponse(page);

        // Then
        assertNotNull(response);
        assertNotNull(response.getContent());
        assertEquals(1, response.getContent().size());
        assertEquals(paymentResponse1.getId(), response.getContent().get(0).getId());
        assertEquals(2, response.getTotalPages());
        assertEquals(2, response.getTotalElements());
        assertEquals(1, response.getSize());
        assertEquals(0, response.getNumber());
        assertTrue(response.isFirst());
        assertFalse(response.isLast());
        assertEquals(1, response.getNumberOfElements());
    }

    @Test
    void testPageConstructor_LastPage() {
        // Given
        Pageable pageable = PageRequest.of(1, 1);
        Page<PaymentResponse> page = new PageImpl<>(Arrays.asList(paymentResponse2), pageable, 2);

        // When
        PagedPaymentResponse response = new PagedPaymentResponse(page);

        // Then
        assertNotNull(response);
        assertNotNull(response.getContent());
        assertEquals(1, response.getContent().size());
        assertEquals(paymentResponse2.getId(), response.getContent().get(0).getId());
        assertEquals(2, response.getTotalPages());
        assertEquals(2, response.getTotalElements());
        assertEquals(1, response.getSize());
        assertEquals(1, response.getNumber());
        assertFalse(response.isFirst());
        assertTrue(response.isLast());
        assertEquals(1, response.getNumberOfElements());
    }

    @Test
    void testPageConstructor_MiddlePage() {
        // Given
        PaymentResponse paymentResponse3 = new PaymentResponse();
        paymentResponse3.setId(3L);
        paymentResponse3.setAmount_total(java.math.BigDecimal.valueOf(300.00));
        paymentResponse3.setCurrency("GBP");

        List<PaymentResponse> allPayments = Arrays.asList(paymentResponse1, paymentResponse2, paymentResponse3);
        Pageable pageable = PageRequest.of(1, 1);
        Page<PaymentResponse> page = new PageImpl<>(Arrays.asList(paymentResponse2), pageable, 3);

        // When
        PagedPaymentResponse response = new PagedPaymentResponse(page);

        // Then
        assertNotNull(response);
        assertNotNull(response.getContent());
        assertEquals(1, response.getContent().size());
        assertEquals(paymentResponse2.getId(), response.getContent().get(0).getId());
        assertEquals(3, response.getTotalPages());
        assertEquals(3, response.getTotalElements());
        assertEquals(1, response.getSize());
        assertEquals(1, response.getNumber());
        assertFalse(response.isFirst());
        assertFalse(response.isLast());
        assertEquals(1, response.getNumberOfElements());
    }

    @Test
    void testPageConstructor_LargeDataset() {
        // Given
        List<PaymentResponse> largeContent = Arrays.asList(paymentResponse1, paymentResponse2);
        Pageable pageable = PageRequest.of(0, 2);
        Page<PaymentResponse> page = new PageImpl<>(largeContent, pageable, 1000);

        // When
        PagedPaymentResponse response = new PagedPaymentResponse(page);

        // Then
        assertNotNull(response);
        assertNotNull(response.getContent());
        assertEquals(2, response.getContent().size());
        assertEquals(500, response.getTotalPages());
        assertEquals(1000, response.getTotalElements());
        assertEquals(2, response.getSize());
        assertEquals(0, response.getNumber());
        assertTrue(response.isFirst());
        assertFalse(response.isLast());
        assertEquals(2, response.getNumberOfElements());
    }

    // ========== SETTER AND GETTER TESTS ==========

    @Test
    void testSetAndGetContent() {
        // Given
        PagedPaymentResponse response = new PagedPaymentResponse();

        // When
        response.setContent(paymentResponses);

        // Then
        assertNotNull(response.getContent());
        assertEquals(2, response.getContent().size());
        assertEquals(paymentResponse1.getId(), response.getContent().get(0).getId());
        assertEquals(paymentResponse2.getId(), response.getContent().get(1).getId());
    }

    @Test
    void testSetContent_Null() {
        // Given
        PagedPaymentResponse response = new PagedPaymentResponse();
        response.setContent(paymentResponses);

        // When
        response.setContent(null);

        // Then
        assertNull(response.getContent());
    }

    @Test
    void testSetContent_EmptyList() {
        // Given
        PagedPaymentResponse response = new PagedPaymentResponse();

        // When
        response.setContent(Collections.emptyList());

        // Then
        assertNotNull(response.getContent());
        assertTrue(response.getContent().isEmpty());
    }

    // ========== EDGE CASES ==========

    @Test
    void testPageConstructor_NullPage() {
        // When & Then
        assertThrows(NullPointerException.class, () -> {
            new PagedPaymentResponse(null);
        });
    }

    @Test
    void testPageConstructor_PageWithNullContent() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        
        // When & Then - PageImpl constructor throws IllegalArgumentException for null content
        assertThrows(IllegalArgumentException.class, () -> {
            new PageImpl<>(null, pageable, 0);
        });
    }

    @Test
    void testPageConstructor_ZeroSize() {
        // Given & When & Then - PageRequest.of throws IllegalArgumentException for zero size
        assertThrows(IllegalArgumentException.class, () -> {
            PageRequest.of(0, 0);
        });
    }

    @Test
    void testPageConstructor_NegativePageNumber() {
        // Given & When & Then - PageRequest.of throws IllegalArgumentException for negative page number
        assertThrows(IllegalArgumentException.class, () -> {
            PageRequest.of(-1, 10);
        });
    }

    @Test
    void testPageConstructor_VeryLargePageNumber() {
        // Given
        Pageable pageable = PageRequest.of(1000, 10);
        Page<PaymentResponse> page = new PageImpl<>(Collections.emptyList(), pageable, 0);

        // When
        PagedPaymentResponse response = new PagedPaymentResponse(page);

        // Then
        assertNotNull(response);
        assertNotNull(response.getContent());
        assertTrue(response.getContent().isEmpty());
        assertEquals(0, response.getTotalPages());
        assertEquals(0, response.getTotalElements());
        assertEquals(10, response.getSize());
        assertEquals(1000, response.getNumber());
        assertFalse(response.isFirst()); // Page 1000 is not the first page
        assertTrue(response.isLast());
        assertEquals(0, response.getNumberOfElements());
    }
}
