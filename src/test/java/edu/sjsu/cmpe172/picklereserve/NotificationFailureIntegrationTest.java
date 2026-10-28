package edu.sjsu.cmpe172.picklereserve;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.time.Instant;

import edu.sjsu.cmpe172.picklereserve.service.BookingService;
import io.micrometer.core.instrument.MeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;

@SpringBootTest(properties = {
        "notification.gateway.enabled=true",
        "notification.gateway.base-url=http://127.0.0.1:1",
        "notification.gateway.connect-timeout=50ms",
        "notification.gateway.read-timeout=50ms",
        "notification.gateway.max-attempts=2",
        "notification.gateway.retry-backoff=10ms"
})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class NotificationFailureIntegrationTest {
    @Autowired
    BookingService booking;

    @Autowired
    JdbcTemplate jdbc;

    @Autowired
    MeterRegistry meterRegistry;

    @BeforeEach
    void resetBooking() {
        jdbc.update("DELETE FROM appointments");
        jdbc.update("UPDATE availability_slots SET version=0,status='OPEN' WHERE id=1");
    }

    @Test
    void bookingSucceedsWhenNotificationServiceIsDown() throws Exception {
        Instant started = Instant.now();
        long appointmentId = booking.book(1, 1, 1);
        Duration bookingLatency = Duration.between(started, Instant.now());

        Integer count = jdbc.queryForObject(
                "SELECT COUNT(*) FROM appointments WHERE id=? AND status='BOOKED'", Integer.class, appointmentId);
        assertThat(count).isEqualTo(1);
        assertThat(bookingLatency).isLessThan(Duration.ofSeconds(1));

        double fallbackCount = awaitFallback();
        assertThat(fallbackCount).isEqualTo(1.0);
    }

    private double awaitFallback() throws InterruptedException {
        for (int attempt = 0; attempt < 50; attempt++) {
            var counter = meterRegistry.find("picklereserve.notifications.fallback").counter();
            if (counter != null && counter.count() >= 1.0) {
                return counter.count();
            }
            Thread.sleep(20);
        }
        return 0.0;
    }
}
