package edu.sjsu.cmpe172.picklereserve.dto;

import java.time.OffsetDateTime;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CreateSlotRequest(
        @Positive long serviceId,
        @NotNull OffsetDateTime startsAt,
        @NotNull OffsetDateTime endsAt) {}
