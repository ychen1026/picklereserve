package edu.sjsu.cmpe172.picklereserve;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import edu.sjsu.cmpe172.picklereserve.integration.NotificationStubStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class MockNotificationControllerIntegrationTest {
    @Autowired
    MockMvc mvc;

    @Autowired
    NotificationStubStore store;

    @BeforeEach
    void clearStore() {
        store.clear();
    }

    @Test
    void repeatedIdempotencyKeyIsAcceptedOnlyOnce() throws Exception {
        String body = """
                {"recipient":"player.demo","type":"BOOKING_CONFIRMED","message":"Court reserved."}
                """;
        mvc.perform(post("/mock-notifications")
                        .header("Idempotency-Key", "appointment-42-booking_confirmed")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.duplicate").value(false));

        mvc.perform(post("/mock-notifications")
                        .header("Idempotency-Key", "appointment-42-booking_confirmed")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.duplicate").value(true));

        assertThat(store.size()).isEqualTo(1);
    }
}
