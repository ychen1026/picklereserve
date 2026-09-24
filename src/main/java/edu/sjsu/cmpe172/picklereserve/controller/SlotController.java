package edu.sjsu.cmpe172.picklereserve.controller;

import java.time.LocalDate;

import edu.sjsu.cmpe172.picklereserve.dto.PageResponse;
import edu.sjsu.cmpe172.picklereserve.dto.SlotResponse;
import edu.sjsu.cmpe172.picklereserve.service.SlotService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SlotController {

    private final SlotService slotService;

    public SlotController(SlotService slotService) {
        this.slotService = slotService;
    }

    @GetMapping("/slots")
    public PageResponse<SlotResponse> availableSlots(
            @RequestParam(required = false) Long providerId,
            @RequestParam(required = false) Long serviceId,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return slotService.findAvailableSlots(providerId, serviceId, date, page, size);
    }
}
