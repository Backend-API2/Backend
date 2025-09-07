package backend_api.Backend.Service.Implementation;

import backend_api.Backend.Service.Interface.CardValidationService;
import backend_api.Backend.Repository.CardBinRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CardValidationServiceImpl implements CardValidationService {
    
    @Autowired
    private CardBinRepository cardBinRepository;
    
    @Override
    public boolean isValidCardBin(String cardNumber) {
        if (cardNumber == null || cardNumber.length() < 3) {
            return false;
        }
        
        String bin = extractBin(cardNumber);
        return cardBinRepository.existsByBinAndIsActiveTrue(bin);
    }
    
    @Override
    public String extractBin(String cardNumber) {
        if (cardNumber == null || cardNumber.length() < 3) {
            throw new IllegalArgumentException("Card number must have at least 3 digits");
        }
        String cleanCardNumber = cardNumber.replaceAll("[\\s-]", "");
        
        return cleanCardNumber.substring(0, 3);
    }
}
