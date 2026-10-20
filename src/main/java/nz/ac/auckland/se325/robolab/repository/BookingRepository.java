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

    /** Is this robot already booked on the given day? */
    public boolean isBookedOn(long robotId, String date) {
        Integer count = jdbc.queryForObject("SELECT COUNT(*) FROM bookings "
                + "WHERE robot_id = ? AND booking_date = ?", Integer.class, robotId, date);
        return count != null && count > 0;
    }

    public long insert(long robotId, long userId, String date, String purpose) {
        jdbc.update("INSERT INTO bookings (robot_id, user_id, booking_date, purpose) VALUES (?, ?, ?, ?)",
                robotId, userId, date, purpose);
        return jdbc.queryForObject("SELECT MAX(id) FROM bookings", Long.class);
    }
}
