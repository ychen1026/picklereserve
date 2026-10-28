package edu.sjsu.cmpe172.picklereserve.integration;

import edu.sjsu.cmpe172.picklereserve.dto.NotificationRequest;

public interface NotificationGateway {
    void send(String idempotencyKey, NotificationRequest request);
}
