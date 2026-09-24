package edu.sjsu.cmpe172.picklereserve.service;

import java.time.LocalDate;
import java.util.List;

import edu.sjsu.cmpe172.picklereserve.dto.PageResponse;
import edu.sjsu.cmpe172.picklereserve.dto.SlotResponse;
import edu.sjsu.cmpe172.picklereserve.repository.SlotRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SlotService {

    private static final int MAX_PAGE_SIZE = 100;

    private final SlotRepository slotRepository;

    public SlotService(SlotRepository slotRepository) {
        this.slotRepository = slotRepository;
    }

    @Transactional(readOnly = true)
    public PageResponse<SlotResponse> findAvailableSlots(
            Long providerId,
            Long serviceId,
            LocalDate date,
            int page,
            int size) {
        requirePositive(providerId, "providerId");
        requirePositive(serviceId, "serviceId");
        if (page < 0) {
            throw new IllegalArgumentException("page must be zero or greater");
        }
        if (size < 1 || size > MAX_PAGE_SIZE) {
            throw new IllegalArgumentException("size must be between 1 and " + MAX_PAGE_SIZE);
        }

        int offset;
        try {
            offset = Math.multiplyExact(page, size);
        } catch (ArithmeticException exception) {
            throw new IllegalArgumentException("page and size produce an invalid offset", exception);
        }
        List<SlotResponse> slots = slotRepository.findAvailable(
                providerId, serviceId, date, size, offset);
        long total = slotRepository.countAvailable(providerId, serviceId, date);
        return PageResponse.of(slots, page, size, total);
    }

    private void requirePositive(Long value, String name) {
        if (value != null && value < 1) {
            throw new IllegalArgumentException(name + " must be a positive integer");
        }
    }
}
