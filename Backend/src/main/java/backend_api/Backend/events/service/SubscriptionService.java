package backend_api.Backend.events.service;

import backend_api.Backend.events.entity.EventSubscription;
import backend_api.Backend.events.entity.EventType;
import backend_api.Backend.events.repository.EventSubscriptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.HexFormat;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SubscriptionService {

    private final EventSubscriptionRepository repository;
    private final SecureRandom random = new SecureRandom();

    public List<EventSubscription> listActive() { return repository.findByActiveTrue(); }
    public List<EventSubscription> listAll()    { return repository.findAll(); }

    @Transactional
    public EventSubscription create(String name, String targetUrl, List<EventType> types) {
        EventSubscription s = new EventSubscription();
        s.setName(name);
        s.setTargetUrl(targetUrl);
        s.setEventTypes(String.join(",", types.stream().map(Enum::name).toList()));
        s.setSecret(generateSecret());
        return repository.save(s);
    }

    @Transactional
    public EventSubscription update(Long id, EventSubscription patch) {
        EventSubscription s = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Subscription not found: " + id));
        if (patch.getName() != null) s.setName(patch.getName());
        if (patch.getTargetUrl() != null) s.setTargetUrl(patch.getTargetUrl());
        if (patch.getEventTypes() != null) s.setEventTypes(patch.getEventTypes());
        if (patch.getMaxRetries() > 0) s.setMaxRetries(patch.getMaxRetries());
        if (patch.getBackoffMs() > 0) s.setBackoffMs(patch.getBackoffMs());
        if (patch.getRequestTimeoutMs() > 0) s.setRequestTimeoutMs(patch.getRequestTimeoutMs());
        s.setActive(patch.isActive());
        return repository.save(s);
    }

    @Transactional public void delete(Long id) { repository.deleteById(id); }

    @Transactional
    public String rotateSecret(Long id) {
        EventSubscription s = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Subscription not found: " + id));
        String sec = generateSecret();
        s.setSecret(sec);
        repository.save(s);
        return sec;
    }

    private String generateSecret() {
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        return HexFormat.of().formatHex(bytes);
    }
}