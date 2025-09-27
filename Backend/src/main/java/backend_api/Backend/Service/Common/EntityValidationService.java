package backend_api.Backend.Service.Common;

import backend_api.Backend.Entity.invoice.Invoice;
import backend_api.Backend.Entity.payment.Payment;
import backend_api.Backend.Entity.user.User;
import backend_api.Backend.Repository.InvoiceRepository;
import backend_api.Backend.Repository.PaymentRepository;
import backend_api.Backend.Repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;




@Service
@RequiredArgsConstructor
public class EntityValidationService {

    private final PaymentRepository paymentRepository;
    private final InvoiceRepository invoiceRepository;
    private final UserRepository userRepository;

    public Payment getPaymentOrThrow(Long paymentId) {
        return paymentRepository.findById(paymentId)
                .orElseThrow(() -> new EntityNotFoundException("Pago no encontrado con ID " + paymentId));
    }

    public Invoice getInvoiceOrThrow(Long invoiceId) {
        return invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new EntityNotFoundException("Factura no encontrada con ID " + invoiceId));
    }

    public User getUserOrThrow(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado con ID " + userId));
    }

    public User getUserByEmailOrThrow(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado con email " + email));
    }

    public Invoice getInvoiceByNumberOrThrow(String invoiceNumber) {
        return invoiceRepository.findByInvoiceNumber(invoiceNumber)
                .orElseThrow(() -> new EntityNotFoundException("Factura no encontrada con número " + invoiceNumber));
    }

    public boolean paymentExists(Long paymentId) {
        return paymentRepository.existsById(paymentId);
    }
    
    public boolean invoiceExists(Long invoiceId) {
        return invoiceRepository.existsById(invoiceId);
    }

    public boolean userExists(Long userId) {
        return userRepository.existsById(userId);
    }

    public void validatePaymentOwnership(Long paymentId, Long userId, String userRole) {
        Payment payment = getPaymentOrThrow(paymentId);
        boolean hasAccess = false;
        if ("MERCHANT".equals(userRole)) {
            hasAccess = payment.getProvider_id().equals(userId);
        } else {
            hasAccess = payment.getUser_id().equals(userId);
        }

        if (!hasAccess) {
            throw new SecurityException("No tiene permiso para acceder a este pago");
        }
    }

    public void validateInvoiceOwnership(Long invoiceId, Long userId) {
        Invoice invoice = getInvoiceOrThrow(invoiceId);
        if (!invoice.getUserId().equals(userId) && !invoice.getProviderId().equals(userId)) {
            throw new SecurityException("No tiene permiso para acceder a esta factura");
        }
    }

}
