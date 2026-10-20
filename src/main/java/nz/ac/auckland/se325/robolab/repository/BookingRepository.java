package nz.ac.auckland.se325.robolab.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import nz.ac.auckland.se325.robolab.model.Booking;

@Repository
public class BookingRepository {

    private static final String SELECT_BOOKING =
            "SELECT b.id, b.robot_id, r.name AS robot_name, b.user_id, u.username, "
            + "b.booking_date, b.purpose "
            + "FROM bookings b "
            + "JOIN robots r ON r.id = b.robot_id "
            + "JOIN users u ON u.id = b.user_id ";

    private static final RowMapper<Booking> BOOKING = (rs, i) -> new Booking(
            rs.getLong("id"),
            rs.getLong("robot_id"),
            rs.getString("robot_name"),
            rs.getLong("user_id"),
            rs.getString("username"),
            LocalDate.parse(rs.getString("booking_date")),
            rs.getString("purpose"));

    private final JdbcTemplate jdbc;

    public BookingRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /**
     * Bookings belonging to one user, optionally filtered by robot name.
     *
     * @param search case-insensitive substring of the robot name (may be empty)
     * @param sort   column to sort by: booking_date or robot_name
     * @param dir    asc or desc
     */
    public List<Booking> findForUser(long userId, String search, String sort, String dir) {
        String sortColumn = "robot_name".equalsIgnoreCase(sort) ? "robot_name" : "booking_date";
        String direction = "desc".equalsIgnoreCase(dir) ? "DESC" : "ASC";
        String sql = SELECT_BOOKING
                + "WHERE b.user_id = ? "
                + "AND LOWER(r.name) LIKE LOWER(?) "
                + "ORDER BY " + sortColumn + " " + direction;

        return jdbc.query(sql, BOOKING, userId, "%" + search + "%");
    }

    public Optional<Booking> findById(long id) {
        return jdbc.query(SELECT_BOOKING + "WHERE b.id = ?", BOOKING, id).stream().findFirst();
    }

    public Optional<Long> insertIfAvailable(long robotId, long userId, String date, String purpose) {
        String sql = "INSERT INTO bookings (robot_id, user_id, booking_date, purpose) "
                + "VALUES (?, ?, ?, ?) "
                + "ON CONFLICT (robot_id, booking_date) DO NOTHING "
                + "RETURNING id";
        return jdbc.query(sql, (rs, i) -> rs.getLong("id"), robotId, userId, date, purpose)
                .stream()
                .findFirst();
    }

    public int deleteById(long id) {
        return jdbc.update("DELETE FROM bookings WHERE id = ?", id);
    }

    public int deleteByIdAndUserId(long id, long userId) {
        return jdbc.update("DELETE FROM bookings WHERE id = ? AND user_id = ?", id, userId);
    }
}
