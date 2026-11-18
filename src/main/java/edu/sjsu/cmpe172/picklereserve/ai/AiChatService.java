package edu.sjsu.cmpe172.picklereserve.ai;

import edu.sjsu.cmpe172.picklereserve.service.AppointmentService;
import edu.sjsu.cmpe172.picklereserve.service.BookingService;
import edu.sjsu.cmpe172.picklereserve.service.SlotService;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Profile("ai")
@Service
public class AiChatService {
    private final ChatClient chatClient;
    private final SlotService slots;
    private final AppointmentService appointments;
    private final BookingService booking;
    private final PendingActionStore pending;
    private final AiConversationStore memory;

    public AiChatService(ChatClient chatClient, SlotService slots, AppointmentService appointments,
            BookingService booking, PendingActionStore pending, AiConversationStore memory) {
        this.chatClient = chatClient;
        this.slots = slots;
        this.appointments = appointments;
        this.booking = booking;
        this.pending = pending;
        this.memory = memory;
    }

    public String chat(long userId, String conversationId, String message) {
        String key = userId + ":" + conversationId;
        String prior = memory.context(key);
        PickleReserveTools tools = new PickleReserveTools(userId, conversationId, slots, appointments, booking, pending);
        String prompt = (prior.isBlank() ? "" : "Previous conversation:\n" + prior + "\n\n") + "Current user message:\n" + message;
        String answer = chatClient.prompt()
                .system("You are PickleReserve's booking assistant. Use tools for live availability and appointments. "
                        + "You act only as the authenticated user. Never invent slot or appointment data. "
                        + "For booking or cancellation, first use confirmed=false and show the summary. "
                        + "Only use confirmed=true after the user clearly confirms the exact pending action. "
                        + "If a slot is taken, explain it and offer alternatives.")
                .user(prompt)
                .tools(tools)
                .call()
                .content();
        memory.add(key, message, answer);
        return answer;
    }
}
