CREATE TABLE categorias (
    idCategoria BIGSERIAL PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL UNIQUE
);

CREATE TABLE usuarios (
    idUsuario BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    contrasena VARCHAR(255) NOT NULL,
    rol VARCHAR(50) NOT NULL CHECK (rol IN ('ARQUERO', 'ADMIN'))
);

CREATE TABLE arqueros (
    idArquero BIGSERIAL PRIMARY KEY,
    idUsuario BIGINT NOT NULL UNIQUE, 
    nombre VARCHAR(255) NOT NULL,
    idCategoria BIGINT, 
    CONSTRAINT fk_arquero_usuario FOREIGN KEY (idUsuario) REFERENCES usuarios(idUsuario) ON DELETE CASCADE,
    CONSTRAINT fk_arquero_categoria FOREIGN KEY (idCategoria) REFERENCES categorias(idCategoria) ON DELETE SET NULL
);

CREATE TABLE torneos (
    idTorneo BIGSERIAL PRIMARY KEY,
    nombre VARCHAR(255) NOT NULL,
    idCategoria BIGINT NOT NULL,
    fechaInicio DATE NOT NULL,
    fechaFin DATE NOT NULL,
    Activo BOOLEAN DEFAULT TRUE,
    CONSTRAINT fk_torneo_categoria FOREIGN KEY (idCategoria) REFERENCES categorias(idCategoria) ON DELETE RESTRICT
);

CREATE TABLE inscripciones (
    idInscripcion BIGSERIAL PRIMARY KEY,
    idArquero BIGINT NOT NULL,
    idTorneo BIGINT NOT NULL,
    puntaje INT DEFAULT 0,
    CONSTRAINT fk_inscripcion_arquero FOREIGN KEY (idArquero) REFERENCES arqueros(idArquero) ON DELETE CASCADE,
    CONSTRAINT fk_inscripcion_torneo FOREIGN KEY (idTorneo) REFERENCES torneos(idTorneo) ON DELETE CASCADE,
    CONSTRAINT uq_arquero_torneo UNIQUE(idArquero, idTorneo)
);

CREATE TABLE rondas (
    IdRonda BIGSERIAL PRIMARY KEY,
    idTorneo BIGINT NOT NULL,
    numeroRonda INT NOT NULL,
    puntuacion INT DEFAULT 0,
    CONSTRAINT fk_ronda_torneo FOREIGN KEY (idTorneo) REFERENCES torneos(idTorneo) ON DELETE CASCADE
);

CREATE TABLE flechas (
    IdFlecha BIGSERIAL PRIMARY KEY,
    IdRonda BIGINT NOT NULL,
    idArquero BIGINT NOT NULL,
    puntuacion INT NOT NULL CHECK (puntuacion >= 0 AND puntuacion <= 10),
    CONSTRAINT fk_flecha_ronda FOREIGN KEY (IdRonda) REFERENCES rondas(IdRonda) ON DELETE CASCADE,
    CONSTRAINT fk_flecha_arquero FOREIGN KEY (idArquero) REFERENCES arqueros(idArquero) ON DELETE CASCADE
);

CREATE TABLE rankings (
    idRanking BIGSERIAL PRIMARY KEY,
    idTorneo BIGINT NOT NULL,
    idArquero BIGINT NOT NULL,
    posicion INT NOT NULL,
    puntajeFinal INT NOT NULL,
    CONSTRAINT fk_ranking_torneo FOREIGN KEY (idTorneo) REFERENCES torneos(idTorneo) ON DELETE CASCADE,
    CONSTRAINT fk_ranking_arquero FOREIGN KEY (idArquero) REFERENCES arqueros(idArquero) ON DELETE CASCADE
);

CREATE TABLE auditorias (
    idAuditoria BIGSERIAL PRIMARY KEY,
    idUsuario BIGINT, 
    idInscripcion BIGINT NOT NULL,
    puntajeAnterior INT NOT NULL,
    puntajeNuevo INT NOT NULL,
    fechaModificacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_auditoria_usuario FOREIGN KEY (idUsuario) REFERENCES usuarios(idUsuario) ON DELETE SET NULL,
    CONSTRAINT fk_auditoria_inscripcion FOREIGN KEY (idInscripcion) REFERENCES inscripciones(idInscripcion) ON DELETE CASCADE
);

-- Index para TournamentController
CREATE INDEX idx_torneos_filtros ON torneos(Activo, idCategoria);

-- Index para ArcherController (Podio)
CREATE INDEX idx_rankings_torneo_posicion ON rankings(idTorneo, posicion);

-- Index para ArcherController (Historial)
CREATE INDEX idx_inscripciones_arquero ON inscripciones(idArquero);

-- Index de Agregación para Cálculos (Top Archer)
CREATE INDEX idx_flechas_ronda_arquero ON flechas(IdRonda, idArquero);