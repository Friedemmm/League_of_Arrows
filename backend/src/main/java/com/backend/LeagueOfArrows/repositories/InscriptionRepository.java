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
        inscription.setInscriptionId(rs.getLong("id_inscripcion"));
        inscription.setTournamentId(rs.getLong("id_torneo"));
        inscription.setArcherId(rs.getLong("id_arquero"));
        inscription.setScore(rs.getInt("puntaje_total"));
        return inscription;
    };

    public List<InscriptionEntity> findAll() {
        return jdbc.query("SELECT * FROM inscripcion", inscriptionEntityRowMapper);
    }

    public List<InscriptionEntity> findByArcherId(Long archerId) {
        return jdbc.query("SELECT * FROM inscripcion WHERE id_arquero = ?", inscriptionEntityRowMapper, archerId);
    }

    public Optional<InscriptionEntity> findById(Long id) {
        var list = jdbc.query("SELECT * FROM inscripcion WHERE id_inscripcion = ?", inscriptionEntityRowMapper, id);
        return list.stream().findFirst();
    }

    public Long save(Long archerId, Long tournamentId) {
        return jdbc.queryForObject("INSERT INTO inscripcion (id_arquero, id_torneo, puntaje_total) VALUES (?, ?, 0) RETURNING id_inscripcion", Long.class, archerId, tournamentId);
    }

    public int deteleById(Long id) {
        return jdbc.update("DELETE FROM inscripcion WHERE id_inscripcion = ?", id);
    }

}
