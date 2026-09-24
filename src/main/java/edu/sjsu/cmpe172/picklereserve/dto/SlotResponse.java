package edu.sjsu.cmpe172.picklereserve.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record SlotResponse(
        long slotId,
        long providerId,
        String providerName,
        long serviceId,
        String serviceName,
        int durationMinutes,
        BigDecimal priceDollars,
        OffsetDateTime startsAt,
        OffsetDateTime endsAt,
        String status) {
}
