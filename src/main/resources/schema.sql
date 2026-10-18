CREATE TABLE IF NOT EXISTS users (
    id        INTEGER PRIMARY KEY AUTOINCREMENT,
    username  TEXT NOT NULL UNIQUE,
    password  TEXT NOT NULL,
    full_name TEXT NOT NULL,
    role      TEXT NOT NULL
);

CREATE TABLE IF NOT EXISTS robots (
    id       INTEGER PRIMARY KEY AUTOINCREMENT,
    name     TEXT NOT NULL,
    model    TEXT NOT NULL,
    location TEXT NOT NULL
);

CREATE TABLE IF NOT EXISTS bookings (
    id           INTEGER PRIMARY KEY AUTOINCREMENT,
    robot_id     INTEGER NOT NULL REFERENCES robots (id),
    user_id      INTEGER NOT NULL REFERENCES users (id),
    booking_date TEXT NOT NULL,   -- ISO-8601 date, e.g. 2026-10-12 (a robot is booked for the whole day)
    purpose      TEXT NOT NULL
);
