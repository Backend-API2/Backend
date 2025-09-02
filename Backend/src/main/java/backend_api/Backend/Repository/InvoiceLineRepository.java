package backend_api.Backend.Repository;

import backend_api.Backend.Entity.invoice.InvoiceLine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface InvoiceLineRepository extends JpaRepository<InvoiceLine, Long> {
    
    List<InvoiceLine> findByInvoiceIdOrderByLineNumber(Long invoiceId);
    
    List<InvoiceLine> findByInvoiceId(Long invoiceId);
    
    List<InvoiceLine> findByProductId(Long productId);
    
    @Query("SELECT SUM(il.totalAmount) FROM InvoiceLine il WHERE il.invoiceId = :invoiceId")
    BigDecimal getTotalAmountByInvoiceId(@Param("invoiceId") Long invoiceId);
    
    @Query("SELECT COUNT(il) FROM InvoiceLine il WHERE il.invoiceId = :invoiceId")
    Long countByInvoiceId(@Param("invoiceId") Long invoiceId);
    
    @Query("SELECT il FROM InvoiceLine il WHERE il.invoiceId = :invoiceId AND il.productId = :productId")
    List<InvoiceLine> findByInvoiceIdAndProductId(@Param("invoiceId") Long invoiceId, @Param("productId") Long productId);
    
    void deleteByInvoiceId(Long invoiceId);
}