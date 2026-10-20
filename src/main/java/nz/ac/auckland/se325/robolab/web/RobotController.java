package nz.ac.auckland.se325.robolab.web;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import nz.ac.auckland.se325.robolab.model.Robot;
import nz.ac.auckland.se325.robolab.model.RobotRequest;
import nz.ac.auckland.se325.robolab.model.ScheduleSlot;
import nz.ac.auckland.se325.robolab.repository.RobotRepository;
import jakarta.servlet.http.HttpSession;

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

    /** POST /api/robots — add a robot (lab managers only). */
    @PostMapping
    public ResponseEntity<Robot> create(HttpSession session, @RequestBody RobotRequest request) {
        CurrentUser.requireAdmin(session);
        if (request == null
                || request.name() == null || request.name().isBlank()
                || request.model() == null || request.model().isBlank()
                || request.location() == null || request.location().isBlank()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Name, model, and location are required");
        }

        Robot created = robots.insert(request.name().trim(), request.model().trim(), request.location().trim());
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /** DELETE /api/robots/{id} — remove an unbooked robot (lab managers only). */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(HttpSession session, @PathVariable long id) {
        CurrentUser.requireAdmin(session);
        if (robots.deleteIfUnbooked(id) > 0) {
            return ResponseEntity.noContent().build();
        }
        if (robots.findById(id).isEmpty()) {
            throw new ApiException(HttpStatus.NOT_FOUND, "No such robot");
        }
        throw new ApiException(HttpStatus.CONFLICT, "Robot still has bookings");
    }

    /** GET /api/robots/1/bookings — the days this robot is already taken. */
    @GetMapping("/{id}/bookings")
    public List<ScheduleSlot> bookings(@PathVariable long id) {
        robots.findById(id).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "No such robot"));
        return robots.findBookings(id);
    }
}
