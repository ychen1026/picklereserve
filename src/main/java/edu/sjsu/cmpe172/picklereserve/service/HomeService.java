package edu.sjsu.cmpe172.picklereserve.service;

import java.util.List;

import edu.sjsu.cmpe172.picklereserve.dto.HomeResponse;
import edu.sjsu.cmpe172.picklereserve.model.HomeStatistics;
import edu.sjsu.cmpe172.picklereserve.repository.HomeRepository;
import edu.sjsu.cmpe172.picklereserve.repository.SlotRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class HomeService {

    private final HomeRepository homeRepository;
    private final SlotRepository slotRepository;

    public HomeService(HomeRepository homeRepository, SlotRepository slotRepository) {
        this.homeRepository = homeRepository;
        this.slotRepository = slotRepository;
    }

    @Transactional(readOnly = true)
    public HomeResponse getHome() {
        HomeStatistics statistics = homeRepository.loadStatistics();
        return new HomeResponse(
                "PickleReserve",
                "Pickleball court and coaching reservation system",
                List.of("PLAYER", "ADMIN"),
                statistics.providerCount(),
                statistics.serviceCount(),
                statistics.openSlotCount(),
                slotRepository.findAvailable(null, null, null, 3, 0));
    }
}
