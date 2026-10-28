package edu.sjsu.cmpe172.picklereserve.integration;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import edu.sjsu.cmpe172.picklereserve.dto.NotificationRequest;
import org.springframework.stereotype.Component;

@Component
public class NotificationStubStore {
    private final Map<String, NotificationRequest> accepted = new ConcurrentHashMap<>();

    public boolean accept(String idempotencyKey, NotificationRequest request) {
        return accepted.putIfAbsent(idempotencyKey, request) != null;
    }

    public int size() {
        return accepted.size();
    }

    public void clear() {
        accepted.clear();
    }
}
