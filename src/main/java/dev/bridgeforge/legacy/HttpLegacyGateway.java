package dev.bridgeforge.legacy;

import dev.bridgeforge.service.UpstreamUnavailable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import java.util.Optional;

@Component
public class HttpLegacyGateway implements LegacyGateway {
    private final RestClient client;
    private final String url;
    private final boolean enabled;

    public HttpLegacyGateway(RestClient.Builder builder,
            @Value("${legacy.url:}") String url,
            @Value("${legacy.enabled:false}") boolean enabled) {
        this.client = builder.build();
        this.url = url;
        this.enabled = enabled;
    }

    @Override
    public Optional<LegacyCustomer> fetch(Long id) {
        if (!enabled || url.isBlank()) return Optional.empty();
        try {
            return Optional.ofNullable(client.get().uri(url + "/customers/{id}", id)
                .retrieve().body(LegacyCustomer.class));
        } catch (HttpClientErrorException.NotFound ex) {
            return Optional.empty();
        } catch (RestClientException ex) {
            throw new UpstreamUnavailable("Legacy customer service is unavailable.");
        }
    }
}
