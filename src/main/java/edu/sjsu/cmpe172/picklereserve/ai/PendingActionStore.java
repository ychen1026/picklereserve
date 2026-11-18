package edu.sjsu.cmpe172.picklereserve.ai;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

@Component
public class PendingActionStore {
    private final Map<String, PendingAction> actions = new ConcurrentHashMap<>();

    public void put(String key, PendingAction action) {
        actions.put(key, action);
    }

    public PendingAction remove(String key) {
        return actions.remove(key);
    }

    public record PendingAction(String type, long appointmentOrSlotId, long serviceId, String summary) {
    }
}
