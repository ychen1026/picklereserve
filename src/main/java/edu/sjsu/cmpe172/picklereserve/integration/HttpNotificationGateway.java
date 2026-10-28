package edu.sjsu.cmpe172.picklereserve.integration;

import edu.sjsu.cmpe172.picklereserve.config.NotificationGatewayProperties;
import edu.sjsu.cmpe172.picklereserve.dto.NotificationRequest;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
public class HttpNotificationGateway implements NotificationGateway {
    private static final Logger log = LoggerFactory.getLogger(HttpNotificationGateway.class);
    private final RestClient restClient;
    private final NotificationGatewayProperties properties;
    private final NotificationFallbackHandler fallbackHandler;
    private final Counter retryCounter;
    private final Counter deliveredCounter;

    public HttpNotificationGateway(
            RestClient notificationRestClient,
            NotificationGatewayProperties properties,
            NotificationFallbackHandler fallbackHandler,
            MeterRegistry meterRegistry) {
        this.restClient = notificationRestClient;
        this.properties = properties;
        this.fallbackHandler = fallbackHandler;
        this.retryCounter = Counter.builder("picklereserve.notifications.retry")
                .description("Remote notification retry attempts")
                .register(meterRegistry);
        this.deliveredCounter = Counter.builder("picklereserve.notifications.delivered")
                .description("Notifications accepted by the remote service")
                .register(meterRegistry);
    }

    @Override
    public void send(String idempotencyKey, NotificationRequest request) {
        if (!properties.isEnabled()) {
            log.info("Notification gateway disabled key={} type={}", idempotencyKey, request.type());
            return;
        }

        RuntimeException lastFailure = null;
        int attempts = Math.max(1, properties.getMaxAttempts());
        for (int attempt = 1; attempt <= attempts; attempt++) {
            try {
                restClient.post()
                        .uri("/mock-notifications")
                        .header("Idempotency-Key", idempotencyKey)
                        .body(request)
                        .retrieve()
                        .toBodilessEntity();
                deliveredCounter.increment();
                log.info("Notification delivered key={} type={} attempt={}",
                        idempotencyKey, request.type(), attempt);
                return;
            } catch (RestClientException exception) {
                lastFailure = exception;
                if (attempt < attempts) {
                    retryCounter.increment();
                    log.warn("Notification attempt failed; retrying key={} attempt={} maxAttempts={} reason={}",
                            idempotencyKey, attempt, attempts, exception.getMessage());
                    pauseBeforeRetry(attempt);
                }
            }
        }
        fallbackHandler.handle(idempotencyKey, request, lastFailure);
    }

    private void pauseBeforeRetry(int attempt) {
        try {
            Thread.sleep(properties.getRetryBackoff().toMillis() * attempt);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new RestClientException("Notification retry interrupted", exception);
        }
    }
}
