package backend_api.Backend.Service.Common;

import backend_api.Backend.DTO.payment.PaymentResponse;
import backend_api.Backend.Entity.payment.Payment;
import backend_api.Backend.Entity.user.User;
import backend_api.Backend.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor

public class ResponseMapperService {

    private final UserRepository userRepository;

    public List<PaymentResponse> mapPaymentsToResponses(List<Payment> payments, String userRole) {
        // Optimización: Obtener todos los IDs únicos de usuarios y providers
        Set<Long> allUserIds = payments.stream()
            .flatMap(p -> java.util.stream.Stream.of(p.getUser_id(), p.getProvider_id()))
            .filter(id -> id != null)
            .collect(Collectors.toSet());

        // Hacer UNA SOLA query para traer todos los usuarios necesarios
        Map<Long, User> userMap = userRepository.findAllById(allUserIds).stream()
            .collect(Collectors.toMap(User::getId, user -> user));

        // Ahora mapear sin hacer queries adicionales
        return payments.stream()
            .map(payment -> PaymentResponse.fromEntityWithNamesOptimized(payment, userMap, userRole))
            .collect(Collectors.toList());
    }

    public PaymentResponse mapPaymentToResponse(Payment payment, String userRole) {
        return PaymentResponse.fromEntityWithNames(payment, userRepository, userRole);
    }
}