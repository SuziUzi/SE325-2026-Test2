package nz.ac.auckland.se325.robolab.model;

import java.time.LocalDate;

public record Booking(
        Long id,
        Long robotId,
        String robotName,
        Long userId,
        String username,
        LocalDate date,
        String purpose) {
}
