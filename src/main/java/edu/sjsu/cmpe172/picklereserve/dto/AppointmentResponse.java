package edu.sjsu.cmpe172.picklereserve.dto;

import java.time.OffsetDateTime;

public record AppointmentResponse(
        long appointmentId,
        long slotId,
        long userId,
        String customerName,
        long providerId,
        String providerName,
        long serviceId,
        String serviceName,
        OffsetDateTime startsAt,
        OffsetDateTime endsAt,
        String status) {}
