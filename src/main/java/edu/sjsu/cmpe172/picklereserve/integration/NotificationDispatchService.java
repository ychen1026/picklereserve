package edu.sjsu.cmpe172.picklereserve.integration;

import java.time.format.DateTimeFormatter;

import edu.sjsu.cmpe172.picklereserve.dto.NotificationRequest;
import edu.sjsu.cmpe172.picklereserve.model.NotificationDetails;
import edu.sjsu.cmpe172.picklereserve.repository.NotificationRepository;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Service
public class NotificationDispatchService {
    private static final Logger log = LoggerFactory.getLogger(NotificationDispatchService.class);
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("MMM d, uuuu h:mm a xxx");
    private final NotificationRepository repository;
    private final NotificationGateway gateway;
    private final Counter bookingsCommitted;
    private final Counter cancellationsCommitted;
    private final Counter contentMissing;

    public NotificationDispatchService(
            NotificationRepository repository,
            NotificationGateway gateway,
            MeterRegistry meterRegistry) {
        this.repository = repository;
        this.gateway = gateway;
        this.bookingsCommitted = Counter.builder("picklereserve.bookings.committed")
                .description("Successfully committed appointment bookings")
                .register(meterRegistry);
        this.cancellationsCommitted = Counter.builder("picklereserve.cancellations.committed")
                .description("Successfully committed appointment cancellations")
                .register(meterRegistry);
        this.contentMissing = Counter.builder("picklereserve.notifications.content.missing")
                .description("Committed appointments missing notification content")
                .register(meterRegistry);
    }

    @Async("notificationExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void dispatch(AppointmentNotificationEvent event) {
        if (event.type() == NotificationType.BOOKING_CONFIRMED) {
            bookingsCommitted.increment();
        } else {
            cancellationsCommitted.increment();
        }

        NotificationDetails details = repository.findDetails(event.appointmentId()).orElse(null);
        if (details == null) {
            contentMissing.increment();
            log.error("Notification content missing after commit appointmentId={} type={}",
                    event.appointmentId(), event.type());
            return;
        }

        String idempotencyKey = "appointment-" + event.appointmentId() + "-" + event.type().name().toLowerCase();
        NotificationRequest request = new NotificationRequest(
                details.recipient(),
                event.type().name(),
                message(event.type(), details));
        log.info("Committed appointment event queued for notification appointmentId={} type={}",
                event.appointmentId(), event.type());
        gateway.send(idempotencyKey, request);
    }

    private String message(NotificationType type, NotificationDetails details) {
        String time = details.startsAt().format(TIME_FORMAT);
        if (type == NotificationType.BOOKING_CONFIRMED) {
            return "Confirmed: " + details.serviceName() + " with " + details.providerName() + " on " + time + ".";
        }
        return "Cancelled: " + details.serviceName() + " with " + details.providerName() + " on " + time + ".";
    }
}
