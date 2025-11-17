package backend_api.Backend.Service.Implementation;

import backend_api.Backend.Entity.UserData;
import backend_api.Backend.Entity.payment.Payment;
import backend_api.Backend.Entity.user.User;
import backend_api.Backend.Repository.PaymentRepository;
import backend_api.Backend.Repository.UserDataRepository;
import backend_api.Backend.Repository.UserRepository;
import backend_api.Backend.Service.Interface.BalanceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class BalanceServiceImpl implements BalanceService {

    private final UserRepository userRepository;
    private final UserDataRepository userDataRepository;
    private final PaymentRepository paymentRepository;

    @Override
    public boolean hasSufficientBalance(Long userId, BigDecimal amount) {
        // Consultar primero en user_data (donde está el saldo real)
        java.util.Optional<UserData> userDataOpt = userDataRepository.findByUserId(userId);
        
        if (userDataOpt.isPresent()) {
            UserData userData = userDataOpt.get();
            String role = userData.getRole();
            
            if (role != null && role.equalsIgnoreCase("MERCHANT")) {
                return true; // Los merchants no tienen restricción de saldo
            }
            
            BigDecimal saldo = userData.getSaldoDisponible() != null 
                ? userData.getSaldoDisponible() 
                : BigDecimal.ZERO;
            
            log.info("🔍 Verificando saldo desde user_data - UserId: {}, Saldo: {}, Monto requerido: {}", 
                userId, saldo, amount);
            
            return saldo.compareTo(amount) >= 0;
        }
        
        // Fallback a users si no existe en user_data
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        
        if (user.getRole().name().equals("MERCHANT")) {
            return true; // Los merchants no tienen restricción de saldo
        }
        
        BigDecimal saldo = user.getSaldo_disponible() != null 
            ? user.getSaldo_disponible() 
            : BigDecimal.ZERO;
        
        log.info("🔍 Verificando saldo desde users (fallback) - UserId: {}, Saldo: {}, Monto requerido: {}", 
            userId, saldo, amount);
        
        return saldo.compareTo(amount) >= 0;
    }

    @Override
    public User deductBalance(Long userId, BigDecimal amount) {
        // Consultar primero en user_data (donde está el saldo real)
        java.util.Optional<UserData> userDataOpt = userDataRepository.findByUserId(userId);
        
        if (userDataOpt.isPresent()) {
            UserData userData = userDataOpt.get();
            String role = userData.getRole();
            
            if (role != null && role.equalsIgnoreCase("MERCHANT")) {
                // Retornar un User dummy para mantener compatibilidad con la interfaz
                User dummyUser = new User();
                dummyUser.setId(userId);
                return dummyUser;
            }
            
            if (!hasSufficientBalance(userId, amount)) {
                throw new IllegalStateException("Saldo insuficiente");
            }
            
            BigDecimal saldoActual = userData.getSaldoDisponible() != null 
                ? userData.getSaldoDisponible() 
                : BigDecimal.ZERO;
            
            BigDecimal nuevoSaldo = saldoActual.subtract(amount);
            userData.setSaldoDisponible(nuevoSaldo);
            userDataRepository.save(userData);
            
            log.info("✅ Balance descontado desde user_data - UserId: {}, Saldo anterior: {}, Monto: {}, Saldo nuevo: {}", 
                userId, saldoActual, amount, nuevoSaldo);
            
            // Retornar un User dummy para mantener compatibilidad con la interfaz
            User dummyUser = new User();
            dummyUser.setId(userId);
            return dummyUser;
        }
        
        // Fallback a users si no existe en user_data
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        
        if (user.getRole().name().equals("MERCHANT")) {
            return user; // Los merchants no tienen saldo
        }
        
        if (!hasSufficientBalance(userId, amount)) {
            throw new IllegalStateException("Saldo insuficiente");
        }
        
        BigDecimal saldoActual = user.getSaldo_disponible() != null 
            ? user.getSaldo_disponible() 
            : BigDecimal.ZERO;
        
        user.setSaldo_disponible(saldoActual.subtract(amount));
        log.info("✅ Balance descontado desde users (fallback) - UserId: {}, Saldo anterior: {}, Monto: {}, Saldo nuevo: {}", 
            userId, saldoActual, amount, saldoActual.subtract(amount));
        
        return userRepository.save(user);
    }

    @Override
    public User addBalance(Long userId, BigDecimal amount) {
        // Consultar primero en user_data (donde está el saldo real)
        java.util.Optional<UserData> userDataOpt = userDataRepository.findByUserId(userId);
        
        if (userDataOpt.isPresent()) {
            UserData userData = userDataOpt.get();
            String role = userData.getRole();
            
            if (role != null && role.equalsIgnoreCase("MERCHANT")) {
                // Retornar un User dummy para mantener compatibilidad con la interfaz
                User dummyUser = new User();
                dummyUser.setId(userId);
                return dummyUser;
            }
            
            BigDecimal saldoActual = userData.getSaldoDisponible() != null 
                ? userData.getSaldoDisponible() 
                : BigDecimal.ZERO;
            
            BigDecimal nuevoSaldo = saldoActual.add(amount);
            userData.setSaldoDisponible(nuevoSaldo);
            userDataRepository.save(userData);
            
            log.info("✅ Balance agregado desde user_data - UserId: {}, Saldo anterior: {}, Monto: {}, Saldo nuevo: {}", 
                userId, saldoActual, amount, nuevoSaldo);
            
            // Retornar un User dummy para mantener compatibilidad con la interfaz
            User dummyUser = new User();
            dummyUser.setId(userId);
            return dummyUser;
        }
        
        // Fallback a users si no existe en user_data
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        
        if (user.getRole().name().equals("MERCHANT")) {
            return user; // Los merchants no tienen saldo
        }
        
        BigDecimal saldoActual = user.getSaldo_disponible() != null 
            ? user.getSaldo_disponible() 
            : BigDecimal.ZERO;
        
        user.setSaldo_disponible(saldoActual.add(amount));
        log.info("✅ Balance agregado desde users (fallback) - UserId: {}, Saldo anterior: {}, Monto: {}, Saldo nuevo: {}", 
            userId, saldoActual, amount, saldoActual.add(amount));
        
        return userRepository.save(user);
    }

    @Override
    public BigDecimal getCurrentBalance(Long userId) {
        // Consultar primero en user_data (donde está el saldo real)
        java.util.Optional<UserData> userDataOpt = userDataRepository.findByUserId(userId);
        
        if (userDataOpt.isPresent()) {
            UserData userData = userDataOpt.get();
            BigDecimal saldo = userData.getSaldoDisponible() != null 
                ? userData.getSaldoDisponible() 
                : BigDecimal.ZERO;
            
            log.info("💰 Saldo obtenido desde user_data - UserId: {}, Saldo: {}", userId, saldo);
            return saldo;
        }
        
        // Fallback a users si no existe en user_data
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        
        BigDecimal saldo = user.getSaldo_disponible() != null 
            ? user.getSaldo_disponible() 
            : BigDecimal.ZERO;
        
        log.info("💰 Saldo obtenido desde users (fallback) - UserId: {}, Saldo: {}", userId, saldo);
        return saldo;
    }

    @Override
    public boolean canRetryPayment(Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Pago no encontrado"));
        
        // Puede reintentar si:
        // 1. Fue rechazado por saldo insuficiente
        // 2. No ha excedido el máximo de intentos (3)
        // 3. El usuario actual tiene saldo suficiente (se verifica en el controller)
        boolean rejectedByBalance = payment.getRejected_by_balance() != null && payment.getRejected_by_balance();
        boolean hasAttemptsLeft = payment.getRetry_attempts() < 3;
        boolean canRetry = rejectedByBalance && hasAttemptsLeft;
        
        log.info("🔍 canRetryPayment - PaymentId: {}, RejectedByBalance: {}, RetryAttempts: {}, HasAttemptsLeft: {}, CanRetry: {}", 
            paymentId, 
            payment.getRejected_by_balance(),
            payment.getRetry_attempts(),
            hasAttemptsLeft,
            canRetry);
        
        if (!canRetry) {
            if (!rejectedByBalance) {
                log.warn("⚠️ Pago no puede ser reintentado - PaymentId: {} - Razón: No fue rechazado por saldo insuficiente (rejected_by_balance: {})", 
                    paymentId, payment.getRejected_by_balance());
            }
            if (!hasAttemptsLeft) {
                log.warn("⚠️ Pago no puede ser reintentado - PaymentId: {} - Razón: Máximo de intentos alcanzado (retry_attempts: {})", 
                    paymentId, payment.getRetry_attempts());
            }
        }
        
        return canRetry;
    }
}
