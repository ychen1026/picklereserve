package edu.sjsu.cmpe172.picklereserve.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record NotificationRequest(
        @NotBlank @Size(max = 120) String recipient,
        @NotBlank @Size(max = 60) String type,
        @NotBlank @Size(max = 1000) String message) {
}
