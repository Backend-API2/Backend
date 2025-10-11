package backend_api.Backend.events.repository;

import backend_api.Backend.events.entity.EventSubscription;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface EventSubscriptionRepository extends JpaRepository<EventSubscription, Long> {
    List<EventSubscription> findByActiveTrue();
}