package edu.sjsu.cmpe172.picklereserve.service;

import edu.sjsu.cmpe172.picklereserve.auth.AuthenticatedUser;
import edu.sjsu.cmpe172.picklereserve.error.AuthenticationRequiredException;
import edu.sjsu.cmpe172.picklereserve.error.ForbiddenException;
import edu.sjsu.cmpe172.picklereserve.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    public static final String SESSION_USER = "AUTH_USER";
    private final UserRepository users;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
    public AuthService(UserRepository users) { this.users = users; }

    public AuthenticatedUser login(String username, String password, HttpSession session) {
        var account = users.findByUsername(username).orElseThrow(() -> new AuthenticationRequiredException("Invalid username or password"));
        if (!encoder.matches(password, account.passwordHash())) throw new AuthenticationRequiredException("Invalid username or password");
        var user = new AuthenticatedUser(account.id(), account.username(), account.fullName(), account.role());
        session.setAttribute(SESSION_USER, user);
        return user;
    }
    public AuthenticatedUser requireUser(HttpSession session) {
        Object value = session.getAttribute(SESSION_USER);
        if (!(value instanceof AuthenticatedUser user)) throw new AuthenticationRequiredException("Login required");
        return user;
    }
    public AuthenticatedUser requirePlayer(HttpSession session) {
        var user = requireUser(session); if (!user.isPlayer()) throw new ForbiddenException("Player access required"); return user;
    }
    public AuthenticatedUser requireAdmin(HttpSession session) {
        var user = requireUser(session); if (!user.isAdmin()) throw new ForbiddenException("Provider access required"); return user;
    }
}
