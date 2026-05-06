package com.backend.LeagueOfArrows.repositories;

import com.backend.LeagueOfArrows.entities.InscriptionEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


@Repository
@RequiredArgsConstructor
public class InscriptionRepository {

    private final JdbcTemplate jdbc;

    private final RowMapper<InscriptionEntity> inscriptionEntityRowMapper = (rs, rowNum) -> {
        InscriptionEntity inscription = new InscriptionEntity();
        inscription.setInscriptionId(rs.getLong("id_inscription"));
        inscription.setArcherId(rs.getLong("id_archer"));
        inscription.setTournamentId(rs.getLong("id_tournament"));
        inscription.setScore(rs.getInt("score"));
        return inscription;
    };

    public List<InscriptionEntity> findAll() {
        return jdbc.query("SELECT * FROM inscriptions", inscriptionEntityRowMapper);
    }

    public List<InscriptionEntity> findByArcherId(Long archerId) {
        return jdbc.query("SELECT * FROM inscriptions WHERE id_archer = ?", inscriptionEntityRowMapper, archerId);
    }

    public Optional<InscriptionEntity> findById(Long id) {
        var list = jdbc.query("SELECT * FROM inscriptions WHERE id_inscription = ?", inscriptionEntityRowMapper, id);
        return list.stream().findFirst();
    }

    public Long save(Long archerId, Long tournamentId) {
        return jdbc.queryForObject("INSERT INTO inscriptions (id_archer, id_tournament, score) VALUES (?, ?, 0) RETURNING id_inscription", Long.class, archerId, tournamentId);
    }

    public int deteleById(Long id) {
        return jdbc.update("DELETE FROM inscriptions WHERE id_inscription = ?", id);
    }

}
