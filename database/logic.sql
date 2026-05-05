CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- ============================================================
-- 1. STORED PROCEDURE 1: Registrar Puntuación de Ronda
-- ============================================================
CREATE OR REPLACE PROCEDURE registrar_puntuacion_ronda(
    p_idTorneo BIGINT,
    p_idArquero BIGINT,
    p_numeroRonda INT,
    p_scores INTEGER[]
)
LANGUAGE plpgsql
AS $$
DECLARE
    v_is_active BOOLEAN;
    v_idRonda BIGINT;
    v_score INTEGER;
BEGIN
    -- 1. Validar que el torneo existe y está activo
    SELECT Activo INTO v_is_active FROM torneos WHERE idTorneo = p_idTorneo;
    IF NOT FOUND THEN
        RAISE EXCEPTION 'Torneo con id % no existe.', p_idTorneo;
    END IF;

    IF NOT v_is_active THEN
        RAISE EXCEPTION 'El torneo % no está activo.', p_idTorneo;
    END IF;

    -- 2. Validar que el arquero existe
    IF NOT EXISTS (SELECT 1 FROM arqueros WHERE idArquero = p_idArquero) THEN
        RAISE EXCEPTION 'Arquero con id % no existe.', p_idArquero;
    END IF;

    -- 3. Validar si la ronda ya existe para el torneo; si no, crearla
    SELECT IdRonda INTO v_idRonda FROM rondas WHERE idTorneo = p_idTorneo AND numeroRonda = p_numeroRonda LIMIT 1;
    
    IF NOT FOUND THEN
        INSERT INTO rondas (idTorneo, numeroRonda, puntuacion)
        VALUES (p_idTorneo, p_numeroRonda, 0)
        RETURNING IdRonda INTO v_idRonda;
    END IF;

    -- 4. Validar que no haya flechas ya registradas para este arquero en esta ronda
    IF EXISTS (SELECT 1 FROM flechas WHERE IdRonda = v_idRonda AND idArquero = p_idArquero) THEN
        RAISE EXCEPTION 'Las flechas de la ronda % para el arquero % ya fueron registradas.', p_numeroRonda, p_idArquero;
    END IF;

    -- 5. Insertar cada flecha
    FOREACH v_score IN ARRAY p_scores
    LOOP
        INSERT INTO flechas (IdRonda, idArquero, puntuacion)
        VALUES (v_idRonda, p_idArquero, v_score);
    END LOOP;

    RAISE NOTICE 'Ronda % registrada para arquero % en torneo %.', p_numeroRonda, p_idArquero, p_idTorneo;
END;
$$;

-- ============================================================
-- 2. STORED PROCEDURE 2: Calcular Ranking del Torneo
-- ============================================================
CREATE OR REPLACE PROCEDURE calcular_ranking_torneo(
    p_idTorneo BIGINT
)
LANGUAGE plpgsql
AS $$
DECLARE
    v_is_active BOOLEAN;
    rec RECORD;
    v_posicion INT := 1;
BEGIN
    -- 1. Validar torneo
    SELECT Activo INTO v_is_active FROM torneos WHERE idTorneo = p_idTorneo;
    IF NOT FOUND THEN
        RAISE EXCEPTION 'Torneo con id % no existe.', p_idTorneo;
    END IF;

    IF v_is_active THEN
        RAISE WARNING 'El torneo % sigue activo. Cálculo parcial.', p_idTorneo;
    END IF;

    -- 2. Limpiar ranking previo
    DELETE FROM rankings WHERE idTorneo = p_idTorneo;

    -- 3. Calcular suma total y reinsertar
    FOR rec IN
        SELECT 
            f.idArquero, 
            SUM(f.puntuacion) AS puntajeTotal
        FROM rondas r
        JOIN flechas f ON f.IdRonda = r.IdRonda
        WHERE r.idTorneo = p_idTorneo
        GROUP BY f.idArquero
        ORDER BY puntajeTotal DESC
    LOOP
        INSERT INTO rankings (idTorneo, idArquero, posicion, puntajeFinal)
        VALUES (p_idTorneo, rec.idArquero, v_posicion, rec.puntajeTotal);
        
        v_posicion := v_posicion + 1;
    END LOOP;
END;
$$;

-- ============================================================
-- 3. TRIGGER 1: Validar puntuación [0, 10]
-- ============================================================
CREATE OR REPLACE FUNCTION fn_validate_arrow_score()
RETURNS TRIGGER
LANGUAGE plpgsql
AS $$
BEGIN
    IF NEW.puntuacion < 0 OR NEW.puntuacion > 10 THEN
        RAISE EXCEPTION 'Puntuación inválida: %. Debe estar entre 0 y 10.', NEW.puntuacion;
    END IF;
    RETURN NEW;
END;
$$;

DROP TRIGGER IF EXISTS trg_validate_arrow_score ON flechas;
CREATE TRIGGER trg_validate_arrow_score
BEFORE INSERT OR UPDATE ON flechas
FOR EACH ROW
EXECUTE FUNCTION fn_validate_arrow_score();

-- ============================================================
-- 4. TRIGGER 2: Auditoría de Modificación de Puntajes
-- ============================================================
CREATE OR REPLACE FUNCTION fn_audit_score_modification()
RETURNS TRIGGER
LANGUAGE plpgsql
AS $$
DECLARE
    v_idInscripcion BIGINT;
    v_modified_by BIGINT;
    v_user_setting TEXT;
BEGIN
    -- Solo auditar si hubo un cambio real
    IF OLD.puntuacion = NEW.puntuacion THEN
        RETURN NEW;
    END IF;

    -- Obtener el ID del usuario desde la variable de sesión
    v_user_setting := current_setting('app.current_user_id', true);
    IF v_user_setting IS NULL OR v_user_setting = '' THEN
        RAISE EXCEPTION 'Auditoría fallida: falta app.current_user_id en la sesión.';
    END IF;
    v_modified_by := v_user_setting::BIGINT;

    -- Obtener la inscripción cruzando la ronda de la flecha modificada
    SELECT i.idInscripcion INTO v_idInscripcion
    FROM rondas r
    JOIN inscripciones i ON r.idTorneo = i.idTorneo AND i.idArquero = NEW.idArquero
    WHERE r.IdRonda = NEW.IdRonda;

    -- Insertar en tabla de auditoría según UML
    INSERT INTO auditorias (
        idUsuario, 
        idInscripcion, 
        puntajeAnterior, 
        puntajeNuevo, 
        fechaModificacion
    ) VALUES (
        v_modified_by, 
        v_idInscripcion, 
        OLD.puntuacion, 
        NEW.puntuacion, 
        NOW()
    );

    RETURN NEW;
END;
$$;

DROP TRIGGER IF EXISTS trg_audit_score_modification ON flechas;
CREATE TRIGGER trg_audit_score_modification
AFTER UPDATE ON flechas
FOR EACH ROW
EXECUTE FUNCTION fn_audit_score_modification();

-- ============================================================
-- 5. VISTA MATERIALIZADA: Leaderboard Histórico
-- ============================================================
CREATE MATERIALIZED VIEW IF NOT EXISTS mv_leaderboard_historico AS
SELECT
    ROW_NUMBER() OVER (ORDER BY ROUND(AVG(f.puntuacion)::NUMERIC, 4) DESC) AS posicion_global,
    ar.idArquero,
    ar.nombre,
    COUNT(DISTINCT r.idTorneo) AS torneos_jugados,
    COUNT(f.IdFlecha) AS flechas_lanzadas,
    SUM(f.puntuacion) AS puntaje_total,
    ROUND(AVG(f.puntuacion)::NUMERIC, 4) AS promedio_por_flecha
FROM arqueros ar
JOIN flechas f ON f.idArquero = ar.idArquero
JOIN rondas r ON r.IdRonda = f.IdRonda
GROUP BY ar.idArquero, ar.nombre
ORDER BY promedio_por_flecha DESC
LIMIT 50
WITH DATA;

CREATE UNIQUE INDEX IF NOT EXISTS uidx_mv_leaderboard_arquero 
    ON mv_leaderboard_historico (idArquero);

-- ============================================================
-- 6. FUNCIÓN DE REFRESCO DIARIO
-- ============================================================
CREATE OR REPLACE FUNCTION refresh_historical_leaderboard()
RETURNS VOID
LANGUAGE plpgsql
AS $$
BEGIN
    REFRESH MATERIALIZED VIEW CONCURRENTLY mv_leaderboard_historico;
    RAISE NOTICE 'mv_leaderboard_historico refrescada a las %', NOW();
END;
$$;