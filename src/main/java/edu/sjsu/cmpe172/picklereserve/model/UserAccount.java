package edu.sjsu.cmpe172.picklereserve.model;

public record UserAccount(long id, String username, String passwordHash, String role, String fullName) {}
