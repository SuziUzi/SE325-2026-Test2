package nz.ac.auckland.se325.robolab.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import nz.ac.auckland.se325.robolab.model.Robot;
import nz.ac.auckland.se325.robolab.model.ScheduleSlot;

@Repository
public class RobotRepository {

    private static final RowMapper<Robot> ROBOT = (rs, i) -> new Robot(
            rs.getLong("id"),
            rs.getString("name"),
            rs.getString("model"),
            rs.getString("location"));

    private final JdbcTemplate jdbc;

    public RobotRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<Robot> findAll() {
        return jdbc.query("SELECT id, name, model, location FROM robots ORDER BY id", ROBOT);
    }

    public Optional<Robot> findById(long id) {
        return jdbc.query("SELECT id, name, model, location FROM robots WHERE id = " + id, ROBOT)
                .stream().findFirst();
    }

    /** The days a robot is already booked, with who booked each (purpose hidden). */
    public List<ScheduleSlot> findBookings(long robotId) {
        String sql = "SELECT b.id, b.booking_date, u.username "
                + "FROM bookings b JOIN users u ON u.id = b.user_id "
                + "WHERE b.robot_id = " + robotId + " "
                + "ORDER BY b.booking_date";

        return jdbc.query(sql, (rs, i) -> new ScheduleSlot(
                rs.getLong(1),
                LocalDate.parse(rs.getString(2)),
                rs.getString(3)));
    }
}
