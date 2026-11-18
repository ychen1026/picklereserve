package edu.sjsu.cmpe172.picklereserve.ai;

import java.util.UUID;
import edu.sjsu.cmpe172.picklereserve.service.AuthService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.*;

@Profile("ai")
@RestController
@RequestMapping("/api/ai")
public class AiChatController {
    private final AuthService auth;
    private final AiChatService chat;
    private final RagService rag;

    public AiChatController(AuthService auth, AiChatService chat, RagService rag) {
        this.auth = auth;
        this.chat = chat;
        this.rag = rag;
    }

    @PostMapping("/chat")
    public AiResponse chat(@Valid @RequestBody ChatRequest request, HttpSession session) {
        var user = auth.requirePlayer(session);
        String conversationId = request.conversationId() == null || request.conversationId().isBlank()
                ? UUID.randomUUID().toString() : request.conversationId();
        return new AiResponse(conversationId, chat.chat(user.userId(), conversationId, request.message()));
    }

    @GetMapping("/faq")
    public AiResponse faq(@RequestParam @NotBlank String question, HttpSession session) {
        auth.requireUser(session);
        return new AiResponse("faq", rag.answer(question));
    }

    public record ChatRequest(String conversationId, @NotBlank String message) {}
    public record AiResponse(String conversationId, String answer) {}
}
