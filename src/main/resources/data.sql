-- INSERT OR IGNORE means re-running on an already-seeded robolab.db is a no-op.
-- Passwords are stored in plain text in this legacy system. That is a known issue and
-- is OUT OF SCOPE for this ticket.
INSERT OR IGNORE INTO users (id, username, password, full_name, role) VALUES
    (1, 'admin',   'R0b0t!Adm1n', 'Dr Ada Lovelace', 'ADMIN'),
    (2, 'alice',   'alice123',    'Alice Chen',      'STUDENT'),
    (3, 'bob',     'bob123',      'Bob Smith',       'STUDENT'),
    (4, 'charlie', 'charlie123',  'Charlie Ngata',   'STUDENT');

INSERT OR IGNORE INTO robots (id, name, model, location) VALUES
    (1, 'UR5e Arm',       'Universal Robots UR5e',  'Bay A'),
    (2, 'Spot Quadruped', 'Boston Dynamics Spot',   'Bay B'),
    (3, 'TurtleBot 4',    'Clearpath TurtleBot 4',  'Bay C'),
    (4, 'Panda Arm',      'Franka Emika Panda',     'Bay A');

INSERT OR IGNORE INTO bookings (id, robot_id, user_id, booking_date, purpose) VALUES
    (101, 1, 2, '2026-10-12', 'Pick-and-place calibration'),
    (102, 2, 2, '2026-10-13', 'Gait data collection'),
    (103, 4, 2, '2026-10-14', 'Assignment 2 grasping demo'),
    (104, 1, 3, '2026-10-15', 'CONFIDENTIAL thesis: novel grasp planner'),
    (105, 2, 3, '2026-10-16', 'Spot stair-climbing trial'),
    (106, 4, 4, '2026-10-12', 'Capstone project demo');
