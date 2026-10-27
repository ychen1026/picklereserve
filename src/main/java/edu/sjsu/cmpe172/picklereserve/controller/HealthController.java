package edu.sjsu.cmpe172.picklereserve.controller;

import java.time.Instant;

import edu.sjsu.cmpe172.picklereserve.dto.HealthResponse;
import edu.sjsu.cmpe172.picklereserve.repository.HealthRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {
    private static final Logger log = LoggerFactory.getLogger(HealthController.class);
    private final HealthRepository healthRepository;

    public HealthController(HealthRepository healthRepository) {
        this.healthRepository = healthRepository;
    }

    @GetMapping("/health")
    public ResponseEntity<HealthResponse> health() {
        try {
            if (healthRepository.canConnect()) {
                return ResponseEntity.ok(new HealthResponse("UP", "UP", Instant.now()));
            }
        } catch (RuntimeException exception) {
            log.error("Health check failed because SQLite is unreachable", exception);
        }
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(new HealthResponse("DOWN", "DOWN", Instant.now()));
    }
}
