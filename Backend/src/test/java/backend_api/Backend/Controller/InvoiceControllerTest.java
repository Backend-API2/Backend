package backend_api.Backend.Controller;

import backend_api.Backend.Auth.JwtUtil;
import backend_api.Backend.DTO.invoice.*;
import backend_api.Backend.Entity.invoice.InvoiceStatus;
import backend_api.Backend.Entity.user.User;
import backend_api.Backend.Entity.user.UserRole;
import backend_api.Backend.Repository.UserRepository;
import backend_api.Backend.Service.Interface.InvoiceService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InvoiceControllerTest {

    @Mock
    private InvoiceService invoiceService;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private InvoiceController invoiceController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(invoiceController).build();
        objectMapper = new ObjectMapper();
    }

    @Test
    void testCreateInvoice_Success() {
        // Given
        CreateInvoiceRequest request = new CreateInvoiceRequest();
        request.setPaymentId(1L);
        request.setUserId(1L);
        request.setProviderId(1L);

        InvoiceResponse expectedResponse = InvoiceResponse.builder()
                .id(1L)
                .invoiceNumber("INV-001")
                .totalAmount(BigDecimal.valueOf(100.00))
                .status(InvoiceStatus.PENDING)
                .build();

        when(invoiceService.createInvoice(request)).thenReturn(expectedResponse);

        // When
        ResponseEntity<InvoiceResponse> response = invoiceController.createInvoice(request);

        // Then
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1L, response.getBody().getId());
        assertEquals("INV-001", response.getBody().getInvoiceNumber());
        assertEquals(BigDecimal.valueOf(100.00), response.getBody().getTotalAmount());
        assertEquals(InvoiceStatus.PENDING, response.getBody().getStatus());

        verify(invoiceService).createInvoice(request);
    }

    @Test
    void testGetInvoiceById_Success() {
        // Given
        Long invoiceId = 1L;
        InvoiceResponse expectedResponse = InvoiceResponse.builder()
                .id(invoiceId)
                .invoiceNumber("INV-001")
                .totalAmount(BigDecimal.valueOf(100.00))
                .status(InvoiceStatus.PENDING)
                .build();

        when(invoiceService.getInvoiceById(invoiceId)).thenReturn(expectedResponse);

        // When
        ResponseEntity<InvoiceResponse> response = invoiceController.getInvoiceById(invoiceId);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(invoiceId, response.getBody().getId());
        assertEquals("INV-001", response.getBody().getInvoiceNumber());

        verify(invoiceService).getInvoiceById(invoiceId);
    }

    @Test
    void testGetInvoiceByNumber_Success() {
        // Given
        String invoiceNumber = "INV-001";
        InvoiceResponse expectedResponse = InvoiceResponse.builder()
                .id(1L)
                .invoiceNumber(invoiceNumber)
                .totalAmount(BigDecimal.valueOf(100.00))
                .status(InvoiceStatus.PENDING)
                .build();

        when(invoiceService.getInvoiceByNumber(invoiceNumber)).thenReturn(expectedResponse);

        // When
        ResponseEntity<InvoiceResponse> response = invoiceController.getInvoiceByNumber(invoiceNumber);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(invoiceNumber, response.getBody().getInvoiceNumber());

        verify(invoiceService).getInvoiceByNumber(invoiceNumber);
    }

    @Test
    void testDeleteInvoice_Success() {
        // Given
        Long invoiceId = 1L;

        doNothing().when(invoiceService).deleteInvoice(invoiceId);

        // When
        ResponseEntity<Map<String, String>> response = invoiceController.deleteInvoice(invoiceId);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Factura eliminada exitosamente", response.getBody().get("message"));

        verify(invoiceService).deleteInvoice(invoiceId);
    }

    @Test
    void testGetInvoicesByUserId_Success() {
        // Given
        Long userId = 1L;
        int page = 0;
        int size = 10;

        InvoiceResponse invoice = InvoiceResponse.builder()
                .id(1L)
                .invoiceNumber("INV-001")
                .userId(userId)
                .build();

        Page<InvoiceResponse> expectedPage = new PageImpl<>(
            Arrays.asList(invoice),
            PageRequest.of(page, size),
            1L
        );

        when(invoiceService.getInvoicesByUserId(userId, page, size)).thenReturn(expectedPage);

        // When
        ResponseEntity<Page<InvoiceResponse>> response = invoiceController.getInvoicesByUserId(userId, page, size);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().getContent().size());
        assertEquals(userId, response.getBody().getContent().get(0).getUserId());

        verify(invoiceService).getInvoicesByUserId(userId, page, size);
    }

    @Test
    void testGetInvoicesByStatus_Success() {
        // Given
        String status = "PENDING";
        int page = 0;
        int size = 10;

        InvoiceResponse invoice = InvoiceResponse.builder()
                .id(1L)
                .invoiceNumber("INV-001")
                .status(InvoiceStatus.PENDING)
                .build();

        Page<InvoiceResponse> expectedPage = new PageImpl<>(
            Arrays.asList(invoice),
            PageRequest.of(page, size),
            1L
        );

        when(invoiceService.getInvoicesByStatus(InvoiceStatus.PENDING, page, size)).thenReturn(expectedPage);

        // When
        ResponseEntity<Page<InvoiceResponse>> response = invoiceController.getInvoicesByStatus(status, page, size);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().getContent().size());
        assertEquals(InvoiceStatus.PENDING, response.getBody().getContent().get(0).getStatus());

        verify(invoiceService).getInvoicesByStatus(InvoiceStatus.PENDING, page, size);
    }

    @Test
    void testGetInvoicesByPaymentId_Success() {
        // Given
        Long paymentId = 1L;

        InvoiceResponse invoice1 = InvoiceResponse.builder()
                .id(1L)
                .invoiceNumber("INV-001")
                .paymentId(paymentId)
                .build();

        InvoiceResponse invoice2 = InvoiceResponse.builder()
                .id(2L)
                .invoiceNumber("INV-002")
                .paymentId(paymentId)
                .build();

        List<InvoiceResponse> expectedInvoices = Arrays.asList(invoice1, invoice2);

        when(invoiceService.getInvoicesByPaymentId(paymentId)).thenReturn(expectedInvoices);

        // When
        ResponseEntity<List<InvoiceResponse>> response = invoiceController.getInvoicesByPaymentId(paymentId);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());
        assertEquals(paymentId, response.getBody().get(0).getPaymentId());
        assertEquals(paymentId, response.getBody().get(1).getPaymentId());

        verify(invoiceService).getInvoicesByPaymentId(paymentId);
    }

    @Test
    void testGeneratePdf_Success() {
        // Given
        Long invoiceId = 1L;
        String expectedPdfUrl = "http://localhost:8080/api/invoices/1/pdf/download";

        when(invoiceService.generatePdf(invoiceId)).thenReturn(expectedPdfUrl);

        // When
        ResponseEntity<Map<String, String>> response = invoiceController.generatePdf(invoiceId);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(expectedPdfUrl, response.getBody().get("pdfUrl"));
        assertEquals("PDF generado exitosamente", response.getBody().get("message"));

        verify(invoiceService).generatePdf(invoiceId);
    }

    @Test
    void testGetMyInvoices_Success() {
        // Given
        String token = "valid-jwt-token";
        String authHeader = "Bearer " + token;
        int page = 0;
        int size = 10;

        User user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");
        user.setRole(UserRole.USER);

        InvoiceResponse invoice = InvoiceResponse.builder()
                .id(1L)
                .invoiceNumber("INV-001")
                .userId(1L)
                .build();

        Page<InvoiceResponse> expectedPage = new PageImpl<>(
            Arrays.asList(invoice),
            PageRequest.of(page, size),
            1L
        );

        when(jwtUtil.getSubject(token)).thenReturn("test@example.com");
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(invoiceService.getInvoicesByUserId(1L, page, size)).thenReturn(expectedPage);

        // When
        ResponseEntity<Page<InvoiceResponse>> response = invoiceController.getMyInvoices(page, size, authHeader);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().getContent().size());
        assertEquals(1L, response.getBody().getContent().get(0).getUserId());

        verify(jwtUtil).getSubject(token);
        verify(userRepository).findByEmail("test@example.com");
        verify(invoiceService).getInvoicesByUserId(1L, page, size);
    }

    @Test
    void testGetMyInvoices_InvalidToken() {
        // Given
        String token = "invalid-jwt-token";
        String authHeader = "Bearer " + token;
        int page = 0;
        int size = 10;

        when(jwtUtil.getSubject(token)).thenReturn(null);

        // When
        ResponseEntity<Page<InvoiceResponse>> response = invoiceController.getMyInvoices(page, size, authHeader);

        // Then
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNull(response.getBody());

        verify(jwtUtil).getSubject(token);
        verify(userRepository, never()).findByEmail(anyString());
    }
}