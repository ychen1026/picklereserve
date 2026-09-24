package edu.sjsu.cmpe172.picklereserve;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class PickleReserveIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @AfterEach
    void cleanAppointments() {
        jdbcTemplate.update("DELETE FROM appointments");
    }

    @Test
    void homeReadsSummaryAndSlotsFromSQLite() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.application").value("PickleReserve"))
                .andExpect(jsonPath("$.providerCount").value(2))
                .andExpect(jsonPath("$.serviceCount").value(3))
                .andExpect(jsonPath("$.openSlotCount").value(5))
                .andExpect(jsonPath("$.nextAvailableSlots.length()").value(3));
    }

    @Test
    void slotsFiltersAndPaginatesUsingSql() throws Exception {
        mockMvc.perform(get("/slots")
                        .param("providerId", "1")
                        .param("date", "2026-09-26")
                        .param("page", "0")
                        .param("size", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].providerName").value("Bob Kim"))
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.totalPages").value(2));
    }

    @Test
    void invalidPaginationReturnsStableBadRequestPayload() throws Exception {
        mockMvc.perform(get("/slots").param("size", "0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("size must be between 1 and 100"))
                .andExpect(jsonPath("$.path").value("/slots"));
    }

    @Test
    void databaseEnforcesForeignKeysAndUniqueSlotBookingGuard() {
        Integer foreignKeysEnabled = jdbcTemplate.queryForObject("PRAGMA foreign_keys", Integer.class);
        assertThat(foreignKeysEnabled).isEqualTo(1);

        jdbcTemplate.update("""
                INSERT INTO appointments (user_id, slot_id, service_id, status)
                VALUES (1, 1, 1, 'BOOKED')
                """);

        assertThatThrownBy(() -> jdbcTemplate.update("""
                INSERT INTO appointments (user_id, slot_id, service_id, status)
                VALUES (1, 1, 1, 'BOOKED')
                """))
                .isInstanceOf(DataAccessException.class)
                .hasMessageContaining("SQLITE_CONSTRAINT_UNIQUE")
                .hasMessageContaining("appointments.slot_id");
    }
}
