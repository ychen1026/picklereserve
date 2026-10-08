package edu.sjsu.cmpe172.picklereserve.model;

public record BookableSlot(long id, long providerId, long serviceId, String status, long version) {}
