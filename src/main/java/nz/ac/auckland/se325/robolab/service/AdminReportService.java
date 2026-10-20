package nz.ac.auckland.se325.robolab.service;

import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import nz.ac.auckland.se325.robolab.model.RobotUsage;

/** Usage statistics for lab managers: how many days each robot is booked in a date range. */
@Service
public class AdminReportService {

    private static final String USAGE_REPORT = """
            SELECT r.name AS robot_name, COUNT(b.id) AS bookings
            FROM bookings b JOIN robots r ON r.id = b.robot_id
            WHERE b.booking_date >= ? AND b.booking_date < ?
            GROUP BY r.name
            ORDER BY r.name
            """;

    private final JdbcTemplate jdbc;

    public AdminReportService(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /** @param from inclusive, yyyy-MM-dd   @param to exclusive, yyyy-MM-dd */
    public List<RobotUsage> usage(String from, String to) {
        return jdbc.query(USAGE_REPORT, (rs, i) -> new RobotUsage(
                rs.getString("robot_name"),
                rs.getLong("bookings")), from, to);
    }
}
