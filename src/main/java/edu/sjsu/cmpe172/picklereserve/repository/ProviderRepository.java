package edu.sjsu.cmpe172.picklereserve.repository;

import java.time.OffsetDateTime;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class ProviderRepository {
    private final JdbcTemplate jdbc;
    public ProviderRepository(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    public Optional<Long> providerIdForUser(long userId) {
        var rows = jdbc.query("SELECT id FROM providers WHERE user_id=?", (rs,n)->rs.getLong(1), userId);
        return rows.stream().findFirst();
    }
    public boolean ownsService(long providerId, long serviceId) {
        Integer n = jdbc.queryForObject("SELECT COUNT(*) FROM services WHERE id=? AND provider_id=? AND active=1", Integer.class, serviceId, providerId);
        return n != null && n == 1;
    }
    public long createSlot(long providerId, long serviceId, OffsetDateTime start, OffsetDateTime end) {
        jdbc.update("INSERT INTO availability_slots(provider_id,service_id,start_time,end_time,status,version) VALUES(?,?,?,?,'OPEN',0)", providerId, serviceId, start.toString(), end.toString());
        Long id = jdbc.queryForObject("SELECT id FROM availability_slots WHERE provider_id=? AND start_time=?", Long.class, providerId, start.toString());
        return id == null ? 0 : id;
    }
    public int removeOwnedSlot(long slotId, long providerId) {
        return jdbc.update("""
            UPDATE availability_slots SET status='REMOVED', version=version+1
             WHERE id=? AND provider_id=? AND status='OPEN'
               AND NOT EXISTS (SELECT 1 FROM appointments WHERE slot_id=? AND status='BOOKED')
            """, slotId, providerId, slotId);
    }
}
