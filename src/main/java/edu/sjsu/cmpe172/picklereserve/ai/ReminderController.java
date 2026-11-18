package edu.sjsu.cmpe172.picklereserve.ai;

import java.util.Map;
import edu.sjsu.cmpe172.picklereserve.service.AuthService;
import jakarta.servlet.http.HttpSession;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Profile("ai")
@RestController
@RequestMapping("/api/ai/reminders")
public class ReminderController {
    private final AuthService auth;
    private final AutonomousReminderAgent reminders;

    public ReminderController(AuthService auth, AutonomousReminderAgent reminders) {
        this.auth = auth;
        this.reminders = reminders;
    }

    @PostMapping("/run")
    public Map<String, Object> run(HttpSession session) {
        auth.requireAdmin(session);
        return Map.of("sent", reminders.runReminders());
    }
}
