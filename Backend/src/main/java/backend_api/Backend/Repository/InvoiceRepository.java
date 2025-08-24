package backend_api.Backend.Repository;

import backend_api.Backend.Entity.invoice.Invoice;
import backend_api.Backend.Entity.invoice.InvoiceStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {
    
    List<Invoice> findByUserId(Long userId);
    
    List<Invoice> findByProviderId(Long providerId);
    
    List<Invoice> findByStatus(InvoiceStatus status);
    
    Optional<Invoice> findByInvoiceNumber(String invoiceNumber);
    
    List<Invoice> findByPaymentId(Long paymentId);
    
    @Query("SELECT i FROM Invoice i WHERE i.user_id = :userId AND i.status = :status")
    List<Invoice> findByUserIdAndStatus(@Param("userId") Long userId, @Param("status") InvoiceStatus status);
}
