CREATE TABLE categories (
    id_category BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE
);

CREATE TABLE users (
    id_user BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    rol VARCHAR(50) NOT NULL CHECK (rol IN ('ARQUERO', 'ADMIN'))
);

CREATE TABLE archers (
    id_archer BIGSERIAL PRIMARY KEY,
    id_user BIGINT NOT NULL UNIQUE, 
    name VARCHAR(255) NOT NULL,
    id_category BIGINT, 
    CONSTRAINT fk_archer_user FOREIGN KEY (id_user) REFERENCES users(id_user) ON DELETE CASCADE,
    CONSTRAINT fk_archer_category FOREIGN KEY (id_category) REFERENCES categories(id_category) ON DELETE SET NULL
);

CREATE TABLE tournaments (
    id_tournament BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    id_category BIGINT NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    is_active BOOLEAN DEFAULT TRUE,
    CONSTRAINT fk_tournament_category FOREIGN KEY (id_category) REFERENCES categories(id_category) ON DELETE RESTRICT
);

CREATE TABLE inscriptions (
    id_inscription BIGSERIAL PRIMARY KEY,
    id_archer BIGINT NOT NULL,
    id_tournament BIGINT NOT NULL,
    score INT DEFAULT 0,
    CONSTRAINT fk_inscription_archer FOREIGN KEY (id_archer) REFERENCES archers(id_archer) ON DELETE CASCADE,
    CONSTRAINT fk_inscription_tournament FOREIGN KEY (id_tournament) REFERENCES tournaments(id_tournament) ON DELETE CASCADE,
    CONSTRAINT uq_archer_tournament UNIQUE(id_archer, id_tournament)
);

CREATE TABLE rounds (
    id_round BIGSERIAL PRIMARY KEY,
    id_tournament BIGINT NOT NULL,
    id_archer BIGINT NOT NULL, 
    round_number INT NOT NULL,
    registered_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_round_tournament FOREIGN KEY (id_tournament) REFERENCES tournaments(id_tournament) ON DELETE CASCADE,
    CONSTRAINT fk_round_archer FOREIGN KEY (id_archer) REFERENCES archers(id_archer) ON DELETE CASCADE
);

CREATE TABLE arrows (
    id_arrow BIGSERIAL PRIMARY KEY,
    id_round BIGINT NOT NULL,
    arrow_number INT NOT NULL,
    score INT NOT NULL CHECK (score >= 0 AND score <= 10),
    CONSTRAINT fk_arrow_round FOREIGN KEY (id_round) REFERENCES rounds(id_round) ON DELETE CASCADE
);

CREATE TABLE rankings (
    id_ranking BIGSERIAL PRIMARY KEY,
    id_tournament BIGINT NOT NULL,
    id_archer BIGINT NOT NULL,
    total_score INT NOT NULL,
    position INT NOT NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_ranking_tournament FOREIGN KEY (id_tournament) REFERENCES tournaments(id_tournament) ON DELETE CASCADE,
    CONSTRAINT fk_ranking_archer FOREIGN KEY (id_archer) REFERENCES archers(id_archer) ON DELETE CASCADE
);

CREATE TABLE audit_log (
    id_audit BIGSERIAL PRIMARY KEY,
    id_archer BIGINT NOT NULL,
    id_tournament BIGINT NOT NULL,
    old_score INT NOT NULL,
    new_score INT NOT NULL,
    modified_by BIGINT, 
    modified_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_audit_archer FOREIGN KEY (id_archer) REFERENCES archers(id_archer) ON DELETE CASCADE,
    CONSTRAINT fk_audit_tournament FOREIGN KEY (id_tournament) REFERENCES tournaments(id_tournament) ON DELETE CASCADE,
    CONSTRAINT fk_audit_user FOREIGN KEY (modified_by) REFERENCES users(id_user) ON DELETE SET NULL
);

CREATE INDEX idx_tournaments_category ON tournaments (id_category);
CREATE INDEX idx_rounds_archer ON rounds (id_archer);
CREATE INDEX idx_rounds_tournament ON rounds (id_tournament);
CREATE INDEX idx_rankings_tournament_position ON rankings (id_tournament, position);
CREATE INDEX idx_audit_log_tournament_archer ON audit_log (id_tournament, id_archer);