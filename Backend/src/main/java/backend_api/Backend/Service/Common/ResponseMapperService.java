package backend_api.Backend.Service.Common;

import backend_api.Backend.DTO.payment.PaymentResponse;
import backend_api.Backend.Entity.payment.Payment;
import backend_api.Backend.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor

public class ResponseMapperService {

    private final UserRepository userRepository;

    public List<PaymentResponse> mapPaymentsToResponses(List<Payment> payments, String userRole) {
        return payments.stream()
            .map(payment -> PaymentResponse.fromEntityWithNames(payment, userRepository, userRole)).collect(Collectors.toList());

    }

    public PaymentResponse mapPaymentToResponse(Payment payment, String userRole) {
        return PaymentResponse.fromEntityWithNames(payment, userRepository, userRole);
    }
}