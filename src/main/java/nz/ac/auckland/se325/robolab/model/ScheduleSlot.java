package nz.ac.auckland.se325.robolab.model;

import java.time.LocalDate;

/** A day on which a robot is already booked. Purpose is deliberately hidden from other users. */
public record ScheduleSlot(Long bookingId, LocalDate date, String bookedBy) {
}
