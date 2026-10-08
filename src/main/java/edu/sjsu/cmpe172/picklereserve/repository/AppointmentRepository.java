package edu.sjsu.cmpe172.picklereserve.repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.List;
import edu.sjsu.cmpe172.picklereserve.dto.AppointmentResponse;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

@Repository
public class AppointmentRepository {
    private static final String SELECT = """
        SELECT a.id appointment_id, a.slot_id, a.user_id, customer.full_name customer_name,
               p.id provider_id, provider_user.full_name provider_name,
               s.id service_id, s.name service_name, slot.start_time, slot.end_time,
               CASE WHEN a.status = 'BOOKED' AND slot.end_time < strftime('%Y-%m-%dT%H:%M:%S','now') THEN 'COMPLETED' ELSE a.status END display_status
          FROM appointments a
          JOIN users customer ON customer.id = a.user_id
          JOIN availability_slots slot ON slot.id = a.slot_id
          JOIN providers p ON p.id = slot.provider_id
          JOIN users provider_user ON provider_user.id = p.user_id
          JOIN services s ON s.id = a.service_id
        """;
    private final JdbcTemplate jdbc;
    public AppointmentRepository(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    public List<AppointmentResponse> findForUser(long userId) {
        return jdbc.query(SELECT + " WHERE a.user_id = ? ORDER BY slot.start_time DESC", new Mapper(), userId);
    }
    public List<AppointmentResponse> findForProvider(long providerId) {
        return jdbc.query(SELECT + " WHERE p.id = ? ORDER BY slot.start_time DESC", new Mapper(), providerId);
    }
    public int cancelOwned(long appointmentId, long userId) {
        return jdbc.update("UPDATE appointments SET status='CANCELLED', cancelled_at=CURRENT_TIMESTAMP WHERE id=? AND user_id=? AND status='BOOKED'", appointmentId, userId);
    }
    public boolean exists(long appointmentId) {
        Integer n = jdbc.queryForObject("SELECT COUNT(*) FROM appointments WHERE id=?", Integer.class, appointmentId);
        return n != null && n > 0;
    }
    private static class Mapper implements RowMapper<AppointmentResponse> {
        public AppointmentResponse mapRow(ResultSet rs, int row) throws SQLException {
            return new AppointmentResponse(rs.getLong("appointment_id"), rs.getLong("slot_id"), rs.getLong("user_id"), rs.getString("customer_name"),
                    rs.getLong("provider_id"), rs.getString("provider_name"), rs.getLong("service_id"), rs.getString("service_name"),
                    OffsetDateTime.parse(rs.getString("start_time")), OffsetDateTime.parse(rs.getString("end_time")), rs.getString("display_status"));
        }
    }
}
