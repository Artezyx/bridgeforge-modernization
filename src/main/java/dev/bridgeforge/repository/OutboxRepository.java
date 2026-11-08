package dev.bridgeforge.repository;

import dev.bridgeforge.model.OutboxEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface OutboxRepository extends JpaRepository<OutboxEvent, UUID> {
    List<OutboxEvent> findTop20ByPublishedFalseAndAttemptsLessThanOrderByCreatedAtAsc(int maxAttempts);
}
