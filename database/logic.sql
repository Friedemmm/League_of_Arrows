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
    v_arrow_index INT := 1;
BEGIN
    -- 1. Validate that tournament exists and is active
    SELECT is_active INTO v_is_active FROM tournaments WHERE id_tournament = p_tournament_id;
    IF NOT FOUND THEN
        RAISE EXCEPTION 'Torneo con id % no existe.', p_tournament_id;
    END IF;

    IF NOT v_is_active THEN
        RAISE EXCEPTION 'El torneo % no está activo.', p_tournament_id;
    END IF;

    -- 2. Validate that the archer exists
    IF NOT EXISTS (SELECT 1 FROM archers WHERE id_archer = p_archer_id) THEN
        RAISE EXCEPTION 'Arquero con id % no existe.', p_archer_id;
    END IF;

    -- 3. Check if the round already exists for the tournament, archer and round number; if not, create
    SELECT id_round INTO v_round_id 
    FROM rounds 
    WHERE id_tournament = p_tournament_id 
      AND id_archer = p_archer_id 
      AND round_number = p_round_number 
    LIMIT 1;
    
    IF NOT FOUND THEN
        INSERT INTO rounds (id_tournament, id_archer, round_number)
        VALUES (p_tournament_id, p_archer_id, p_round_number)
        RETURNING id_round INTO v_round_id;
    END IF;

    -- 4. Validate that no arrows have been registered for this round yet
    IF EXISTS (SELECT 1 FROM arrows WHERE id_round = v_round_id) THEN
        RAISE EXCEPTION 'Las flechas de la ronda % para el arquero % ya fueron registradas.', p_round_number, p_archer_id;
    END IF;

    -- 5. Insert each arrow
    FOREACH v_score IN ARRAY p_scores
    LOOP
        INSERT INTO arrows (id_round, arrow_number, score)
        VALUES (v_round_id, v_arrow_index, v_score);
        v_arrow_index := v_arrow_index + 1;
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
    v_position INT := 1;
BEGIN
    -- 1. Validate Tournament
    SELECT is_active INTO v_is_active FROM tournaments WHERE id_tournament = p_tournament_id;
    IF NOT FOUND THEN
        RAISE EXCEPTION 'Torneo con id % no existe.', p_tournament_id;
    END IF;

    IF v_is_active THEN
        RAISE WARNING 'El torneo % sigue activo. Cálculo parcial.', p_tournament_id;
    END IF;

    -- 2. Clear previous ranking
    DELETE FROM rankings WHERE id_tournament = p_tournament_id;

    -- 3. Calculate total sum and re-insert
    FOR rec IN
        SELECT 
            r.id_archer, 
            SUM(a.score) AS total_score
        FROM rounds r
        JOIN arrows a ON a.id_round = r.id_round
        WHERE r.id_tournament = p_tournament_id
        GROUP BY r.id_archer
        ORDER BY total_score DESC
    LOOP
        INSERT INTO rankings (id_tournament, id_archer, position, total_score, updated_at)
        VALUES (p_tournament_id, rec.id_archer, v_position, rec.total_score, CURRENT_TIMESTAMP);
        
        v_position := v_position + 1;
    END LOOP;
END;
$$;

-- ============================================================
-- 3. TRIGGER 1: Validate score [0, 10]
-- ============================================================
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
    v_round_record RECORD;
    v_modified_by BIGINT;
    v_user_setting TEXT;
BEGIN
    -- Skip audit if score didn't change (UPDATE with same value)
    IF TG_OP = 'UPDATE' AND OLD.score = NEW.score THEN
        RETURN NEW;
    END IF;

    -- Get the User ID from the session variable (set by Spring before the CALL)
    v_user_setting := current_setting('app.current_user_id', true);
    IF v_user_setting IS NOT NULL AND v_user_setting <> '' THEN
        v_modified_by := v_user_setting::BIGINT;
    ELSE
        v_modified_by := NULL;  -- log without admin ID rather than crashing
    END IF;

    -- Get tournament and archer from the round associated with the arrow
    SELECT id_tournament, id_archer INTO v_round_record
    FROM rounds
    WHERE id_round = NEW.id_round;

    -- Insert into audit_log table
    -- For INSERT: old_score = 0 (arrow is new)
    -- For UPDATE: old_score = previous value
    INSERT INTO audit_log (id_archer, id_tournament, old_score, new_score, modified_by, modified_at)
    VALUES (
        v_round_record.id_archer,
        v_round_record.id_tournament,
        CASE WHEN TG_OP = 'INSERT' THEN 0 ELSE OLD.score END,
        NEW.score,
        v_modified_by,
        CURRENT_TIMESTAMP
    );

    RETURN NEW;
END;
$$;

DROP TRIGGER IF EXISTS trg_audit_score_modification ON arrows;
CREATE TRIGGER trg_audit_score_modification
AFTER INSERT OR UPDATE ON arrows
FOR EACH ROW
EXECUTE FUNCTION fn_audit_score_modification();

-- ============================================================
-- 5. MATERIALIZED VIEW: Historical Leaderboard
-- ============================================================
CREATE MATERIALIZED VIEW IF NOT EXISTS mv_leaderboard_historico AS
SELECT
    ROW_NUMBER() OVER (ORDER BY ROUND(AVG(a.score)::NUMERIC, 4) DESC) AS posicion_global,
    ar.id_archer,
    ar.name AS nombre,
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
