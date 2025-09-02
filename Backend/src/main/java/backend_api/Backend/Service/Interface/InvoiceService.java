package backend_api.Backend.Service.Interface;

import backend_api.Backend.DTO.invoice.*;
import backend_api.Backend.Entity.invoice.InvoiceStatus;
import org.springframework.data.domain.Page;

import java.util.List;

public interface InvoiceService {
    
    InvoiceResponse createInvoice(CreateInvoiceRequest request);
    InvoiceResponse getInvoiceById(Long id);
    InvoiceResponse getInvoiceByNumber(String invoiceNumber);
    InvoiceResponse updateInvoice(Long id, UpdateInvoiceRequest request);
    void deleteInvoice(Long id);
    
    InvoiceResponse updateInvoiceStatus(Long id, UpdateInvoiceStatusRequest request);
    InvoiceResponse markAsSent(Long id);
    InvoiceResponse markAsPaid(Long id);
    InvoiceResponse markAsOverdue(Long id);
    InvoiceResponse cancelInvoice(Long id);
    
    Page<InvoiceResponse> searchInvoices(InvoiceSearchRequest request);
    Page<InvoiceResponse> getInvoicesByUserId(Long userId, int page, int size);
    Page<InvoiceResponse> getInvoicesByProviderId(Long providerId, int page, int size);
    Page<InvoiceResponse> getInvoicesByStatus(InvoiceStatus status, int page, int size);
    List<InvoiceResponse> getInvoicesByPaymentId(Long paymentId);
    
    String generatePdf(Long id);
    String regeneratePdf(Long id);
    byte[] downloadPdf(Long id);
    
    List<InvoiceEventResponse> getInvoiceTimeline(Long id);
    
    InvoiceSummaryResponse getInvoiceSummary(Long providerId);
    InvoiceSummaryResponse getInvoiceSummaryByUser(Long userId);
    
    void processOverdueInvoices();
    void sendDueReminders();
    List<InvoiceResponse> getInvoicesDueSoon(int days);
    
    boolean validateInvoiceOwnership(Long invoiceId, Long userId);
    boolean canModifyInvoice(Long invoiceId);
    
    InvoiceResponse createInvoiceFromPayment(Long paymentId);
}
