package edu.sjsu.cmpe172.picklereserve.ai;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;
import org.springframework.context.annotation.Profile;

@Profile("ai")
@Component
public class AiConversationStore {
    private final Map<String, Deque<String>> conversations = new ConcurrentHashMap<>();

    public String context(String key) {
        return String.join("\n", conversations.getOrDefault(key, new ArrayDeque<>()));
    }

    public void add(String key, String userMessage, String assistantMessage) {
        Deque<String> turns = conversations.computeIfAbsent(key, ignored -> new ArrayDeque<>());
        turns.addLast("User: " + userMessage);
        turns.addLast("Assistant: " + assistantMessage);
        while (turns.size() > 12) turns.removeFirst();
    }
}
