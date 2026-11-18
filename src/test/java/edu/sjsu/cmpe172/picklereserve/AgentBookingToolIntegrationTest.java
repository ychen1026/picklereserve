package edu.sjsu.cmpe172.picklereserve;

import static org.assertj.core.api.Assertions.assertThat;

import edu.sjsu.cmpe172.picklereserve.ai.PendingActionStore;
import edu.sjsu.cmpe172.picklereserve.ai.PickleReserveTools;
import edu.sjsu.cmpe172.picklereserve.service.AppointmentService;
import edu.sjsu.cmpe172.picklereserve.service.BookingService;
import edu.sjsu.cmpe172.picklereserve.service.SlotService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

@SpringBootTest
class AgentBookingToolIntegrationTest {
    @Autowired SlotService slots;
    @Autowired AppointmentService appointments;
    @Autowired BookingService booking;
    @Autowired PendingActionStore pending;
    @Autowired JdbcTemplate jdbc;

    @AfterEach
    void cleanAppointments() {
        jdbc.update("DELETE FROM appointments");
    }

    @Test
    void agentRequiresConfirmationAndStillUsesTransactionalBookingPath() {
        var firstAgent = new PickleReserveTools(1, "demo", slots, appointments, booking, pending);
        String preview = firstAgent.bookAppointment(1, 1, false);
        assertThat(preview).contains("CONFIRMATION_REQUIRED");

        String booked = firstAgent.bookAppointment(1, 1, true);
        assertThat(booked).contains("BOOKED");
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM appointments WHERE slot_id=1 AND status='BOOKED'", Integer.class))
                .isEqualTo(1);

        var secondAgent = new PickleReserveTools(4, "demo-2", slots, appointments, booking, pending);
        secondAgent.bookAppointment(1, 1, false);
        assertThat(secondAgent.bookAppointment(1, 1, true)).contains("no longer available");
    }
}
