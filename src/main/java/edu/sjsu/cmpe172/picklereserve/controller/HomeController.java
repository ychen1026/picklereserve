package edu.sjsu.cmpe172.picklereserve.controller;

import edu.sjsu.cmpe172.picklereserve.dto.HomeResponse;
import edu.sjsu.cmpe172.picklereserve.service.HomeService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HomeController {

    private final HomeService homeService;

    public HomeController(HomeService homeService) {
        this.homeService = homeService;
    }

    @GetMapping("/")
    public HomeResponse home() {
        return homeService.getHome();
    }
}
