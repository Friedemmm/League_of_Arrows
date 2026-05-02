package com.backend.LeagueOfArrows.repositories;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import com.backend.LeagueOfArrows.entities.TournamentEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class TournamentRepository {

    private final JdbcTemplate jdbc;

    private final RowMapper<TournamentEntity> tournamentEntityRowMapper = (rs, rowNum) -> {
        TournamentEntity tournament = new TournamentEntity();
        tournament.setTournamentId(rs.getLong("id_torneo"));
        tournament.setCategoryId(rs.getLong("id_categoria"));
        tournament.setStartDate(rs.getDate("fecha_inicio").toLocalDate());
        tournament.setEndDate(rs.getDate("fecha_fin").toLocalDate());
        tournament.setActive(rs.getBoolean("activo"));
        return tournament;
    };

    public List<TournamentEntity> findAll() {
        return jdbc.query("SELECT * FROM torneo", tournamentEntityRowMapper);
    }

    public Optional<TournamentEntity> findById(Long id) {
        var list = jdbc.query("SELECT * FROM torneo WHERE id_torneo = ?", tournamentEntityRowMapper, id);
        return list.stream().findFirst();
    }

    public Long save(TournamentEntity tournament){
        return jdbc.queryForObject("INSERT INTO torneo (id_categoria, fecha_inicio, fecha_fin, activo) VALUES (?, ?, ?, ?) RETURNING id_torneo", Long.class, tournament.getCategoryId(), tournament.getStartDate(), tournament.getEndDate(), tournament.getActive());
    }

    public int update(TournamentEntity tournament){
        return jdbc.update("UPDATE torneo SET id_categoria = ?, fecha_inicio = ?, fecha_fin = ?, activo = ? WHERE id_torneo = ?", tournament.getCategoryId(), tournament.getStartDate(), tournament.getEndDate(), tournament.getActive(), tournament.getTournamentId());
    }

    public int deleteById(Long id){
        return jdbc.update("DELETE FROM torneo WHERE id_torneo = ?", id);
    }
}
