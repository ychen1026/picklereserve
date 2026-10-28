package edu.sjsu.cmpe172.picklereserve.repository;

import java.time.OffsetDateTime;
import java.util.Optional;

import edu.sjsu.cmpe172.picklereserve.model.NotificationDetails;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class NotificationRepository {
    private final JdbcTemplate jdbc;

    public NotificationRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public Optional<NotificationDetails> findDetails(long appointmentId) {
        var rows = jdbc.query("""
                SELECT a.id appointment_id,
                       customer.username recipient,
                       s.name service_name,
                       provider_user.full_name provider_name,
                       slot.start_time
                  FROM appointments a
                  JOIN users customer ON customer.id = a.user_id
                  JOIN availability_slots slot ON slot.id = a.slot_id
                  JOIN providers p ON p.id = slot.provider_id
                  JOIN users provider_user ON provider_user.id = p.user_id
                  JOIN services s ON s.id = a.service_id
                 WHERE a.id = ?
                """, (resultSet, rowNumber) -> new NotificationDetails(
                resultSet.getLong("appointment_id"),
                resultSet.getString("recipient"),
                resultSet.getString("service_name"),
                resultSet.getString("provider_name"),
                OffsetDateTime.parse(resultSet.getString("start_time"))), appointmentId);
        return rows.stream().findFirst();
    }
}
