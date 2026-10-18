package nz.ac.auckland.se325.robolab;

import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

/**
 * Task 2 — a robot must never be double-booked for a day, even under concurrent requests.
 *
 * A check in the service code is not enough: two requests can both pass the check and then both
 * insert. The only guarantee that survives any interleaving is one the DATABASE enforces, so this
 * test asserts the database itself rejects a duplicate (robot, day). A correct fix therefore adds
 * a UNIQUE constraint on (robot_id, booking_date); this test then turns GREEN. Don't edit this file.
 */
@SpringBootTest
@Transactional
@DisplayName("Task 2 — a robot cannot be double-booked for a day")
class Task2ConcurrencyTest {

    @Autowired JdbcTemplate jdbc;

    @Test
    @DisplayName("the database rejects a second booking for the same robot and day")
    void databaseEnforcesOneBookingPerRobotPerDay() {
        // Seed row 101 already books robot 1 on 2026-10-12. Inserting another booking for the
        // same robot and day — bypassing the service entirely, as a race effectively would —
        // must fail at the database level.
        assertThrows(DataAccessException.class, () ->
                jdbc.update("INSERT INTO bookings (robot_id, user_id, booking_date, purpose) "
                        + "VALUES (1, 3, '2026-10-12', 'duplicate that must be rejected')"));
    }
}
