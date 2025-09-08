package backend_api.Backend.Service.Implementation;

import backend_api.Backend.Repository.TestCardRepository;
import backend_api.Backend.Service.Interface.TestCardService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;

@Service
public class TestCardServiceImpl implements TestCardService {

    private final TestCardRepository repo;

    @Value("${app.test-cards.enabled:true}") // por defecto ON en dev
    private boolean enabled;

    public TestCardServiceImpl(TestCardRepository repo) {
        this.repo = repo;
    }

    @Override
    public boolean isAllowedTestCard(String pan, String cvv) {
        if (!enabled) return true; // si está deshabilitado, no filtra por whitelist
        if (pan == null || cvv == null) return false;
        String panHash = sha256(pan.replaceAll("[\\s-]", ""));
        String cvvHash = sha256(cvv.trim());
        return repo.existsByPanSha256AndCvvSha256AndIsActiveTrue(panHash, cvvHash);
    }

    private String sha256(String s) {
        try {
            var md = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(md.digest(s.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}