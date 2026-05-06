package com.backend.LeagueOfArrows.repositories;

import com.backend.LeagueOfArrows.dtos.RoundDetailDTO;
import com.backend.LeagueOfArrows.dtos.RoundRequestDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Repository
@RequiredArgsConstructor
public class RoundRepository {

    private final JdbcTemplate jdbc;

    // ── Register full round via Stored Procedure ─────────────────────
    @Transactional
    public void registerRound(RoundRequestDTO dto, Long adminUserId) {
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

    // ── Get rounds with arrows for a given archer+tournament ─────────
    public List<RoundDetailDTO> getRoundsByTournamentAndArcher(Long tournamentId, Long archerId) {
        String roundSql = """
            SELECT id_round, round_number
            FROM rounds
            WHERE id_tournament = ? AND id_archer = ?
            ORDER BY round_number
        """;

        String arrowSql = """
            SELECT id_arrow, arrow_number, score
            FROM arrows
            WHERE id_round = ?
            ORDER BY arrow_number
        """;

        List<RoundDetailDTO> rounds = jdbc.query(roundSql, (rs, rn) -> {
            RoundDetailDTO r = new RoundDetailDTO();
            r.setRoundId(rs.getLong("id_round"));
            r.setRoundNumber(rs.getInt("round_number"));
            r.setArrows(new ArrayList<>());
            return r;
        }, tournamentId, archerId);

        for (RoundDetailDTO round : rounds) {
            List<RoundDetailDTO.ArrowDTO> arrows = jdbc.query(arrowSql, (rs, rn) -> {
                RoundDetailDTO.ArrowDTO a = new RoundDetailDTO.ArrowDTO();
                a.setArrowId(rs.getLong("id_arrow"));
                a.setArrowNumber(rs.getInt("arrow_number"));
                a.setScore(rs.getInt("score"));
                return a;
            }, round.getRoundId());
            round.setArrows(arrows);
        }

        return rounds;
    }

    // ── Update a single arrow — triggers audit log automatically ─────
    @Transactional
    public void updateArrowScore(Long roundId, Long arrowId, Integer newScore, Long adminUserId) {
        if (adminUserId != null) {
            jdbc.execute("SET LOCAL app.current_user_id = " + adminUserId);
        }
        jdbc.update("""
            UPDATE arrows
            SET score = ?
            WHERE id_arrow = ? AND id_round = ?
        """, newScore, arrowId, roundId);
    }
}
