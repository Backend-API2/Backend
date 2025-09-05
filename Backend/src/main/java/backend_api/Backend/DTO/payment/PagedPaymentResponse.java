package backend_api.Backend.DTO.payment;

import org.springframework.data.domain.Page;

import lombok.Data;

import java.util.List;


@Data
public class PagedPaymentResponse {
    private List<PaymentResponse> content;
    private int totalPages;
    private long totalElements;
    private int size;
    private int number;
    private boolean first;
    private boolean last;
    private int numberOfElements;

    public PagedPaymentResponse() {}

    public PagedPaymentResponse(Page<PaymentResponse> page) {
        this.content = page.getContent();
        this.totalPages = page.getTotalPages();
        this.totalElements = page.getTotalElements();
        this.size = page.getSize();
        this.number = page.getNumber();
        this.first = page.isFirst();
        this.last = page.isLast();
        this.numberOfElements = page.getNumberOfElements();
    }

    public List<PaymentResponse> getContent() {
        return content;
    }

    public void setContent(List<PaymentResponse> content) {
        this.content = content;
    }


}
