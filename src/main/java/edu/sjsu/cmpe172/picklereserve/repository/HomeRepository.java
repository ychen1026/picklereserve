package edu.sjsu.cmpe172.picklereserve.repository;

import edu.sjsu.cmpe172.picklereserve.model.HomeStatistics;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class HomeRepository {

    private final JdbcTemplate jdbcTemplate;

    public HomeRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public HomeStatistics loadStatistics() {
        String sql = """
                SELECT
                    (SELECT COUNT(*) FROM providers) AS provider_count,
                    (SELECT COUNT(*) FROM services WHERE active = 1) AS service_count,
                    (SELECT COUNT(*)
                       FROM availability_slots slot
                      WHERE slot.status = 'OPEN'
                        AND NOT EXISTS (
                            SELECT 1 FROM appointments appointment
                             WHERE appointment.slot_id = slot.id
                               AND appointment.status = 'BOOKED'
                        )) AS open_slot_count
                """;
        return jdbcTemplate.queryForObject(sql, (resultSet, rowNumber) -> new HomeStatistics(
                resultSet.getLong("provider_count"),
                resultSet.getLong("service_count"),
                resultSet.getLong("open_slot_count")));
    }
}
