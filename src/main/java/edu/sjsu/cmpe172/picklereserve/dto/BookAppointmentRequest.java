package edu.sjsu.cmpe172.picklereserve.dto;

import jakarta.validation.constraints.Positive;

public record BookAppointmentRequest(@Positive long slotId, @Positive long serviceId) {}
