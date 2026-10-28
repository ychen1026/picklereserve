package edu.sjsu.cmpe172.picklereserve.model;

import java.time.OffsetDateTime;

public record NotificationDetails(
        long appointmentId,
        String recipient,
        String serviceName,
        String providerName,
        OffsetDateTime startsAt) {
}
