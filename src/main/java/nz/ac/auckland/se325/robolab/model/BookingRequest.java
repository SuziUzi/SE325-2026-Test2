package nz.ac.auckland.se325.robolab.model;

/** date is an ISO-8601 local date, e.g. "2026-10-20". A robot is booked for the whole day. */
public record BookingRequest(Long robotId, String date, String purpose) {
}
