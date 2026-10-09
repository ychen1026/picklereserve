INSERT OR IGNORE INTO users (id, username, password_hash, role, full_name)
VALUES
    (1, 'player.demo', '$2y$10$NmUiqTG0XbEynUrB.6tijump9RDyrt/DRbVKLRAaOb5TEF4Mh7uJq', 'PLAYER', 'Demo Player'),
    (2, 'coach.bob', '$2y$10$NmUiqTG0XbEynUrB.6tijump9RDyrt/DRbVKLRAaOb5TEF4Mh7uJq', 'ADMIN', 'Bob Kim'),
    (3, 'coach.tanner', '$2y$10$NmUiqTG0XbEynUrB.6tijump9RDyrt/DRbVKLRAaOb5TEF4Mh7uJq', 'ADMIN', 'Tanner Lee'),
    (4, 'player.two', '$2y$10$NmUiqTG0XbEynUrB.6tijump9RDyrt/DRbVKLRAaOb5TEF4Mh7uJq', 'PLAYER', 'Second Player');

INSERT OR IGNORE INTO providers (id, user_id, bio)
VALUES
    (1, 2, 'Certified pickleball coach specializing in beginner fundamentals.'),
    (2, 3, 'Competitive doubles coach and match-play strategist.');

INSERT OR IGNORE INTO services
    (id, provider_id, name, description, duration_minutes, price_dollars, active)
VALUES
    (1, 1, 'Court Rental', 'One reserved court for recreational play.', 60, 24.00, 1),
    (2, 1, 'Beginner Lesson', 'Private lesson covering rules, grip, serve, and scoring.', 60, 60.00, 1),
    (3, 2, 'Doubles Strategy', 'Small-group positioning and match strategy session.', 90, 85.00, 1);

INSERT OR IGNORE INTO availability_slots
    (id, provider_id, service_id, start_time, end_time, status)
VALUES
    (1, 1, 1, '2026-10-15T09:00:00-07:00', '2026-10-15T10:00:00-07:00', 'OPEN'),
    (2, 1, 2, '2026-10-15T10:30:00-07:00', '2026-10-15T11:30:00-07:00', 'OPEN'),
    (3, 2, 3, '2026-10-16T13:00:00-07:00', '2026-10-16T14:30:00-07:00', 'OPEN'),
    (4, 1, 1, '2026-10-17T18:00:00-07:00', '2026-10-17T19:00:00-07:00', 'OPEN'),
    (5, 2, 3, '2026-10-18T17:00:00-07:00', '2026-10-18T18:30:00-07:00', 'OPEN');
