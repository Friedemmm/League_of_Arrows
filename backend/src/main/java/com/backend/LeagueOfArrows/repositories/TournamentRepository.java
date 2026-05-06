package com.backend.LeagueOfArrows.repositories;


import com.backend.LeagueOfArrows.dtos.PodiumDTO;
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
        tournament.setTournamentId(rs.getLong("id_tournament"));
        tournament.setName(rs.getString("name"));
        tournament.setCategoryId(rs.getLong("id_category"));
        tournament.setStartDate(rs.getDate("start_date").toLocalDate());
        tournament.setEndDate(rs.getDate("end_date").toLocalDate());
        tournament.setActive(rs.getBoolean("is_active"));
        try { tournament.setCategoryName(rs.getString("category_name")); } catch (Exception ignored) {}
        return tournament;
    };

    public List<TournamentEntity> findAll() {
        String sql = """
            SELECT t.*, c.name AS category_name
            FROM tournaments t
            LEFT JOIN categories c ON c.id_category = t.id_category
            ORDER BY t.start_date DESC
            """;
        return jdbc.query(sql, tournamentEntityRowMapper);
    }

    public Optional<TournamentEntity> findById(Long id) {
        var list = jdbc.query("SELECT * FROM tournaments WHERE id_tournament = ?", tournamentEntityRowMapper, id);
        return list.stream().findFirst();
    }

    public Long save(TournamentEntity tournament){
        return jdbc.queryForObject("INSERT INTO tournaments (name, id_category, start_date, end_date, is_active) VALUES (?, ?, ?, ?, ?) RETURNING id_tournament", Long.class, tournament.getName(), tournament.getCategoryId(), tournament.getStartDate(), tournament.getEndDate(), tournament.getActive());
    }

    public int update(TournamentEntity tournament){
        return jdbc.update("UPDATE tournaments SET name = ?, id_category = ?, start_date = ?, end_date = ?, is_active = ? WHERE id_tournament = ?", tournament.getName(), tournament.getCategoryId(), tournament.getStartDate(), tournament.getEndDate(), tournament.getActive(), tournament.getTournamentId());
    }

    public int deleteById(Long id){
        return jdbc.update("DELETE FROM tournaments WHERE id_tournament = ?", id);
    }

    public List<PodiumDTO> findPodiumByTournamentId(Long tournamentId) {
        // 1st: use pre-calculated rankings (populated for finished tournaments)
        // 2nd: fallback to live calculation from arrows for active tournaments
        String sql = """
        WITH ranked AS (
            -- Pre-calculated rankings (finished tournaments)
            SELECT
                r.position,
                r.id_archer,
                a.name,
                r.total_score
            FROM rankings r
            INNER JOIN archers a ON r.id_archer = a.id_archer
            WHERE r.id_tournament = ?

            UNION ALL

            -- Live calculation from arrows (active / no rankings yet)
            SELECT
                CAST(ROW_NUMBER() OVER (ORDER BY SUM(ar.score) DESC) AS INT) AS position,
                ro.id_archer,
                a.name,
                SUM(ar.score) AS total_score
            FROM rounds ro
            INNER JOIN arrows ar ON ar.id_round = ro.id_round
            INNER JOIN archers a ON a.id_archer = ro.id_archer
            WHERE ro.id_tournament = ?
              AND NOT EXISTS (
                  SELECT 1 FROM rankings r2 WHERE r2.id_tournament = ?
              )
            GROUP BY ro.id_archer, a.name
        )
        SELECT DISTINCT ON (position) position, id_archer, name, total_score
        FROM ranked
        WHERE position <= 3
        ORDER BY position ASC
        """;
        return jdbc.query(sql, (rs, n) -> new PodiumDTO(
                rs.getInt("position"),
                rs.getLong("id_archer"),
                rs.getString("name"),
                rs.getInt("total_score")
        ), tournamentId, tournamentId, tournamentId);
    }
}
