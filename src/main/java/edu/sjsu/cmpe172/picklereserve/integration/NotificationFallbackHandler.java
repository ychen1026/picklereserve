package edu.sjsu.cmpe172.picklereserve.integration;

import edu.sjsu.cmpe172.picklereserve.dto.NotificationRequest;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class NotificationFallbackHandler {
    private static final Logger log = LoggerFactory.getLogger(NotificationFallbackHandler.class);
    private final Counter fallbackCounter;

    public NotificationFallbackHandler(MeterRegistry meterRegistry) {
        this.fallbackCounter = Counter.builder("picklereserve.notifications.fallback")
                .description("Notifications abandoned after all remote attempts failed")
                .register(meterRegistry);
    }

    public void handle(String idempotencyKey, NotificationRequest request, RuntimeException exception) {
        fallbackCounter.increment();
        log.error("Notification fallback activated key={} type={} recipient={} reason={}",
                idempotencyKey, request.type(), request.recipient(), exception.getMessage());
    }
}
