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
    
    @Query("SELECT i FROM Invoice i WHERE i.user_id = :userId")
    List<Invoice> findByUserId(@Param("userId") Long userId);
    
    @Query("SELECT i FROM Invoice i WHERE i.provider_id = :providerId")
    List<Invoice> findByProviderId(@Param("providerId") Long providerId);
    
    @Query("SELECT i FROM Invoice i WHERE i.status = :status")
    List<Invoice> findByStatus(@Param("status") InvoiceStatus status);
    
    @Query("SELECT i FROM Invoice i WHERE i.invoice_number = :invoiceNumber")
    Optional<Invoice> findByInvoiceNumber(@Param("invoiceNumber") String invoiceNumber);
    
    @Query("SELECT i FROM Invoice i WHERE i.payment_id = :paymentId")
    List<Invoice> findByPaymentId(@Param("paymentId") Long paymentId);
    
    @Query("SELECT i FROM Invoice i WHERE i.user_id = :userId AND i.status = :status")
    List<Invoice> findByUserIdAndStatus(@Param("userId") Long userId, @Param("status") InvoiceStatus status);
}
