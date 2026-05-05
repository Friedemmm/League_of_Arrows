CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- ===========================================================
-- PRE-REQUISITES & CLEANUP
-- Drop procedures and functions to avoid parameter name conflicts
-- ===========================================================

DROP PROCEDURE IF EXISTS calcular_ranking_torneo(bigint);
DROP PROCEDURE IF EXISTS registrar_puntuacion_ronda(bigint, bigint, integer, integer[]);

-- ============================================================
-- 1. STORED PROCEDURE 1: Register Round Score
-- ============================================================

CREATE OR REPLACE PROCEDURE registrar_puntuacion_ronda(
    p_tournament_id BIGINT,
    p_archer_id BIGINT,
    p_round_number INT,
    p_scores INTEGER[]
)
LANGUAGE plpgsql
AS $$
DECLARE
    v_is_active BOOLEAN;
    v_round_id BIGINT;
    v_score INTEGER;
    v_arrow_count INT := 1;
BEGIN
    -- 1. Validar que el torneo exista y esté activo
    SELECT is_active INTO v_is_active FROM tournaments WHERE id_tournament = p_tournament_id;
    IF NOT FOUND THEN
        RAISE EXCEPTION 'Torneo con id % no existe.', p_tournament_id;
    END IF;

    IF NOT v_is_active THEN
        RAISE EXCEPTION 'El torneo % no está activo.', p_tournament_id;
    END IF;

    -- 2. Validar que el arquero exista
    IF NOT EXISTS (SELECT 1 FROM archers WHERE id_archer = p_archer_id) THEN
        RAISE EXCEPTION 'Arquero con id % no existe.', p_archer_id;
    END IF;

    -- 3. Verificar si la ronda ya existe para ese arquero en ese torneo
    SELECT id_round INTO v_round_id FROM rounds 
    WHERE id_tournament = p_tournament_id AND id_archer = p_archer_id AND round_number = p_round_number 
    LIMIT 1;
    
    IF NOT FOUND THEN
        INSERT INTO rounds (id_tournament, id_archer, round_number)
        VALUES (p_tournament_id, p_archer_id, p_round_number)
        RETURNING id_round INTO v_round_id;
    END IF;

    -- 4. Validar que no haya flechas registradas para esta ronda
    IF EXISTS (SELECT 1 FROM arrows WHERE id_round = v_round_id) THEN
        RAISE EXCEPTION 'Las flechas de la ronda % para el arquero % ya fueron registradas.', p_round_number, p_archer_id;
    END IF;

    -- 5. Insertar cada flecha
    FOREACH v_score IN ARRAY p_scores
    LOOP
        INSERT INTO arrows (id_round, arrow_number, score)
        VALUES (v_round_id, v_arrow_count, v_score);
        v_arrow_count := v_arrow_count + 1;
    END LOOP;

    RAISE NOTICE 'Ronda % registrada para arquero % en torneo %.', p_round_number, p_archer_id, p_tournament_id;
END;
$$;

-- ============================================================
-- 2. STORED PROCEDURE 2: Calculate Tournament Ranking
-- ============================================================
CREATE OR REPLACE PROCEDURE calcular_ranking_torneo(
    p_tournament_id BIGINT
)
LANGUAGE plpgsql
AS $$
DECLARE
    v_is_active BOOLEAN;
    rec RECORD;
    v_posicion INT := 1;
BEGIN
    -- 1. Validar Torneo
    SELECT is_active INTO v_is_active FROM tournaments WHERE id_tournament = p_tournament_id;
    IF NOT FOUND THEN
        RAISE EXCEPTION 'Torneo con id % no existe.', p_tournament_id;
    END IF;

    IF v_is_active THEN
        RAISE WARNING 'El torneo % sigue activo. Cálculo parcial.', p_tournament_id;
    END IF;

    -- 2. Limpiar ranking previo
    DELETE FROM rankings WHERE id_tournament = p_tournament_id;

    -- 3. Calcular suma total e insertar (agrupando por las rondas del torneo)
    FOR rec IN
        SELECT 
            r.id_archer, 
            SUM(a.score) AS puntajeTotal
        FROM rounds r
        JOIN arrows a ON a.id_round = r.id_round
        WHERE r.id_tournament = p_tournament_id
        GROUP BY r.id_archer
        ORDER BY puntajeTotal DESC
    LOOP
        INSERT INTO rankings (id_tournament, id_archer, position, total_score)
        VALUES (p_tournament_id, rec.id_archer, v_posicion, rec.puntajeTotal);
        
        v_posicion := v_posicion + 1;
    END LOOP;
END;
$$;

-- ============================================================
-- 3. TRIGGER 1: Validate score [0, 10]
-- ============================================================
-- Nota: Aunque la tabla 'arrows' ya tiene un CHECK, el trigger refuerza la lógica.
CREATE OR REPLACE FUNCTION fn_validate_arrow_score()
RETURNS TRIGGER
LANGUAGE plpgsql
AS $$
BEGIN
    IF NEW.score < 0 OR NEW.score > 10 THEN
        RAISE EXCEPTION 'Puntuación inválida: %. Debe estar entre 0 y 10.', NEW.score;
    END IF;
    RETURN NEW;
END;
$$;

DROP TRIGGER IF EXISTS trg_validate_arrow_score ON arrows;
CREATE TRIGGER trg_validate_arrow_score
BEFORE INSERT OR UPDATE ON arrows
FOR EACH ROW
EXECUTE FUNCTION fn_validate_arrow_score();

-- ============================================================
-- 4. TRIGGER 2: Score Modification Audit
-- ============================================================
CREATE OR REPLACE FUNCTION fn_audit_score_modification()
RETURNS TRIGGER
LANGUAGE plpgsql
AS $$
DECLARE
    v_id_archer BIGINT;
    v_id_tournament BIGINT;
    v_modified_by BIGINT;
    v_user_setting TEXT;
BEGIN
    -- Solo auditar si hubo un cambio real
    IF OLD.score = NEW.score THEN
        RETURN NEW;
    END IF;

    -- Obtener el ID de usuario de la sesión
    v_user_setting := current_setting('app.current_user_id', true);
    IF v_user_setting IS NULL OR v_user_setting = '' THEN
        RAISE EXCEPTION 'Auditoría fallida: falta app.current_user_id en la sesión.';
    END IF;
    v_modified_by := v_user_setting::BIGINT;

    -- Obtener datos de la ronda para la auditoría
    SELECT id_archer, id_tournament INTO v_id_archer, v_id_tournament
    FROM rounds 
    WHERE id_round = NEW.id_round;

    -- Insertar en audit_log según esquema
    INSERT INTO audit_log (
        id_archer, 
        id_tournament, 
        old_score, 
        new_score, 
        modified_by
    ) VALUES (
        v_id_archer, 
        v_id_tournament, 
        OLD.score, 
        NEW.score, 
        v_modified_by
    );

    RETURN NEW;
END;
$$;

DROP TRIGGER IF EXISTS trg_audit_score_modification ON arrows;
CREATE TRIGGER trg_audit_score_modification
AFTER UPDATE ON arrows
FOR EACH ROW
EXECUTE FUNCTION fn_audit_score_modification();

-- ============================================================
-- 5. MATERIALIZED VIEW: Historical Leaderboard
-- ============================================================
CREATE MATERIALIZED VIEW IF NOT EXISTS mv_leaderboard_historico AS
SELECT
    ROW_NUMBER() OVER (ORDER BY AVG(a.score) DESC) AS posicion_global,
    ar.id_archer,
    ar.name,
    COUNT(DISTINCT r.id_tournament) AS torneos_jugados,
    COUNT(a.id_arrow) AS flechas_lanzadas,
    SUM(a.score) AS puntaje_total,
    ROUND(AVG(a.score)::NUMERIC, 4) AS promedio_por_flecha
FROM archers ar
JOIN rounds r ON r.id_archer = ar.id_archer
JOIN arrows a ON a.id_round = r.id_round
GROUP BY ar.id_archer, ar.name
ORDER BY promedio_por_flecha DESC
LIMIT 50
WITH DATA;

CREATE UNIQUE INDEX IF NOT EXISTS uidx_mv_leaderboard_arquero 
    ON mv_leaderboard_historico (id_archer);

-- ============================================================
-- 6. DAILY REFRESH FUNCTION
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
