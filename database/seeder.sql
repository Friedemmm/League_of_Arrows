-- ============================================================
-- COMPLETE DATA CLEANUP (reverse dependency order)
-- ============================================================
TRUNCATE TABLE audit_log, rankings, arrows, rounds, inscriptions, tournaments, archers, categories, users RESTART IDENTITY CASCADE;

-- ============================================================
-- INSERT CATEGORIES
-- ============================================================
INSERT INTO categories (name) VALUES 
('Largo'),
('Recurvo'),
('Compuesto'),
('Tradicional');

-- ============================================================
-- INSERT USERS (BCrypt password: 'admin123' for all)
-- ============================================================
INSERT INTO users (email, password, rol) VALUES 
('admin@leagueofarrows.com', '$2a$10$5uAS9l6O9k/nyiS.oKtHAuKr5r.v4vSXuhW7tnbiFEXtszapZvkkq', 'ADMIN'),
('ashe@gmail.com', '$2a$10$5uAS9l6O9k/nyiS.oKtHAuKr5r.v4vSXuhW7tnbiFEXtszapZvkkq', 'ARQUERO'),
('varus@gmail.com', '$2a$10$5uAS9l6O9k/nyiS.oKtHAuKr5r.v4vSXuhW7tnbiFEXtszapZvkkq', 'ARQUERO'),
('kindred@gmail.com', '$2a$10$5uAS9l6O9k/nyiS.oKtHAuKr5r.v4vSXuhW7tnbiFEXtszapZvkkq', 'ARQUERO'),
('vayne@gmail.com', '$2a$10$5uAS9l6O9k/nyiS.oKtHAuKr5r.v4vSXuhW7tnbiFEXtszapZvkkq', 'ARQUERO'),
('quinn@gmail.com', '$2a$10$5uAS9l6O9k/nyiS.oKtHAuKr5r.v4vSXuhW7tnbiFEXtszapZvkkq', 'ARQUERO'),
('twitch@gmail.com', '$2a$10$5uAS9l6O9k/nyiS.oKtHAuKr5r.v4vSXuhW7tnbiFEXtszapZvkkq', 'ARQUERO');

-- ============================================================
-- INSERT ARCHERS (relation with users and categories)
-- ============================================================
INSERT INTO archers (id_user, name, id_category) VALUES 
(2, 'Ashe', 1),
(3, 'Varus', 1),
(4, 'Kindred', 1),
(5, 'Vayne', 2),
(6, 'Quinn', 3),
(7, 'Twitch', 4);

-- ============================================================
-- INSERT TOURNAMENTS
-- ============================================================
INSERT INTO tournaments (name, id_category, start_date, end_date, is_active) VALUES 
('Campeonato mundial de tiro 2026', 1, CURRENT_DATE - INTERVAL '5 days', CURRENT_DATE + INTERVAL '2 days', TRUE),
('MSI 2025', 1, '2025-10-20', '2025-11-05', FALSE);

-- ============================================================
-- INSERT INSCRIPTIONS (initial accumulated score)
-- ============================================================
INSERT INTO inscriptions (id_archer, id_tournament, score) VALUES 
(1, 1, 58),
(2, 1, 56),
(3, 1, 54);

-- ============================================================
-- INSERT ROUNDS
-- ============================================================
INSERT INTO rounds (id_tournament, id_archer, round_number) VALUES 
(1, 1, 1),
(1, 1, 2);

-- ============================================================
-- INSERT ARROWS (individual scores)
-- ============================================================
INSERT INTO arrows (id_round, arrow_number, score) VALUES 
(1, 1, 10),
(1, 2, 10),
(1, 3, 9),
(1, 4, 10),
(1, 5, 9),
(1, 6, 10);

-- ============================================================
-- INSERT AUDIT LOG (score change from 57 to 58 for archer 1, tournament 1)
-- ============================================================
INSERT INTO audit_log (id_archer, id_tournament, old_score, new_score, modified_by, modified_at) VALUES 
(1, 1, 57, 58, 1, CURRENT_TIMESTAMP);

-- ============================================================
-- INSERT PREVIOUS RANKINGS (for tournament 2, MSI 2025)
-- ============================================================
INSERT INTO rankings (id_tournament, id_archer, position, total_score) VALUES 
(2, 1, 1, 680),
(2, 2, 2, 675),
(2, 3, 3, 670);
