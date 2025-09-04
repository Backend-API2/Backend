package backend_api.Backend.DTO.payment;

import backend_api.Backend.Entity.payment.PaymentStatus;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class PaymentSearchRequest {
    
    private PaymentStatus status;
    private String currency;
    private BigDecimal minAmount;
    private BigDecimal maxAmount;
    private LocalDate startDate;
    private LocalDate endDate;
    
    private Long userId;
    private String userName;
    private List<Long> userIds;
    
    private Long providerId;
    private List<Long> providerIds;
    
    private Long solicitudId;
  //  private Long cotizacionId; // Se integra con el módulo Cotizacion
    
    private String metadataKey;
    private String metadataValue;
    
    // Paginación y ordenamiento
    @Min(value = 0, message = "Page number must be non-negative")
    private int page = 0;
    
    @Min(value = 1, message = "Page size must be positive")
    private int size = 10;
    
    @Pattern(regexp = "^(id|user_id|provider_id|amount_total|created_at|updated_at)$", 
             message = "Invalid sort field")
    private String sortBy = "created_at";
    
    @Pattern(regexp = "^(asc|desc)$", message = "Sort direction must be 'asc' or 'desc'")
    private String sortDir = "desc";
    
    public PaymentSearchRequest() {}
    
    public PaymentStatus getStatus() {
        return status;
    }
    
    public void setStatus(PaymentStatus status) {
        this.status = status;
    }
    
    public String getCurrency() {
        return currency;
    }
    
    public void setCurrency(String currency) {
        this.currency = currency;
    }
    
    public BigDecimal getMinAmount() {
        return minAmount;
    }
    
    public void setMinAmount(BigDecimal minAmount) {
        this.minAmount = minAmount;
    }
    
    public BigDecimal getMaxAmount() {
        return maxAmount;
    }
    
    public void setMaxAmount(BigDecimal maxAmount) {
        this.maxAmount = maxAmount;
    }
    
    public LocalDate getStartDate() {
        return startDate;
    }
    
    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }
    
    public LocalDate getEndDate() {
        return endDate;
    }
    
    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }
    
    public Long getUserId() {
        return userId;
    }
    
    public void setUserId(Long userId) {
        this.userId = userId;
    }
    
    public String getUserName() {
        return userName;
    }
    
    public void setUserName(String userName) {
        this.userName = userName;
    }
    
    public List<Long> getUserIds() {
        return userIds;
    }
    
    public void setUserIds(List<Long> userIds) {
        this.userIds = userIds;
    }
    
    public Long getProviderId() {
        return providerId;
    }
    
    public void setProviderId(Long providerId) {
        this.providerId = providerId;
    }
    
    public List<Long> getProviderIds() {
        return providerIds;
    }
    
    public void setProviderIds(List<Long> providerIds) {
        this.providerIds = providerIds;
    }
    
    
    public Long getSolicitudId() {
        return solicitudId;
    }
    
    public void setSolicitudId(Long solicitudId) {
        this.solicitudId = solicitudId;
    }
    
   
    
    public String getMetadataKey() {
        return metadataKey;
    }
    
    public void setMetadataKey(String metadataKey) {
        this.metadataKey = metadataKey;
    }
    
    public String getMetadataValue() {
        return metadataValue;
    }
    
    public void setMetadataValue(String metadataValue) {
        this.metadataValue = metadataValue;
    }
    
    public int getPage() {
        return page;
    }
    
    public void setPage(int page) {
        this.page = page;
    }
    
    public int getSize() {
        return size;
    }
    
    public void setSize(int size) {
        this.size = size;
    }
    
    public String getSortBy() {
        return sortBy;
    }
    
    public void setSortBy(String sortBy) {
        this.sortBy = sortBy;
    }
    
    public String getSortDir() {
        return sortDir;
    }
    
    public void setSortDir(String sortDir) {
        this.sortDir = sortDir;
    }
}
