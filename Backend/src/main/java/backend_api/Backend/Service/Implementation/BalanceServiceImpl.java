package backend_api.Backend.Service.Implementation;

import backend_api.Backend.Entity.payment.Payment;
import backend_api.Backend.Entity.user.User;
import backend_api.Backend.Repository.PaymentRepository;
import backend_api.Backend.Repository.UserRepository;
import backend_api.Backend.Service.Interface.BalanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Transactional
public class BalanceServiceImpl implements BalanceService {

    private final UserRepository userRepository;
    private final PaymentRepository paymentRepository;

    @Override
    public boolean hasSufficientBalance(Long userId, BigDecimal amount) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        
        if (user.getRole().name().equals("MERCHANT")) {
            return true; // Los merchants no tienen restricción de saldo
        }
        
        return user.getSaldo_disponible().compareTo(amount) >= 0;
    }

    @Override
    public User deductBalance(Long userId, BigDecimal amount) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        
        if (user.getRole().name().equals("MERCHANT")) {
            return user; // Los merchants no tienen saldo
        }
        
        if (!hasSufficientBalance(userId, amount)) {
            throw new IllegalStateException("Saldo insuficiente");
        }
        
        user.setSaldo_disponible(user.getSaldo_disponible().subtract(amount));
        return userRepository.save(user);
    }

    @Override
    public User addBalance(Long userId, BigDecimal amount) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        
        if (user.getRole().name().equals("MERCHANT")) {
            return user; // Los merchants no tienen saldo
        }
        
        user.setSaldo_disponible(user.getSaldo_disponible().add(amount));
        return userRepository.save(user);
    }

    @Override
    public BigDecimal getCurrentBalance(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        
        return user.getSaldo_disponible();
    }

    @Override
    public boolean canRetryPayment(Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Pago no encontrado"));
        
        // Puede reintentar si:
        // 1. Fue rechazado por saldo insuficiente
        // 2. No ha excedido el máximo de intentos (3)
        // 3. El usuario actual tiene saldo suficiente (se verifica en el controller)
        return payment.getRejected_by_balance() != null && 
               payment.getRejected_by_balance() && 
               payment.getRetry_attempts() < 3;
    }
}
