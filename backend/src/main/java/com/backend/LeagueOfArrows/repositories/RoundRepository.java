package com.backend.LeagueOfArrows.repositories;

import com.backend.LeagueOfArrows.dtos.RoundRequestDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class RoundRepository {

    private final JdbcTemplate jdbc;

    public void registerRound(RoundRequestDTO dto) {
        jdbc.update("CALL registrar_puntuacion_ronda(?, ?, ?, ?::integer[])",
                dto.getTournamentId(),
                dto.getArcherId(),
                dto.getRoundNumber(),
                "{" + dto.getScores().toString().replace("[", "").replace("]", "") + "}"
        );
    }
}