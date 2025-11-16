package backend_api.Backend.Service.Common;

import backend_api.Backend.DTO.payment.PaymentResponse;
import backend_api.Backend.Entity.payment.Payment;
import backend_api.Backend.Repository.UserRepository;
import backend_api.Backend.Service.Implementation.UserDataIntegrationService;
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
    private final UserDataIntegrationService userDataIntegrationService;

    public List<PaymentResponse> mapPaymentsToResponses(List<Payment> payments, String userRole) {
        Set<Long> allUserIds = payments.stream()
            .flatMap(p -> java.util.stream.Stream.of(p.getUser_id(), p.getProvider_id()))
            .filter(id -> id != null)
            .collect(Collectors.toSet());

        // Usar batch query en vez de queries individuales
        Map<Long, UserDataIntegrationService.UserInfo> userInfoMap = 
            userDataIntegrationService.getUserInfoBatch(allUserIds);

        return payments.stream()
            .map(payment -> PaymentResponse.fromEntityWithRealUserData(payment, userInfoMap, userRole))
            .collect(Collectors.toList());
    }

    public PaymentResponse mapPaymentToResponse(Payment payment, String userRole) {
        // Usar getUserInfoBatch para buscar en user_data, provider_data y User
        Set<Long> userIds = new java.util.HashSet<>();
        if (payment.getUser_id() != null) userIds.add(payment.getUser_id());
        if (payment.getProvider_id() != null) userIds.add(payment.getProvider_id());
        
        Map<Long, UserDataIntegrationService.UserInfo> userInfoMap = 
            userDataIntegrationService.getUserInfoBatch(userIds);
        
        return PaymentResponse.fromEntityWithRealUserData(payment, userInfoMap, userRole);
    }
}