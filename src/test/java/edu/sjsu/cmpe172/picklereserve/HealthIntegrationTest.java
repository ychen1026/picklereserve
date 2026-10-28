package edu.sjsu.cmpe172.picklereserve;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class HealthIntegrationTest {
    @Autowired
    MockMvc mvc;

    @Test
    void healthIncludesDatabaseConnectivity() throws Exception {
        mvc.perform(get("/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.database").value("UP"))
                .andExpect(jsonPath("$.checkedAt").exists());
    }

    @Test
    void bookingMetricIsExposedByActuator() throws Exception {
        mvc.perform(get("/actuator/metrics/picklereserve.bookings.committed"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("picklereserve.bookings.committed"));
    }
}
