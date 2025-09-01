package backend_api.Backend.DTO.payment;

import backend_api.Backend.Entity.payment.PaymentStatus;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public class PaymentStatsRequest {
    
    @NotNull(message = "User ID is required")
    private Long userId;
    
    private List<PaymentStatus> statuses;
    private String currency;
    private String startDate;  // YYYY-MM-DD
    private String endDate;    // YYYY-MM-DD
    
    private boolean groupByStatus = true;
    private boolean groupByCurrency = false;
    private boolean groupByMonth = false;
    
    public PaymentStatsRequest() {}
    
    public PaymentStatsRequest(Long userId) {
        this.userId = userId;
    }
    
    public Long getUserId() {
        return userId;
    }
    
    public void setUserId(Long userId) {
        this.userId = userId;
    }
    
    public List<PaymentStatus> getStatuses() {
        return statuses;
    }
    
    public void setStatuses(List<PaymentStatus> statuses) {
        this.statuses = statuses;
    }
    
    public String getCurrency() {
        return currency;
    }
    
    public void setCurrency(String currency) {
        this.currency = currency;
    }
    
    public String getStartDate() {
        return startDate;
    }
    
    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }
    
    public String getEndDate() {
        return endDate;
    }
    
    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }
    
    public boolean isGroupByStatus() {
        return groupByStatus;
    }
    
    public void setGroupByStatus(boolean groupByStatus) {
        this.groupByStatus = groupByStatus;
    }
    
    public boolean isGroupByCurrency() {
        return groupByCurrency;
    }
    
    public void setGroupByCurrency(boolean groupByCurrency) {
        this.groupByCurrency = groupByCurrency;
    }
    
    public boolean isGroupByMonth() {
        return groupByMonth;
    }
    
    public void setGroupByMonth(boolean groupByMonth) {
        this.groupByMonth = groupByMonth;
    }
}
