
INSERT INTO categorias (nombre) VALUES 
('Largo'),
('Recurvo'),
('Compuesto'),
('Tradicional');

INSERT INTO usuarios (email, contrasena, rol) VALUES 
('admin@leagueofarrows.com', 'Constrasena0', 'ADMIN'),
('ashe@gmail.com', 'Constrasena1', 'ARQUERO'),
('varus@gmail.com', 'Constrasena2', 'ARQUERO'),
('kindred@gmail.com', 'Constrasena3', 'ARQUERO'),
('vayne@gmail.com', 'Constrasen4', 'ARQUERO'),
('quinn@gmail.com', 'Constrasena5', 'ARQUERO'),
('twitch@gmail.com', 'Constrasena6', 'ARQUERO');

INSERT INTO arqueros (idUsuario, nombre, idCategoria) VALUES 
(2, 'Ashe', 1),
(3, 'Varus', 1),
(4, 'Kindred', 1),
(5, 'Vayne', 2),
(6, 'Quinn', 3),
(7, 'Twitch', 4);

INSERT INTO torneos (nombre, idCategoria, fechaInicio, fechaFin, Activo) VALUES 
('Campeonato mundial de tiro 2026', 1, CURRENT_DATE - INTERVAL '5 days', CURRENT_DATE + INTERVAL '2 days', TRUE),
('MSI 2025', 1, '2025-10-20', '2025-11-05', FALSE);

INSERT INTO inscripciones (idArquero, idTorneo, puntaje) VALUES 
(1, 1, 58),
(2, 1, 56),
(3, 1, 54);

INSERT INTO rondas (idTorneo, numeroRonda, puntuacion) VALUES 
(1, 1, 0),
(1, 2, 0);

INSERT INTO flechas (IdRonda, idArquero, puntuacion) VALUES 
(1, 1, 10),
(1, 1, 10),
(1, 1, 9),
(1, 1, 10),
(1, 1, 9),
(1, 1, 10);

INSERT INTO auditorias (idUsuario, idInscripcion, puntajeAnterior, puntajeNuevo) VALUES 
(1, 1, 57, 58);

INSERT INTO rankings (idTorneo, idArquero, posicion, puntajeFinal) VALUES 
(2, 1, 1, 680),
(2, 2, 2, 675),
(2, 3, 3, 670);