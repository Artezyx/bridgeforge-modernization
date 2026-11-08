package dev.bridgeforge.service;

import dev.bridgeforge.repository.OutboxRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Service
public class OutboxDispatcher {
    private static final Logger log = LoggerFactory.getLogger(OutboxDispatcher.class);
    private final OutboxRepository repository;
    private final RestClient client;
    private final String webhookUrl;

    public OutboxDispatcher(OutboxRepository repository, RestClient.Builder builder,
                            @Value("${outbox.webhook-url:}") String webhookUrl) {
        this.repository = repository;
        this.client = builder.build();
        this.webhookUrl = webhookUrl;
    }

    @Scheduled(fixedDelayString = "${outbox.poll-ms:5000}")
    @Transactional
    public void publishPending() {
        if (webhookUrl.isBlank()) return;
        for (var event : repository.findTop20ByPublishedFalseAndAttemptsLessThanOrderByCreatedAtAsc(5)) {
            try {
                client.post().uri(webhookUrl)
                    .header("X-Event-Id", event.getId().toString())
                    .header("X-Event-Type", event.getEventType())
                    .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                    .body(event.getPayload()).retrieve().toBodilessEntity();
                event.markPublished();
            } catch (RestClientException ex) {
                event.recordFailure();
                log.warn("Event {} not delivered; attempt {}", event.getId(), event.getAttempts());
            }
        }
    }
}
