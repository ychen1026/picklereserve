package edu.sjsu.cmpe172.picklereserve.repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import edu.sjsu.cmpe172.picklereserve.dto.SlotResponse;

@Repository
public class SlotRepository {

    private static final String SELECT_COLUMNS = """
            SELECT slot.id AS slot_id,
                   provider.id AS provider_id,
                   provider_user.display_name AS provider_name,
                   service.id AS service_id,
                   service.name AS service_name,
                   service.duration_minutes,
                   service.price_dollars,
                   slot.start_time,
                   slot.end_time,
                   slot.status
              FROM availability_slots slot
              JOIN providers provider ON provider.id = slot.provider_id
              JOIN users provider_user ON provider_user.id = provider.user_id
              JOIN services service ON service.id = slot.service_id
            """;

    private static final String AVAILABLE_PREDICATE = """
             WHERE slot.status = 'OPEN'
               AND service.active = 1
               AND NOT EXISTS (
                   SELECT 1 FROM appointments appointment
                    WHERE appointment.slot_id = slot.id
                      AND appointment.status = 'BOOKED'
               )
            """;

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public SlotRepository(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<SlotResponse> findAvailable(
            Long providerId,
            Long serviceId,
            LocalDate date,
            int limit,
            int offset) {
        QueryParts query = availableQuery(providerId, serviceId, date);
        query.parameters().put("limit", limit);
        query.parameters().put("offset", offset);
        String sql = SELECT_COLUMNS + AVAILABLE_PREDICATE + query.filters()
                + " ORDER BY slot.start_time, slot.id LIMIT :limit OFFSET :offset";
        return jdbcTemplate.query(sql, query.parameters(), new SlotRowMapper());
    }

    public long countAvailable(Long providerId, Long serviceId, LocalDate date) {
        QueryParts query = availableQuery(providerId, serviceId, date);
        String sql = "SELECT COUNT(*) FROM availability_slots slot "
                + "JOIN services service ON service.id = slot.service_id "
                + AVAILABLE_PREDICATE + query.filters();
        Long count = jdbcTemplate.queryForObject(sql, query.parameters(), Long.class);
        return count == null ? 0 : count;
    }

    private QueryParts availableQuery(Long providerId, Long serviceId, LocalDate date) {
        StringBuilder filters = new StringBuilder();
        Map<String, Object> parameters = new HashMap<>();
        if (providerId != null) {
            filters.append(" AND slot.provider_id = :providerId");
            parameters.put("providerId", providerId);
        }
        if (serviceId != null) {
            filters.append(" AND slot.service_id = :serviceId");
            parameters.put("serviceId", serviceId);
        }
        if (date != null) {
            filters.append(" AND substr(slot.start_time, 1, 10) = :date");
            parameters.put("date", date.toString());
        }
        return new QueryParts(filters.toString(), parameters);
    }

    private record QueryParts(String filters, Map<String, Object> parameters) {
    }

    private static final class SlotRowMapper implements RowMapper<SlotResponse> {
        @Override
        public SlotResponse mapRow(ResultSet resultSet, int rowNumber) throws SQLException {
            return new SlotResponse(
                    resultSet.getLong("slot_id"),
                    resultSet.getLong("provider_id"),
                    resultSet.getString("provider_name"),
                    resultSet.getLong("service_id"),
                    resultSet.getString("service_name"),
                    resultSet.getInt("duration_minutes"),
                    resultSet.getBigDecimal("price_dollars"),
                    OffsetDateTime.parse(resultSet.getString("start_time")),
                    OffsetDateTime.parse(resultSet.getString("end_time")),
                    resultSet.getString("status"));
        }
    }
}
