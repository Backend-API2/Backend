package backend_api.Backend.Service.Implementation;

import backend_api.Backend.DTO.invoice.*;
import backend_api.Backend.Entity.invoice.*;
import backend_api.Backend.Entity.payment.Payment;
import backend_api.Backend.Repository.InvoiceLineRepository;
import backend_api.Backend.Repository.InvoiceRepository;
import backend_api.Backend.Repository.PaymentRepository;
import backend_api.Backend.Service.Interface.InvoiceEventService;
import backend_api.Backend.Service.Interface.InvoiceService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class InvoiceServiceImpl implements InvoiceService {
    
    private final InvoiceRepository invoiceRepository;
    private final InvoiceLineRepository invoiceLineRepository;
    private final PaymentRepository paymentRepository;
    private final InvoiceEventService invoiceEventService;
    
    @Override
    public InvoiceResponse createInvoice(CreateInvoiceRequest request) {
        log.info("Creando nueva factura para el pago: {}", request.getPaymentId());
        
        Payment payment = paymentRepository.findById(request.getPaymentId())
                .orElseThrow(() -> new EntityNotFoundException("Pago no encontrado: " + request.getPaymentId()));
        
        Invoice invoice = new Invoice();
        invoice.setPaymentId(request.getPaymentId());
        invoice.setUserId(request.getUserId());
        invoice.setProviderId(request.getProviderId());
        invoice.setType(request.getType());
        invoice.setDueDate(request.getDueDate());
        invoice.setCurrency(request.getCurrency());
        invoice.setLegalFields(request.getLegalFields());
        invoice.setNotes(request.getNotes());
        invoice.setMetadata(request.getMetadata());
        invoice.setIssueDate(LocalDateTime.now());
        
        invoice.setInvoiceNumber(generateInvoiceNumber(request.getProviderId()));
        
        calculateInvoiceTotals(invoice, request.getLines());
        
        Invoice savedInvoice = invoiceRepository.save(invoice);
        
        List<InvoiceLine> lines = request.getLines().stream()
                .map(lineRequest -> createInvoiceLineFromRequest(savedInvoice.getId(), lineRequest))
                .collect(Collectors.toList());
        
        List<InvoiceLine> savedLines = invoiceLineRepository.saveAll(lines);
        
        invoiceEventService.createEvent(
            savedInvoice.getId(),
            InvoiceEventType.INVOICE_CREATED,
            "Factura creada automáticamente desde pago",
            request.getProviderId()
        );
        
        log.info("Factura creada con éxito: {}", savedInvoice.getInvoiceNumber());
        return convertToResponse(savedInvoice, savedLines);
    }
    
    @Override
    public InvoiceResponse getInvoiceById(Long id) {
        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Factura no encontrada: " + id));
        
        List<InvoiceLine> lines = invoiceLineRepository.findByInvoiceIdOrderByLineNumber(id);
        
        return convertToResponse(invoice, lines);
    }
    
    @Override
    public InvoiceResponse getInvoiceByNumber(String invoiceNumber) {
        Invoice invoice = invoiceRepository.findByInvoiceNumber(invoiceNumber)
                .orElseThrow(() -> new EntityNotFoundException("Factura no encontrada: " + invoiceNumber));
        
        List<InvoiceLine> lines = invoiceLineRepository.findByInvoiceIdOrderByLineNumber(invoice.getId());
        
        return convertToResponse(invoice, lines);
    }
    
    @Override
    public InvoiceResponse updateInvoice(Long id, UpdateInvoiceRequest request) {
        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Factura no encontrada: " + id));
        
        if (!canModifyInvoice(id)) {
            throw new IllegalStateException("La factura no puede ser modificada en su estado actual");
        }
        
        if (request.getType() != null) invoice.setType(request.getType());
        if (request.getDueDate() != null) invoice.setDueDate(request.getDueDate());
        if (request.getCurrency() != null) invoice.setCurrency(request.getCurrency());
        if (request.getLegalFields() != null) invoice.setLegalFields(request.getLegalFields());
        if (request.getNotes() != null) invoice.setNotes(request.getNotes());
        if (request.getMetadata() != null) invoice.setMetadata(request.getMetadata());
        
        Invoice updatedInvoice = invoiceRepository.save(invoice);
        
        List<InvoiceLine> lines;
        if (request.getLines() != null && !request.getLines().isEmpty()) {
            lines = updateInvoiceLines(id, request.getLines());
            recalculateInvoiceTotals(updatedInvoice);
            updatedInvoice = invoiceRepository.save(updatedInvoice);
        } else {
            lines = invoiceLineRepository.findByInvoiceIdOrderByLineNumber(id);
        }
        
        invoiceEventService.createEvent(
            id,
            InvoiceEventType.INVOICE_UPDATED,
            "Factura actualizada",
            null 
        );
        
        return convertToResponse(updatedInvoice, lines);
    }
    
    
    @Override
    public void deleteInvoice(Long id) {
        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Factura no encontrada: " + id));
        
        if (!canModifyInvoice(id)) {
            throw new IllegalStateException("La factura no puede ser eliminada en su estado actual");
        }
        
        invoiceLineRepository.deleteByInvoiceId(id);
        
        invoiceEventService.createEvent(
            id,
            InvoiceEventType.INVOICE_DELETED,
            "Factura eliminada",
            null
        );
        
        invoiceRepository.delete(invoice);
    }
    
    @Override
    public InvoiceResponse updateInvoiceStatus(Long id, UpdateInvoiceStatusRequest request) {
        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Factura no encontrada: " + id));
        
        InvoiceStatus newStatus = InvoiceStatus.valueOf(request.getStatus().toUpperCase());
        InvoiceStatus oldStatus = invoice.getStatus();
        
        invoice.setStatus(newStatus);
        
        if (newStatus == InvoiceStatus.SENT && oldStatus != InvoiceStatus.SENT) {
            invoice.setSentAt(LocalDateTime.now());
        } else if (newStatus == InvoiceStatus.PAID && oldStatus != InvoiceStatus.PAID) {
            invoice.setPaidAt(LocalDateTime.now());
        }
        
        Invoice updatedInvoice = invoiceRepository.save(invoice);
        
        invoiceEventService.createEvent(
            id,
            getEventTypeForStatus(newStatus),
            String.format("Estado cambiado de %s a %s", oldStatus, newStatus),
            null
        );
        
        List<InvoiceLine> lines = invoiceLineRepository.findByInvoiceIdOrderByLineNumber(id);
        return convertToResponse(updatedInvoice, lines);
    }
    
    @Override
    public InvoiceResponse markAsSent(Long id) {
        UpdateInvoiceStatusRequest request = new UpdateInvoiceStatusRequest();
        request.setStatus("SENT");
        return updateInvoiceStatus(id, request);
    }
    
    @Override
    public InvoiceResponse markAsPaid(Long id) {
        UpdateInvoiceStatusRequest request = new UpdateInvoiceStatusRequest();
        request.setStatus("PAID");
        return updateInvoiceStatus(id, request);
    }
    
    @Override
    public InvoiceResponse markAsOverdue(Long id) {
        UpdateInvoiceStatusRequest request = new UpdateInvoiceStatusRequest();
        request.setStatus("OVERDUE");
        return updateInvoiceStatus(id, request);
    }
    
    @Override
    public InvoiceResponse cancelInvoice(Long id) {
        UpdateInvoiceStatusRequest request = new UpdateInvoiceStatusRequest();
        request.setStatus("CANCELED");
        return updateInvoiceStatus(id, request);
    }
    
    @Override
    public boolean canModifyInvoice(Long invoiceId) {
        Optional<Invoice> invoiceOpt = invoiceRepository.findById(invoiceId);
        if (invoiceOpt.isEmpty()) return false;
        
        Invoice invoice = invoiceOpt.get();
        return invoice.getStatus() != InvoiceStatus.PAID && 
               invoice.getStatus() != InvoiceStatus.CANCELED;
    }
    
    @Override
    public boolean validateInvoiceOwnership(Long invoiceId, Long userId) {
        Optional<Invoice> invoiceOpt = invoiceRepository.findById(invoiceId);
        if (invoiceOpt.isEmpty()) return false;
        
        Invoice invoice = invoiceOpt.get();
        return invoice.getUserId().equals(userId) || invoice.getProviderId().equals(userId);
    }
    
    private String generateInvoiceNumber(Long providerId) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String providerPrefix = String.format("P%04d", providerId);
        return String.format("INV-%s-%s", providerPrefix, timestamp);
    }
    
    private InvoiceEventType getEventTypeForStatus(InvoiceStatus status) {
        switch (status) {
            case SENT: return InvoiceEventType.INVOICE_SENT;
            case PAID: return InvoiceEventType.PAYMENT_COMPLETED;
            case CANCELED: return InvoiceEventType.INVOICE_CANCELED;
            case OVERDUE: return InvoiceEventType.INVOICE_OVERDUE;
            default: return InvoiceEventType.INVOICE_UPDATED;
        }
    }
    
    private void calculateInvoiceTotals(Invoice invoice, List<CreateInvoiceRequest.CreateInvoiceLineRequest> lines) {
        BigDecimal subtotal = BigDecimal.ZERO;
        BigDecimal taxAmount = BigDecimal.ZERO;
        BigDecimal discountAmount = BigDecimal.ZERO;
        
        for (CreateInvoiceRequest.CreateInvoiceLineRequest line : lines) {
            BigDecimal lineSubtotal = line.getUnitPrice().multiply(BigDecimal.valueOf(line.getQuantity()));
            subtotal = subtotal.add(lineSubtotal);
            
            if (line.getTaxAmount() != null) {
                taxAmount = taxAmount.add(line.getTaxAmount());
            }
            if (line.getDiscountAmount() != null) {
                discountAmount = discountAmount.add(line.getDiscountAmount());
            }
        }
        
        invoice.setSubtotalAmount(subtotal);
        invoice.setTaxAmount(taxAmount);
        invoice.setDiscountAmount(discountAmount);
        invoice.setTotalAmount(subtotal.add(taxAmount).subtract(discountAmount));
    }
    
    private InvoiceLine createInvoiceLineFromRequest(Long invoiceId, CreateInvoiceRequest.CreateInvoiceLineRequest request) {
        InvoiceLine line = new InvoiceLine();
        line.setInvoiceId(invoiceId);
        line.setProductId(request.getProductId());
        line.setDescription(request.getDescription());
        line.setProductName(request.getProductName());
        line.setProductCode(request.getProductCode());
        line.setQuantity(request.getQuantity());
        line.setUnitPrice(request.getUnitPrice());
        line.setTaxRate(request.getTaxRate());
        line.setTaxAmount(request.getTaxAmount());
        line.setDiscountRate(request.getDiscountRate());
        line.setDiscountAmount(request.getDiscountAmount());
        line.setLineNumber(request.getLineNumber());
        line.setUnitOfMeasure(request.getUnitOfMeasure());
        return line;
    }
    
    private List<InvoiceLine> updateInvoiceLines(Long invoiceId, List<UpdateInvoiceRequest.UpdateInvoiceLineRequest> lineRequests) {
        List<InvoiceLine> existingLines = invoiceLineRepository.findByInvoiceId(invoiceId);
        
        List<InvoiceLine> updatedLines = new ArrayList<>();
        
        for (UpdateInvoiceRequest.UpdateInvoiceLineRequest request : lineRequests) {
            if (request.getDeleted() != null && request.getDeleted()) {
                if (request.getId() != null) {
                    invoiceLineRepository.deleteById(request.getId());
                }
            } else if (request.getId() != null) {
                InvoiceLine existingLine = existingLines.stream()
                        .filter(line -> line.getId().equals(request.getId()))
                        .findFirst()
                        .orElse(new InvoiceLine());
                
                updateInvoiceLineFromRequest(existingLine, request);
                existingLine.setInvoiceId(invoiceId);
                updatedLines.add(existingLine);
            } else {
                InvoiceLine newLine = createInvoiceLineFromUpdateRequest(invoiceId, request);
                updatedLines.add(newLine);
            }
        }
        
        return invoiceLineRepository.saveAll(updatedLines);
    }
    
    private void updateInvoiceLineFromRequest(InvoiceLine line, UpdateInvoiceRequest.UpdateInvoiceLineRequest request) {
        if (request.getProductId() != null) line.setProductId(request.getProductId());
        if (request.getDescription() != null) line.setDescription(request.getDescription());
        if (request.getProductName() != null) line.setProductName(request.getProductName());
        if (request.getProductCode() != null) line.setProductCode(request.getProductCode());
        if (request.getQuantity() != null) line.setQuantity(request.getQuantity());
        if (request.getUnitPrice() != null) line.setUnitPrice(request.getUnitPrice());
        if (request.getTaxRate() != null) line.setTaxRate(request.getTaxRate());
        if (request.getTaxAmount() != null) line.setTaxAmount(request.getTaxAmount());
        if (request.getDiscountRate() != null) line.setDiscountRate(request.getDiscountRate());
        if (request.getDiscountAmount() != null) line.setDiscountAmount(request.getDiscountAmount());
        if (request.getLineNumber() != null) line.setLineNumber(request.getLineNumber());
        if (request.getUnitOfMeasure() != null) line.setUnitOfMeasure(request.getUnitOfMeasure());
    }
    
    private InvoiceLine createInvoiceLineFromUpdateRequest(Long invoiceId, UpdateInvoiceRequest.UpdateInvoiceLineRequest request) {
        InvoiceLine line = new InvoiceLine();
        line.setInvoiceId(invoiceId);
        updateInvoiceLineFromRequest(line, request);
        return line;
    }
    
    private void recalculateInvoiceTotals(Invoice invoice) {
        List<InvoiceLine> lines = invoiceLineRepository.findByInvoiceId(invoice.getId());
        
        BigDecimal subtotal = BigDecimal.ZERO;
        BigDecimal taxAmount = BigDecimal.ZERO;
        BigDecimal discountAmount = BigDecimal.ZERO;
        
        for (InvoiceLine line : lines) {
            if (line.getSubtotal() != null) subtotal = subtotal.add(line.getSubtotal());
            if (line.getTaxAmount() != null) taxAmount = taxAmount.add(line.getTaxAmount());
            if (line.getDiscountAmount() != null) discountAmount = discountAmount.add(line.getDiscountAmount());
        }
        
        invoice.setSubtotalAmount(subtotal);
        invoice.setTaxAmount(taxAmount);
        invoice.setDiscountAmount(discountAmount);
        invoice.setTotalAmount(subtotal.add(taxAmount).subtract(discountAmount));
    }
    
    private InvoiceResponse convertToResponse(Invoice invoice, List<InvoiceLine> lines) {
        List<InvoiceResponse.InvoiceLineResponse> lineResponses = lines.stream()
                .map(this::convertLineToResponse)
                .collect(Collectors.toList());
        
        return InvoiceResponse.builder()
                .id(invoice.getId())
                .paymentId(invoice.getPaymentId())
                .invoiceNumber(invoice.getInvoiceNumber())
                .issueDate(invoice.getIssueDate())
                .dueDate(invoice.getDueDate())
                .totalAmount(invoice.getTotalAmount())
                .subtotalAmount(invoice.getSubtotalAmount())
                .taxAmount(invoice.getTaxAmount())
                .discountAmount(invoice.getDiscountAmount())
                .status(invoice.getStatus())
                .type(invoice.getType())
                .userId(invoice.getUserId())
                .providerId(invoice.getProviderId())
                .currency(invoice.getCurrency())
                .legalFields(invoice.getLegalFields())
                .pdfUrl(invoice.getPdfUrl())
                .notes(invoice.getNotes())
                .createdAt(invoice.getCreatedAt())
                .updatedAt(invoice.getUpdatedAt())
                .sentAt(invoice.getSentAt())
                .paidAt(invoice.getPaidAt())
                .metadata(invoice.getMetadata())
                .lines(lineResponses)
                .build();
    }
    
    private InvoiceResponse.InvoiceLineResponse convertLineToResponse(InvoiceLine line) {
        return InvoiceResponse.InvoiceLineResponse.builder()
                .id(line.getId())
                .invoiceId(line.getInvoiceId())
                .productId(line.getProductId())
                .description(line.getDescription())
                .productName(line.getProductName())
                .productCode(line.getProductCode())
                .quantity(line.getQuantity())
                .unitPrice(line.getUnitPrice())
                .subtotal(line.getSubtotal())
                .taxRate(line.getTaxRate())
                .taxAmount(line.getTaxAmount())
                .discountRate(line.getDiscountRate())
                .discountAmount(line.getDiscountAmount())
                .totalAmount(line.getTotalAmount())
                .lineNumber(line.getLineNumber())
                .unitOfMeasure(line.getUnitOfMeasure())
                .createdAt(line.getCreatedAt())
                .updatedAt(line.getUpdatedAt())
                .build();
    }
    
    
    @Override
    public Page<InvoiceResponse> searchInvoices(InvoiceSearchRequest request) {
        Pageable pageable = PageRequest.of(
            request.getPage(), 
            request.getSize(), 
            Sort.by(Sort.Direction.fromString(request.getSortDirection()), request.getSortBy())
        );
        
        Page<Invoice> invoices = invoiceRepository.findByFilters(
            request.getUserId(),
            request.getProviderId(),
            request.getStatus(),
            request.getType(),
            request.getStartDate(),
            request.getEndDate(),
            request.getMinAmount(),
            request.getMaxAmount(),
            request.getInvoiceNumber(),
            pageable
        );
        
        return invoices.map(invoice -> {
            List<InvoiceLine> lines = invoiceLineRepository.findByInvoiceIdOrderByLineNumber(invoice.getId());
            return convertToResponse(invoice, lines);
        });
    }
    
    @Override
    public Page<InvoiceResponse> getInvoicesByUserId(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Invoice> invoices = invoiceRepository.findByUserId(userId, pageable);
        
        return invoices.map(invoice -> {
            List<InvoiceLine> lines = invoiceLineRepository.findByInvoiceIdOrderByLineNumber(invoice.getId());
            return convertToResponse(invoice, lines);
        });
    }
    
    @Override
    public Page<InvoiceResponse> getInvoicesByProviderId(Long providerId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Invoice> invoices = invoiceRepository.findByProviderId(providerId, pageable);
        
        return invoices.map(invoice -> {
            List<InvoiceLine> lines = invoiceLineRepository.findByInvoiceIdOrderByLineNumber(invoice.getId());
            return convertToResponse(invoice, lines);
        });
    }
    
    @Override
    public Page<InvoiceResponse> getInvoicesByStatus(InvoiceStatus status, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Invoice> invoices = invoiceRepository.findByStatus(status, pageable);
        
        return invoices.map(invoice -> {
            List<InvoiceLine> lines = invoiceLineRepository.findByInvoiceIdOrderByLineNumber(invoice.getId());
            return convertToResponse(invoice, lines);
        });
    }
    
    @Override
    public List<InvoiceResponse> getInvoicesByPaymentId(Long paymentId) {
        List<Invoice> invoices = invoiceRepository.findByPaymentId(paymentId);
        
        return invoices.stream().map(invoice -> {
            List<InvoiceLine> lines = invoiceLineRepository.findByInvoiceIdOrderByLineNumber(invoice.getId());
            return convertToResponse(invoice, lines);
        }).collect(Collectors.toList());
    }
    
    @Override
    public String generatePdf(Long id) {
        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Factura no encontrada: " + id));
        
        String pdfUrl = String.format("/api/invoices/%d/pdf", id);
        invoice.setPdfUrl(pdfUrl);
        invoiceRepository.save(invoice);
        
        invoiceEventService.createEvent(
            id,
            InvoiceEventType.PDF_GENERATED,
            "PDF generado exitosamente",
            null
        );
        
        return pdfUrl;
    }
    
    @Override
    public String regeneratePdf(Long id) {
        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Factura no encontrada: " + id));
        
        String pdfUrl = String.format("/api/invoices/%d/pdf?v=%d", id, System.currentTimeMillis());
        invoice.setPdfUrl(pdfUrl);
        invoiceRepository.save(invoice);
        
        invoiceEventService.createEvent(
            id,
            InvoiceEventType.PDF_REGENERATED,
            "PDF regenerado",
            null
        );
        
        return pdfUrl;
    }
    
    @Override
    public byte[] downloadPdf(Long id) {
        invoiceRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Factura no encontrada: " + id));
        
        String content = "PDF content for invoice " + id;
        return content.getBytes();
    }
    
    @Override
    public List<InvoiceEventResponse> getInvoiceTimeline(Long id) {
        if (!invoiceRepository.existsById(id)) {
            throw new EntityNotFoundException("Factura no encontrada: " + id);
        }
        
        return invoiceEventService.getEventsByInvoiceId(id);
    }
    
    @Override
    public InvoiceSummaryResponse getInvoiceSummary(Long providerId) {
        long totalInvoices = invoiceRepository.findByProviderId(providerId, Pageable.unpaged()).getTotalElements();
        long paidInvoices = invoiceRepository.countByProviderIdAndStatus(providerId, InvoiceStatus.PAID);
        long pendingInvoices = invoiceRepository.countByProviderIdAndStatus(providerId, InvoiceStatus.PENDING);
        long overdueInvoices = invoiceRepository.countByProviderIdAndStatus(providerId, InvoiceStatus.OVERDUE);
        
        BigDecimal totalAmount = invoiceRepository.getTotalAmountByProviderAndStatus(providerId, InvoiceStatus.PAID) != null ? 
            invoiceRepository.getTotalAmountByProviderAndStatus(providerId, InvoiceStatus.PAID) : BigDecimal.ZERO;
        BigDecimal paidAmount = invoiceRepository.getTotalAmountByProviderAndStatus(providerId, InvoiceStatus.PAID) != null ? 
            invoiceRepository.getTotalAmountByProviderAndStatus(providerId, InvoiceStatus.PAID) : BigDecimal.ZERO;
        BigDecimal pendingAmount = invoiceRepository.getTotalAmountByProviderAndStatus(providerId, InvoiceStatus.PENDING) != null ? 
            invoiceRepository.getTotalAmountByProviderAndStatus(providerId, InvoiceStatus.PENDING) : BigDecimal.ZERO;
        BigDecimal overdueAmount = invoiceRepository.getTotalAmountByProviderAndStatus(providerId, InvoiceStatus.OVERDUE) != null ? 
            invoiceRepository.getTotalAmountByProviderAndStatus(providerId, InvoiceStatus.OVERDUE) : BigDecimal.ZERO;
            
        return InvoiceSummaryResponse.builder()
            .totalInvoices(totalInvoices)
            .paidInvoices(paidInvoices)
            .pendingInvoices(pendingInvoices)
            .overdueInvoices(overdueInvoices)
            .totalAmount(totalAmount)
            .paidAmount(paidAmount)
            .pendingAmount(pendingAmount)
            .overdueAmount(overdueAmount)
            .build();
    }
    
    @Override
    public InvoiceSummaryResponse getInvoiceSummaryByUser(Long userId) {
        long totalInvoices = invoiceRepository.findByUserId(userId, Pageable.unpaged()).getTotalElements();
        long paidInvoices = invoiceRepository.countByUserIdAndStatus(userId, InvoiceStatus.PAID);
        long pendingInvoices = invoiceRepository.countByUserIdAndStatus(userId, InvoiceStatus.PENDING);
        long overdueInvoices = invoiceRepository.countByUserIdAndStatus(userId, InvoiceStatus.OVERDUE);
            
        return InvoiceSummaryResponse.builder()
            .totalInvoices(totalInvoices)
            .paidInvoices(paidInvoices)
            .pendingInvoices(pendingInvoices)
            .overdueInvoices(overdueInvoices)
            .totalAmount(BigDecimal.ZERO) // TODO: Implementar cálculo
            .paidAmount(BigDecimal.ZERO)
            .pendingAmount(BigDecimal.ZERO)
            .overdueAmount(BigDecimal.ZERO)
            .build();
    }
    
    @Override
    public void processOverdueInvoices() {
        List<Invoice> overdueInvoices = invoiceRepository.findOverdueInvoices();
        
        for (Invoice invoice : overdueInvoices) {
            invoice.setStatus(InvoiceStatus.OVERDUE);
            invoiceRepository.save(invoice);
            
            invoiceEventService.createEvent(
                invoice.getId(),
                InvoiceEventType.INVOICE_OVERDUE,
                "Factura marcada como vencida automáticamente",
                null
            );
        }

        log.info("Procesando {} facturas vencidas", overdueInvoices.size());
    }
    
    @Override
    public void sendDueReminders() {
        LocalDateTime reminderDate = LocalDateTime.now().plusDays(3);
        List<Invoice> invoicesDueSoon = invoiceRepository.findInvoicesDueSoon(reminderDate);
        
        for (Invoice invoice : invoicesDueSoon) {
            invoiceEventService.createEvent(
                invoice.getId(),
                InvoiceEventType.REMINDER_SENT,
                "Recordatorio de vencimiento enviado",
                null
            );
        }

        log.info("Se enviaron recordatorios para {} facturas que vencen pronto", invoicesDueSoon.size());
    }
    
    @Override
    public List<InvoiceResponse> getInvoicesDueSoon(int days) {
        LocalDateTime futureDate = LocalDateTime.now().plusDays(days);
        List<Invoice> invoices = invoiceRepository.findInvoicesDueSoon(futureDate);
        
        return invoices.stream().map(invoice -> {
            List<InvoiceLine> lines = invoiceLineRepository.findByInvoiceIdOrderByLineNumber(invoice.getId());
            return convertToResponse(invoice, lines);
        }).collect(Collectors.toList());
    }
    
    @Override
    public InvoiceResponse createInvoiceFromPayment(Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new EntityNotFoundException("Pago no encontrado: " + paymentId));
        
        List<Invoice> existingInvoices = invoiceRepository.findByPaymentId(paymentId);
        if (!existingInvoices.isEmpty()) {
            throw new IllegalStateException("Ya existe una factura para este pago");
        }
        
        CreateInvoiceRequest request = new CreateInvoiceRequest();
        request.setPaymentId(paymentId);
        request.setUserId(payment.getUser_id());
        request.setProviderId(payment.getProvider_id());
        request.setType(InvoiceType.STANDARD);
        request.setCurrency(payment.getCurrency());
        request.setDueDate(LocalDateTime.now().plusDays(30)); // 30 días por defecto
        
        CreateInvoiceRequest.CreateInvoiceLineRequest line = new CreateInvoiceRequest.CreateInvoiceLineRequest();
        line.setDescription("Pago por servicios");
        line.setQuantity(1);
        line.setUnitPrice(payment.getAmount_total());
        line.setLineNumber(1);
        
        request.setLines(List.of(line));
        
        return createInvoice(request);
    }
}
