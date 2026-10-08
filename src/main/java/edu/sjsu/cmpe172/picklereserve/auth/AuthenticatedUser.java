package edu.sjsu.cmpe172.picklereserve.auth;

public record AuthenticatedUser(long userId, String username, String fullName, String role) {
    public boolean isPlayer() { return "PLAYER".equals(role); }
    public boolean isAdmin() { return "ADMIN".equals(role); }
}
