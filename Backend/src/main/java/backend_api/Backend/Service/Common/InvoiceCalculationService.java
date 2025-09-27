 package backend_api.Backend.Service.Common;

import backend_api.Backend.DTO.invoice.CreateInvoiceRequest;
import backend_api.Backend.DTO.invoice.UpdateInvoiceRequest;
import backend_api.Backend.Entity.invoice.Invoice;
import backend_api.Backend.Entity.invoice.InvoiceLine;
import backend_api.Backend.Repository.InvoiceLineRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class InvoiceCalculationService {
    private final InvoiceLineRepository invoiceLineRepository;

    public void calculateInvoiceTotals(Invoice invoice , List<CreateInvoiceRequest.CreateInvoiceLineRequest> lines) {
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

        setInvoiceTotals(invoice, subtotal, taxAmount, discountAmount);
    }

    public void recalculateInvoiceTotals(Invoice invoice) {
        List<InvoiceLine> lines = invoiceLineRepository.findByInvoiceId(invoice.getId());

        BigDecimal subtotal = BigDecimal.ZERO;
        BigDecimal taxAmount = BigDecimal.ZERO;
        BigDecimal discountAmount = BigDecimal.ZERO;

        for (InvoiceLine line : lines) {
            if (line.getSubtotal() != null) {
                subtotal = subtotal.add(line.getSubtotal());
            }

            if (line.getTaxAmount() != null) {
                taxAmount = taxAmount.add(line.getTaxAmount());
            }
            if (line.getDiscountAmount() != null) {
                discountAmount = discountAmount.add(line.getDiscountAmount());
            }
        }

        setInvoiceTotals(invoice, subtotal, taxAmount, discountAmount);
    }

    private void setInvoiceTotals(Invoice invoice, BigDecimal subtotal, BigDecimal taxAmount, BigDecimal discountAmount) {
        invoice.setSubtotalAmount(subtotal);
        invoice.setTaxAmount(taxAmount);
        invoice.setDiscountAmount(discountAmount);
        invoice.setTotalAmount(subtotal.add(taxAmount).subtract(discountAmount));
    }
        
}