package edu.sjsu.cmpe172.picklereserve;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class PickleReserveApplication {

    public static void main(String[] args) {
        SpringApplication.run(PickleReserveApplication.class, args);
    }
}
