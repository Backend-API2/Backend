package backend_api.Backend.Service.Implementation;

import backend_api.Backend.DTO.invoice.*;
import backend_api.Backend.Entity.invoice.*;
import backend_api.Backend.Entity.payment.Payment;
import backend_api.Backend.Entity.payment.PaymentStatus;
import backend_api.Backend.Repository.InvoiceLineRepository;
import backend_api.Backend.Repository.InvoiceRepository;
import backend_api.Backend.Repository.PaymentRepository;
import backend_api.Backend.Service.Interface.InvoiceEventService;
import backend_api.Backend.Service.Common.EntityValidationService;
import backend_api.Backend.Service.Common.InvoiceCalculationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InvoiceServiceImplTest {

    @Mock
    private InvoiceRepository invoiceRepository;

    @Mock
    private InvoiceLineRepository invoiceLineRepository;

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private InvoiceEventService invoiceEventService;

    @Mock
    private EntityValidationService entityValidationService;

    @Mock
    private InvoiceCalculationService invoiceCalculationService;

    @InjectMocks
    private InvoiceServiceImpl invoiceService;

    private Invoice testInvoice;
    private Payment testPayment;
    private CreateInvoiceRequest createRequest;

    @BeforeEach
    void setUp() {
        // Setup test data
        testPayment = new Payment();
        testPayment.setId(1L);
        testPayment.setStatus(PaymentStatus.COMPLETED);

        testInvoice = new Invoice();
        testInvoice.setId(1L);
        testInvoice.setInvoiceNumber("INV-001");
        testInvoice.setPaymentId(1L);
        testInvoice.setUserId(1L);
        testInvoice.setProviderId(1L);
        testInvoice.setTotalAmount(BigDecimal.valueOf(100.00));
        testInvoice.setStatus(InvoiceStatus.PENDING);
        testInvoice.setIssueDate(LocalDateTime.now());
        testInvoice.setDueDate(LocalDateTime.now().plusDays(30));

        createRequest = new CreateInvoiceRequest();
        createRequest.setPaymentId(1L);
        createRequest.setUserId(1L);
        createRequest.setProviderId(1L);
        createRequest.setType(InvoiceType.STANDARD);
        createRequest.setCurrency("USD");
        
        // Create invoice line
        CreateInvoiceRequest.CreateInvoiceLineRequest line = new CreateInvoiceRequest.CreateInvoiceLineRequest();
        line.setDescription("Test Product");
        line.setQuantity(1);
        line.setUnitPrice(BigDecimal.valueOf(100.00));
        line.setLineNumber(1);
        createRequest.setLines(Arrays.asList(line));

        // Setup mocks (using lenient to avoid unnecessary stubbing errors)
        lenient().when(entityValidationService.getPaymentOrThrow(anyLong())).thenReturn(testPayment);
        lenient().when(entityValidationService.getInvoiceOrThrow(anyLong())).thenReturn(testInvoice);
        lenient().when(entityValidationService.getInvoiceByNumberOrThrow(anyString())).thenReturn(testInvoice);
        lenient().doNothing().when(invoiceCalculationService).calculateInvoiceTotals(any(), any());
    }

    @Test
    void testCreateInvoice_Success() {
        // Given
        when(invoiceRepository.save(any(Invoice.class))).thenReturn(testInvoice);

        // When
        InvoiceResponse response = invoiceService.createInvoice(createRequest);

        // Then
        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("INV-001", response.getInvoiceNumber());
        assertEquals(BigDecimal.valueOf(100.00), response.getTotalAmount());
        assertEquals(InvoiceStatus.PENDING, response.getStatus());

        verify(entityValidationService).getPaymentOrThrow(1L);
        verify(invoiceRepository).save(any(Invoice.class));
    }

    @Test
    void testCreateInvoice_PaymentNotFound() {
        // Given
        when(entityValidationService.getPaymentOrThrow(1L)).thenThrow(new RuntimeException("Payment not found"));

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            invoiceService.createInvoice(createRequest);
        });

        verify(entityValidationService).getPaymentOrThrow(1L);
        verify(invoiceRepository, never()).save(any(Invoice.class));
    }

    @Test
    void testGetInvoiceById_Success() {
        // Given
        // EntityValidationService mock is already set up in setUp()

        // When
        InvoiceResponse response = invoiceService.getInvoiceById(1L);

        // Then
        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("INV-001", response.getInvoiceNumber());
        assertEquals(BigDecimal.valueOf(100.00), response.getTotalAmount());

        verify(entityValidationService).getInvoiceOrThrow(1L);
    }

    @Test
    void testGetInvoiceById_NotFound() {
        // Given
        when(entityValidationService.getInvoiceOrThrow(1L)).thenThrow(new RuntimeException("Invoice not found"));

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            invoiceService.getInvoiceById(1L);
        });

        verify(entityValidationService).getInvoiceOrThrow(1L);
    }

    @Test
    void testGetInvoiceByNumber_Success() {
        // Given
        // EntityValidationService mock is already set up in setUp()

        // When
        InvoiceResponse response = invoiceService.getInvoiceByNumber("INV-001");

        // Then
        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("INV-001", response.getInvoiceNumber());

        verify(entityValidationService).getInvoiceByNumberOrThrow("INV-001");
    }

    @Test
    void testGetInvoiceByNumber_NotFound() {
        // Given
        when(entityValidationService.getInvoiceByNumberOrThrow("INV-999")).thenThrow(new RuntimeException("Invoice not found"));

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            invoiceService.getInvoiceByNumber("INV-999");
        });

        verify(entityValidationService).getInvoiceByNumberOrThrow("INV-999");
    }

    @Test
    void testDeleteInvoice_Success() {
        // Given
        when(invoiceRepository.findById(1L)).thenReturn(Optional.of(testInvoice));
        doNothing().when(invoiceRepository).delete(any(Invoice.class));

        // When
        invoiceService.deleteInvoice(1L);

        // Then
        verify(invoiceRepository, atLeastOnce()).findById(1L);
        verify(invoiceRepository).delete(any(Invoice.class));
    }

    @Test
    void testDeleteInvoice_NotFound() {
        // Given
        when(invoiceRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            invoiceService.deleteInvoice(1L);
        });

        verify(invoiceRepository).findById(1L);
        verify(invoiceRepository, never()).delete(any(Invoice.class));
    }

    @Test
    void testGetInvoicesByUserId_Success() {
        // Given
        Long userId = 1L;
        int page = 0;
        int size = 10;

        Page<Invoice> invoicePage = new PageImpl<>(
            Arrays.asList(testInvoice),
            PageRequest.of(page, size),
            1L
        );

        when(invoiceRepository.findByUserId(eq(userId), any(PageRequest.class))).thenReturn(invoicePage);

        // When
        Page<InvoiceResponse> response = invoiceService.getInvoicesByUserId(userId, page, size);

        // Then
        assertNotNull(response);
        assertEquals(1, response.getContent().size());
        assertEquals(1L, response.getTotalElements());

        verify(invoiceRepository).findByUserId(eq(userId), any(PageRequest.class));
    }

    @Test
    void testGetInvoicesByStatus_Success() {
        // Given
        InvoiceStatus status = InvoiceStatus.PENDING;
        int page = 0;
        int size = 10;

        Page<Invoice> invoicePage = new PageImpl<>(
            Arrays.asList(testInvoice),
            PageRequest.of(page, size),
            1L
        );

        when(invoiceRepository.findByStatus(eq(status), any(PageRequest.class))).thenReturn(invoicePage);

        // When
        Page<InvoiceResponse> response = invoiceService.getInvoicesByStatus(status, page, size);

        // Then
        assertNotNull(response);
        assertEquals(1, response.getContent().size());
        assertEquals(1L, response.getTotalElements());

        verify(invoiceRepository).findByStatus(eq(status), any(PageRequest.class));
    }

    @Test
    void testGetInvoicesByPaymentId_Success() {
        // Given
        Long paymentId = 1L;
        List<Invoice> invoices = Arrays.asList(testInvoice);

        when(invoiceRepository.findByPaymentId(paymentId)).thenReturn(invoices);

        // When
        List<InvoiceResponse> response = invoiceService.getInvoicesByPaymentId(paymentId);

        // Then
        assertNotNull(response);
        assertEquals(1, response.size());
        assertEquals(1L, response.get(0).getId());

        verify(invoiceRepository).findByPaymentId(paymentId);
    }

    @Test
    void testGeneratePdf_Success() {
        // Given
        when(invoiceRepository.save(any(Invoice.class))).thenReturn(testInvoice);

        // When
        String pdfUrl = invoiceService.generatePdf(1L);

        // Then
        assertNotNull(pdfUrl);
        assertTrue(pdfUrl.contains("invoice"));
        assertTrue(pdfUrl.contains("1"));

        verify(entityValidationService).getInvoiceOrThrow(1L);
        verify(invoiceRepository).save(any(Invoice.class));
    }

    @Test
    void testCreateInvoiceFromPayment_Success() {
        // Given
        Long paymentId = 1L;
        
        // Setup test payment with required fields
        testPayment.setUser_id(1L);
        testPayment.setProvider_id(1L);
        testPayment.setCurrency("USD");
        testPayment.setAmount_total(BigDecimal.valueOf(100.00));

        when(invoiceRepository.save(any(Invoice.class))).thenReturn(testInvoice);

        // When
        InvoiceResponse response = invoiceService.createInvoiceFromPayment(paymentId);

        // Then
        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("INV-001", response.getInvoiceNumber());

        verify(entityValidationService, atLeastOnce()).getPaymentOrThrow(paymentId);
        verify(invoiceRepository).save(any(Invoice.class));
    }

    @Test
    void testCreateInvoiceFromPayment_PaymentNotFound() {
        // Given
        Long paymentId = 1L;

        when(entityValidationService.getPaymentOrThrow(paymentId)).thenThrow(new RuntimeException("Payment not found"));

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            invoiceService.createInvoiceFromPayment(paymentId);
        });

        verify(entityValidationService).getPaymentOrThrow(paymentId);
        verify(invoiceRepository, never()).save(any(Invoice.class));
    }
}