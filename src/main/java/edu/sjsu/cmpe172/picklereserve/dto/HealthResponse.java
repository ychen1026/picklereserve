package edu.sjsu.cmpe172.picklereserve.dto;

import java.time.Instant;

public record HealthResponse(String status, String database, Instant checkedAt) {
}
