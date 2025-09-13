package backend_api.Backend.Service.Interface;

import backend_api.Backend.Entity.user.User;
import java.math.BigDecimal;

public interface BalanceService {
    
  
    boolean hasSufficientBalance(Long userId, BigDecimal amount);
    
   
    User deductBalance(Long userId, BigDecimal amount);
    
   
    User addBalance(Long userId, BigDecimal amount);
    
   
    BigDecimal getCurrentBalance(Long userId);
    
   
    boolean canRetryPayment(Long paymentId);
}
