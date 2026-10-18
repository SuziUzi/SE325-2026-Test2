package nz.ac.auckland.se325.robolab.service;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import nz.ac.auckland.se325.robolab.model.Booking;
import nz.ac.auckland.se325.robolab.model.BookingRequest;
import nz.ac.auckland.se325.robolab.model.Robot;
import nz.ac.auckland.se325.robolab.model.User;
import nz.ac.auckland.se325.robolab.repository.BookingRepository;
import nz.ac.auckland.se325.robolab.repository.RobotRepository;
import nz.ac.auckland.se325.robolab.web.ApiException;

@Service
public class BookingService {

    static final int MAX_PURPOSE_LENGTH = 500;

    private final BookingRepository bookings;
    private final RobotRepository robots;

    public BookingService(BookingRepository bookings, RobotRepository robots) {
        this.bookings = bookings;
        this.robots = robots;
    }

    public List<Booking> myBookings(User user, String search, String sort, String dir) {
        String q = search == null ? "" : search.trim();
        String sortBy = (sort == null || sort.isBlank()) ? "booking_date" : sort;
        String direction = (dir == null || dir.isBlank()) ? "asc" : dir;
        return bookings.findForUser(user.id(), q, sortBy, direction);
    }

    @Transactional
    public Booking book(User user, BookingRequest request) {
        if (request.robotId() == null) {
            throw new IllegalArgumentException("robotId is required");
        }
        if (request.purpose() == null || request.purpose().isBlank()) {
            throw new IllegalArgumentException("Please describe what the booking is for");
        }
        if (request.purpose().length() > MAX_PURPOSE_LENGTH) {
            throw new IllegalArgumentException("Purpose must be at most " + MAX_PURPOSE_LENGTH + " characters");
        }

        LocalDate date = parseDate(request.date());

        Robot robot = robots.findById(request.robotId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "No such robot"));
        if (bookings.isBookedOn(robot.id(), date.toString())) {
            throw new ApiException(HttpStatus.CONFLICT, robot.name() + " is already booked on " + date);
        }

        long id = bookings.insert(robot.id(), user.id(), date.toString(), request.purpose().trim());
        return bookings.findById(id).orElseThrow();
    }

    private static LocalDate parseDate(String value) {
        try {
            return LocalDate.parse(value);
        } catch (DateTimeParseException | NullPointerException e) {
            throw new IllegalArgumentException("date must look like 2026-10-20");
        }
    }
}
