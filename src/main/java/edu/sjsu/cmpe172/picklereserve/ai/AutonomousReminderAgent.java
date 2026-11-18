package edu.sjsu.cmpe172.picklereserve.ai;

import java.time.format.DateTimeFormatter;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import edu.sjsu.cmpe172.picklereserve.dto.NotificationRequest;
import edu.sjsu.cmpe172.picklereserve.integration.NotificationGateway;
import edu.sjsu.cmpe172.picklereserve.model.NotificationDetails;
import edu.sjsu.cmpe172.picklereserve.repository.NotificationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Profile("ai")
@Service
public class AutonomousReminderAgent {
    private static final Logger log = LoggerFactory.getLogger(AutonomousReminderAgent.class);
    private static final DateTimeFormatter FORMAT = DateTimeFormatter.ofPattern("MMM d, uuuu h:mm a xxx");
    private final NotificationRepository appointments;
    private final ChatClient chatClient;
    private final NotificationGateway notifications;
    private final Set<String> sentKeys = ConcurrentHashMap.newKeySet();

    public AutonomousReminderAgent(NotificationRepository appointments, ChatClient chatClient,
            NotificationGateway notifications) {
        this.appointments = appointments;
        this.chatClient = chatClient;
        this.notifications = notifications;
    }

    @Scheduled(cron = "${ai.reminder.cron}")
    public void scheduledReminderRun() {
        runReminders();
    }

    public int runReminders() {
        int sent = 0;
        for (NotificationDetails details : appointments.findUpcoming(50)) {
            String key = "appointment-" + details.appointmentId() + "-ai-reminder";
            if (!sentKeys.add(key)) continue;
            String reminder = chatClient.prompt()
                    .system("Write one friendly, concise appointment reminder. Do not invent details. "
                            + "Mention the service, provider, and time. Address the customer by first name only.")
                    .user("Customer: " + details.customerName() + "\nService: " + details.serviceName()
                            + "\nProvider: " + details.providerName() + "\nTime: " + details.startsAt().format(FORMAT))
                    .call().content();
            notifications.send(key, new NotificationRequest(details.recipient(), "AI_APPOINTMENT_REMINDER", reminder));
            log.info("Autonomous AI reminder sent appointmentId={} key={}", details.appointmentId(), key);
            sent++;
        }
        return sent;
    }
}
