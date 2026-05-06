package com.backend.LeagueOfArrows.repositories;

import com.backend.LeagueOfArrows.dtos.RoundRequestDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@RequiredArgsConstructor
public class RoundRepository {

    private final JdbcTemplate jdbc;

    /**
     * Registers a round's arrow scores.
     * Sets app.current_user_id in the same DB session so the audit trigger
     * can record which admin performed the action.
     */
    @Transactional
    public void registerRound(RoundRequestDTO dto, Long adminUserId) {
        // Set the session variable BEFORE calling the stored procedure.
        // The audit trigger reads this to know who modified the score.
        if (adminUserId != null) {
            jdbc.execute("SET LOCAL app.current_user_id = " + adminUserId);
        }

        jdbc.update("CALL registrar_puntuacion_ronda(?, ?, ?, ?::integer[])",
                dto.getTournamentId(),
                dto.getArcherId(),
                dto.getRoundNumber(),
                "{" + dto.getScores().toString().replace("[", "").replace("]", "") + "}"
        );
    }
}