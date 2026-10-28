package edu.sjsu.cmpe172.picklereserve.controller;

import edu.sjsu.cmpe172.picklereserve.dto.NotificationRequest;
import edu.sjsu.cmpe172.picklereserve.dto.NotificationResponse;
import edu.sjsu.cmpe172.picklereserve.integration.NotificationStubStore;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/mock-notifications")
public class MockNotificationController {
    private static final Logger log = LoggerFactory.getLogger(MockNotificationController.class);
    private final NotificationStubStore store;

    public MockNotificationController(NotificationStubStore store) {
        this.store = store;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.ACCEPTED)
    public NotificationResponse accept(
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            @Valid @RequestBody NotificationRequest request) {
        boolean duplicate = store.accept(idempotencyKey, request);
        log.info("Mock notification accepted key={} type={} recipient={} duplicate={}",
                idempotencyKey, request.type(), request.recipient(), duplicate);
        return new NotificationResponse("ACCEPTED", duplicate, idempotencyKey);
    }
}
