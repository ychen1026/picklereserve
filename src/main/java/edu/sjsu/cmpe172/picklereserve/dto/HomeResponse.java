package edu.sjsu.cmpe172.picklereserve.dto;

import java.util.List;

public record HomeResponse(
        String application,
        String scenario,
        List<String> roles,
        long providerCount,
        long serviceCount,
        long openSlotCount,
        List<SlotResponse> nextAvailableSlots) {
}
