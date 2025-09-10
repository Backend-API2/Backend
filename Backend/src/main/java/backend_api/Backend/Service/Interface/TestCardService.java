package backend_api.Backend.Service.Interface;

public interface TestCardService {
    boolean isAllowedTestCard(String pan, String cvv);
}