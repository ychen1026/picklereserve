package edu.sjsu.cmpe172.picklereserve.repository;

import java.util.Optional;
import edu.sjsu.cmpe172.picklereserve.model.BookableSlot;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class BookingRepository {
    private final JdbcTemplate jdbc;
    public BookingRepository(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    public Optional<BookableSlot> findSlot(long slotId) {
        var rows = jdbc.query("SELECT id, provider_id, service_id, status, version FROM availability_slots WHERE id = ?",
                (rs, n) -> new BookableSlot(rs.getLong("id"), rs.getLong("provider_id"), rs.getLong("service_id"), rs.getString("status"), rs.getLong("version")), slotId);
        return rows.stream().findFirst();
    }

    public int claimVersion(long slotId, long expectedVersion) {
        return jdbc.update("""
                UPDATE availability_slots
                   SET version = version + 1
                 WHERE id = ? AND status = 'OPEN' AND version = ?
                   AND NOT EXISTS (SELECT 1 FROM appointments WHERE slot_id = ? AND status = 'BOOKED')
                """, slotId, expectedVersion, slotId);
    }

    public long insertAppointment(long userId, long slotId, long serviceId) {
        jdbc.update("INSERT INTO appointments (user_id, slot_id, service_id, status) VALUES (?, ?, ?, 'BOOKED')", userId, slotId, serviceId);
        Long id = jdbc.queryForObject("SELECT id FROM appointments WHERE slot_id = ?", Long.class, slotId);
        return id == null ? 0 : id;
    }
}
