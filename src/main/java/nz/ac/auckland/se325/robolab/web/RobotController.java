package nz.ac.auckland.se325.robolab.web;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import nz.ac.auckland.se325.robolab.model.Robot;
import nz.ac.auckland.se325.robolab.model.ScheduleSlot;
import nz.ac.auckland.se325.robolab.repository.RobotRepository;

@RestController
@RequestMapping("/api/robots")
public class RobotController {

    private final RobotRepository robots;

    public RobotController(RobotRepository robots) {
        this.robots = robots;
    }

    /** GET /api/robots — all robots. */
    @GetMapping
    public List<Robot> list() {
        return robots.findAll();
    }

    /** GET /api/robots/1/bookings — the days this robot is already taken. */
    @GetMapping("/{id}/bookings")
    public List<ScheduleSlot> bookings(@PathVariable long id) {
        robots.findById(id).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "No such robot"));
        return robots.findBookings(id);
    }
}
