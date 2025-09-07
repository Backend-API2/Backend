package backend_api.Backend.Service.Interface;

public interface CardValidationService {
    boolean isValidCardBin(String cardNumber);
    String extractBin(String cardNumber);
}
