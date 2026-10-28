package edu.sjsu.cmpe172.picklereserve.dto;

public record NotificationResponse(String status, boolean duplicate, String idempotencyKey) {
}
