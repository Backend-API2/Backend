package backend_api.Backend.Repository;

import backend_api.Backend.Entity.invoice.Invoice;
import backend_api.Backend.Entity.invoice.InvoiceStatus;
import backend_api.Backend.Entity.invoice.InvoiceType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {
    
    Optional<Invoice> findByInvoiceNumber(String invoiceNumber);
    
    List<Invoice> findByPaymentId(Long paymentId);
    
    Page<Invoice> findByUserId(Long userId, Pageable pageable);
    
    Page<Invoice> findByProviderId(Long providerId, Pageable pageable);
    
    Page<Invoice> findByStatus(InvoiceStatus status, Pageable pageable);
    
    Page<Invoice> findByType(InvoiceType type, Pageable pageable);
    
    List<Invoice> findByStatusAndDueDateBefore(InvoiceStatus status, LocalDateTime dueDate);
    
    @Query("SELECT i FROM Invoice i WHERE i.userId = :userId AND i.status = :status")
    Page<Invoice> findByUserIdAndStatus(@Param("userId") Long userId, @Param("status") InvoiceStatus status, Pageable pageable);
    
    @Query("SELECT i FROM Invoice i WHERE i.providerId = :providerId AND i.status = :status")
    Page<Invoice> findByProviderIdAndStatus(@Param("providerId") Long providerId, @Param("status") InvoiceStatus status, Pageable pageable);
    
    @Query("SELECT i FROM Invoice i WHERE i.issueDate BETWEEN :startDate AND :endDate")
    Page<Invoice> findByIssueDateBetween(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate, Pageable pageable);
    
    @Query("SELECT i FROM Invoice i WHERE i.totalAmount BETWEEN :minAmount AND :maxAmount")
    Page<Invoice> findByTotalAmountBetween(@Param("minAmount") BigDecimal minAmount, @Param("maxAmount") BigDecimal maxAmount, Pageable pageable);
    
    @Query("SELECT i FROM Invoice i WHERE " +
           "(:userId IS NULL OR i.userId = :userId) AND " +
           "(:providerId IS NULL OR i.providerId = :providerId) AND " +
           "(:status IS NULL OR i.status = :status) AND " +
           "(:type IS NULL OR i.type = :type) AND " +
           "(:startDate IS NULL OR i.issueDate >= :startDate) AND " +
           "(:endDate IS NULL OR i.issueDate <= :endDate) AND " +
           "(:minAmount IS NULL OR i.totalAmount >= :minAmount) AND " +
           "(:maxAmount IS NULL OR i.totalAmount <= :maxAmount) AND " +
           "(:invoiceNumber IS NULL OR i.invoiceNumber LIKE %:invoiceNumber%)")
    Page<Invoice> findByFilters(
            @Param("userId") Long userId,
            @Param("providerId") Long providerId,
            @Param("status") InvoiceStatus status,
            @Param("type") InvoiceType type,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            @Param("minAmount") BigDecimal minAmount,
            @Param("maxAmount") BigDecimal maxAmount,
            @Param("invoiceNumber") String invoiceNumber,
            Pageable pageable
    );
    
    @Query("SELECT SUM(i.totalAmount) FROM Invoice i WHERE i.providerId = :providerId AND i.status = :status")
    BigDecimal getTotalAmountByProviderAndStatus(@Param("providerId") Long providerId, @Param("status") InvoiceStatus status);
    
    @Query("SELECT COUNT(i) FROM Invoice i WHERE i.userId = :userId AND i.status = :status")
    Long countByUserIdAndStatus(@Param("userId") Long userId, @Param("status") InvoiceStatus status);
    
    @Query("SELECT COUNT(i) FROM Invoice i WHERE i.providerId = :providerId AND i.status = :status")
    Long countByProviderIdAndStatus(@Param("providerId") Long providerId, @Param("status") InvoiceStatus status);
    
    @Query("SELECT i FROM Invoice i WHERE i.status = 'PENDING' AND i.dueDate < CURRENT_TIMESTAMP")
    List<Invoice> findOverdueInvoices();
    
    @Query("SELECT i FROM Invoice i WHERE i.status = 'PENDING' AND i.dueDate BETWEEN CURRENT_TIMESTAMP AND :notificationDate")
    List<Invoice> findInvoicesDueSoon(@Param("notificationDate") LocalDateTime notificationDate);
    
    boolean existsByInvoiceNumber(String invoiceNumber);
}
