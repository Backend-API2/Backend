package backend_api.Backend.Repository;

import backend_api.Backend.Entity.invoice.Invoice;
import backend_api.Backend.Entity.invoice.InvoiceStatus;
import backend_api.Backend.Entity.invoice.InvoiceType;
import backend_api.Backend.Entity.payment.Payment;
import backend_api.Backend.Entity.payment.PaymentStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class InvoiceRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private InvoiceRepository invoiceRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    private Payment testPayment;
    private Invoice testInvoice;

    @BeforeEach
    void setUp() {
        // Create test payment
        testPayment = new Payment();
        testPayment.setStatus(PaymentStatus.COMPLETED);
        testPayment = entityManager.persistAndFlush(testPayment);

        // Create test invoice
        testInvoice = new Invoice();
        testInvoice.setInvoiceNumber("INV-001");
        testInvoice.setPaymentId(testPayment.getId());
        testInvoice.setUserId(1L);
        testInvoice.setProviderId(1L);
        testInvoice.setTotalAmount(BigDecimal.valueOf(100.00));
        testInvoice.setSubtotalAmount(BigDecimal.valueOf(90.00));
        testInvoice.setTaxAmount(BigDecimal.valueOf(10.00));
        testInvoice.setStatus(InvoiceStatus.PENDING);
        testInvoice.setType(InvoiceType.STANDARD);
        testInvoice.setCurrency("USD");
        testInvoice.setIssueDate(LocalDateTime.now());
        testInvoice.setDueDate(LocalDateTime.now().plusDays(30));
        testInvoice = entityManager.persistAndFlush(testInvoice);
    }

    @Test
    void testFindByInvoiceNumber_Success() {
        // When
        Optional<Invoice> result = invoiceRepository.findByInvoiceNumber("INV-001");

        // Then
        assertTrue(result.isPresent());
        assertEquals("INV-001", result.get().getInvoiceNumber());
        assertEquals(testInvoice.getId(), result.get().getId());
    }

    @Test
    void testFindByInvoiceNumber_NotFound() {
        // When
        Optional<Invoice> result = invoiceRepository.findByInvoiceNumber("INV-999");

        // Then
        assertFalse(result.isPresent());
    }

    @Test
    void testFindByUserId_Success() {
        // Given
        PageRequest pageRequest = PageRequest.of(0, 10);

        // When
        Page<Invoice> result = invoiceRepository.findByUserId(1L, pageRequest);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals(1L, result.getTotalElements());
        assertEquals(1L, result.getContent().get(0).getUserId());
    }

    @Test
    void testFindByUserId_NotFound() {
        // Given
        PageRequest pageRequest = PageRequest.of(0, 10);

        // When
        Page<Invoice> result = invoiceRepository.findByUserId(999L, pageRequest);

        // Then
        assertNotNull(result);
        assertEquals(0, result.getContent().size());
        assertEquals(0L, result.getTotalElements());
    }

    @Test
    void testFindByProviderId_Success() {
        // Given
        PageRequest pageRequest = PageRequest.of(0, 10);

        // When
        Page<Invoice> result = invoiceRepository.findByProviderId(1L, pageRequest);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals(1L, result.getTotalElements());
        assertEquals(1L, result.getContent().get(0).getProviderId());
    }

    @Test
    void testFindByStatus_Success() {
        // Given
        PageRequest pageRequest = PageRequest.of(0, 10);

        // When
        Page<Invoice> result = invoiceRepository.findByStatus(InvoiceStatus.PENDING, pageRequest);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals(1L, result.getTotalElements());
        assertEquals(InvoiceStatus.PENDING, result.getContent().get(0).getStatus());
    }

    @Test
    void testFindByPaymentId_Success() {
        // When
        List<Invoice> result = invoiceRepository.findByPaymentId(testPayment.getId());

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testPayment.getId(), result.get(0).getPaymentId());
    }

    @Test
    void testSaveInvoice_Success() {
        // Given
        Invoice newInvoice = new Invoice();
        newInvoice.setInvoiceNumber("INV-002");
        newInvoice.setPaymentId(testPayment.getId());
        newInvoice.setUserId(2L);
        newInvoice.setProviderId(2L);
        newInvoice.setTotalAmount(BigDecimal.valueOf(200.00));
        newInvoice.setSubtotalAmount(BigDecimal.valueOf(180.00));
        newInvoice.setTaxAmount(BigDecimal.valueOf(20.00));
        newInvoice.setStatus(InvoiceStatus.PENDING);
        newInvoice.setType(InvoiceType.STANDARD);
        newInvoice.setCurrency("USD");
        newInvoice.setIssueDate(LocalDateTime.now());
        newInvoice.setDueDate(LocalDateTime.now().plusDays(30));

        // When
        Invoice savedInvoice = invoiceRepository.save(newInvoice);

        // Then
        assertNotNull(savedInvoice);
        assertNotNull(savedInvoice.getId());
        assertEquals("INV-002", savedInvoice.getInvoiceNumber());
        assertEquals(BigDecimal.valueOf(200.00), savedInvoice.getTotalAmount());
        assertEquals(InvoiceStatus.PENDING, savedInvoice.getStatus());
    }

    @Test
    void testUpdateInvoice_Success() {
        // Given
        testInvoice.setTotalAmount(BigDecimal.valueOf(150.00));
        testInvoice.setStatus(InvoiceStatus.PAID);

        // When
        Invoice updatedInvoice = invoiceRepository.save(testInvoice);

        // Then
        assertNotNull(updatedInvoice);
        assertEquals(testInvoice.getId(), updatedInvoice.getId());
        assertEquals(BigDecimal.valueOf(150.00), updatedInvoice.getTotalAmount());
        assertEquals(InvoiceStatus.PAID, updatedInvoice.getStatus());
    }

    @Test
    void testDeleteInvoice_Success() {
        // Given
        Long invoiceId = testInvoice.getId();

        // When
        invoiceRepository.deleteById(invoiceId);

        // Then
        Optional<Invoice> deletedInvoice = invoiceRepository.findById(invoiceId);
        assertFalse(deletedInvoice.isPresent());
    }

    @Test
    void testFindAllWithPagination_Success() {
        // Given
        PageRequest pageRequest = PageRequest.of(0, 10);

        // When
        Page<Invoice> result = invoiceRepository.findAll(pageRequest);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals(1L, result.getTotalElements());
        assertEquals(0, result.getNumber());
        assertEquals(10, result.getSize());
    }
}