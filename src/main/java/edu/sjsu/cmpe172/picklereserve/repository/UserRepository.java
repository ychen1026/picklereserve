package edu.sjsu.cmpe172.picklereserve.repository;

import java.util.Optional;
import edu.sjsu.cmpe172.picklereserve.model.UserAccount;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class UserRepository {
    private final JdbcTemplate jdbc;
    public UserRepository(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    public Optional<UserAccount> findByUsername(String username) {
        var rows = jdbc.query("SELECT id, username, password_hash, role, full_name FROM users WHERE username = ?",
                (rs, n) -> new UserAccount(rs.getLong("id"), rs.getString("username"), rs.getString("password_hash"), rs.getString("role"), rs.getString("full_name")), username);
        return rows.stream().findFirst();
    }
}
