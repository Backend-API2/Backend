package backend_api.Backend.Repository;

import backend_api.Backend.Entity.invoice.InvoiceEvent;
import backend_api.Backend.Entity.invoice.InvoiceEventType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface InvoiceEventRepository extends JpaRepository<InvoiceEvent, Long> {
    
    List<InvoiceEvent> findByInvoiceIdOrderByCreatedAtDesc(Long invoiceId);
    
    Page<InvoiceEvent> findByInvoiceId(Long invoiceId, Pageable pageable);
    
    List<InvoiceEvent> findByEventType(InvoiceEventType eventType);
    
    List<InvoiceEvent> findByCreatedBy(Long createdBy);
    
    @Query("SELECT ie FROM InvoiceEvent ie WHERE ie.invoiceId = :invoiceId AND ie.eventType = :eventType")
    List<InvoiceEvent> findByInvoiceIdAndEventType(@Param("invoiceId") Long invoiceId, @Param("eventType") InvoiceEventType eventType);
    
    @Query("SELECT ie FROM InvoiceEvent ie WHERE ie.createdAt BETWEEN :startDate AND :endDate")
    List<InvoiceEvent> findByCreatedAtBetween(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT COUNT(ie) FROM InvoiceEvent ie WHERE ie.invoiceId = :invoiceId")
    Long countByInvoiceId(@Param("invoiceId") Long invoiceId);
    
    @Query("SELECT COUNT(ie) FROM InvoiceEvent ie WHERE ie.invoiceId = :invoiceId AND ie.eventType = :eventType")
    Long countByInvoiceIdAndEventType(@Param("invoiceId") Long invoiceId, @Param("eventType") InvoiceEventType eventType);
}
