package edu.sjsu.cmpe172.picklereserve.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class HealthRepository {
    private final JdbcTemplate jdbc;

    public HealthRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public boolean canConnect() {
        Integer result = jdbc.queryForObject("SELECT 1", Integer.class);
        return result != null && result == 1;
    }
}
