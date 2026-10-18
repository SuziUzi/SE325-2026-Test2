package nz.ac.auckland.se325.robolab.web;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import nz.ac.auckland.se325.robolab.model.Booking;
import nz.ac.auckland.se325.robolab.model.BookingRequest;
import nz.ac.auckland.se325.robolab.service.BookingService;
import jakarta.servlet.http.HttpSession;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    /** GET /api/bookings/mine?q=arm&sort=robot_name&dir=desc */
    @GetMapping("/mine")
    public List<Booking> mine(HttpSession session,
                              @RequestParam(required = false) String q,
                              @RequestParam(required = false) String sort,
                              @RequestParam(required = false) String dir) {
        return bookingService.myBookings(CurrentUser.require(session), q, sort, dir);
    }

    @PostMapping
    public ResponseEntity<Booking> create(HttpSession session, @RequestBody BookingRequest request) {
        Booking booking = bookingService.book(CurrentUser.require(session), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(booking);
    }
}
