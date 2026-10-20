package nz.ac.auckland.se325.robolab.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import nz.ac.auckland.se325.robolab.model.User;

@Repository
public class UserRepository {

    private final JdbcTemplate jdbc;

    public UserRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public Optional<User> findByCredentials(String username, String password) {
        String sql = "SELECT id, username, full_name, role FROM users "
                + "WHERE username = ? AND password = ?";

        List<User> users = jdbc.query(sql, (rs, i) -> new User(
                rs.getLong("id"),
                rs.getString("username"),
                rs.getString("full_name"),
                rs.getString("role")), username, password);

        return users.stream().findFirst();
    }
}
