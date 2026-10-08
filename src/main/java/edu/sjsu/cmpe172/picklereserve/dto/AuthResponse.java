package edu.sjsu.cmpe172.picklereserve.dto;

public record AuthResponse(long userId, String username, String fullName, String role) {}
