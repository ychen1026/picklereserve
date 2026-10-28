package edu.sjsu.cmpe172.picklereserve.integration;

public record AppointmentNotificationEvent(long appointmentId, NotificationType type) {
}
