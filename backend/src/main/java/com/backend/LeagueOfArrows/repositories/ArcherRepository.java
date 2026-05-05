package com.backend.LeagueOfArrows.repositories;

import com.backend.LeagueOfArrows.dtos.LeaderboardDTO;
import com.backend.LeagueOfArrows.dtos.TopArcherDTO;
import com.backend.LeagueOfArrows.entities.ArcherEntity;
import com.backend.LeagueOfArrows.dtos.HistoryDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class ArcherRepository {

    private final JdbcTemplate jdbc;

    private final RowMapper<ArcherEntity> archerRowMapper = (rs, n) -> {
        ArcherEntity archer = new ArcherEntity();
        archer.setArcherId(rs.getLong("id_archer"));
        archer.setUserId(rs.getLong("id_user"));
        archer.setName(rs.getString("name"));
        archer.setCategoryId(rs.getObject("id_category", Long.class));
        // enriched fields (may be absent in single-row queries)
        try { archer.setCategoryName(rs.getString("category_name")); } catch (Exception ignored) {}
        try { archer.setEmail(rs.getString("email")); }              catch (Exception ignored) {}
        return archer;
    };

    public List<ArcherEntity> findAll() {
        String sql = """
            SELECT a.*, c.name AS category_name, u.email
            FROM archers a
            LEFT JOIN categories c ON c.id_category = a.id_category
            LEFT JOIN users      u ON u.id_user      = a.id_user
            ORDER BY a.id_archer
            """;
        return jdbc.query(sql, archerRowMapper);
    }


    public Optional<ArcherEntity> findById(Long id) {
        var list = jdbc.query("SELECT * FROM archers WHERE id_archer = ?", archerRowMapper, id);
        return list.stream().findFirst();
    }

    public Optional<ArcherEntity> findByUserId(Long userId) {
        var list = jdbc.query("SELECT * FROM archers WHERE id_user = ?", archerRowMapper, userId);
        return list.stream().findFirst();
    }


    public Long save(Long userId, String name, Long categoryId) {
        return jdbc.queryForObject("INSERT INTO archers (id_user, name, id_category) VALUES (?, ?, ?) RETURNING id_archer", Long.class, userId, name, categoryId);
    }


    public int update(Long id, String name, Long categoryId) {
        return jdbc.update(
                "UPDATE archers  SET name = ?, id_category = ? WHERE id_archer = ?",
                 name, categoryId, id);
    }

    public int deleteById(Long id) {
        return jdbc.update("DELETE FROM archers WHERE id_archer = ?", id);
    }

    public List<HistoryDTO> findHistoryByArcherId(Long archerId) {
        String sql = """
        SELECT
            t.id_tournament,
            t.name,
            c.name          AS category,
            t.start_date,
            t.end_date,
            t.is_active,
            i.score,
            r.position
        FROM inscriptions i
        INNER JOIN tournaments t  ON i.id_tournament = t.id_tournament
        INNER JOIN categories c   ON t.id_category   = c.id_category
        LEFT JOIN rankings r      ON r.id_tournament = i.id_tournament
                                 AND r.id_archer     = i.id_archer
        WHERE i.id_archer = ?
        ORDER BY t.start_date DESC
        """;
        return jdbc.query(sql, (rs, n) -> new HistoryDTO(
                rs.getLong("id_tournament"),
                rs.getString("name"),
                rs.getString("category"),
                rs.getDate("start_date").toLocalDate(),
                rs.getDate("end_date").toLocalDate(),
                rs.getBoolean("is_active"),
                rs.getInt("score"),
                rs.getObject("position") != null ? rs.getInt("position") : null
        ), archerId);
    }

    public List<TopArcherDTO> findTopArchersLastMonth() {
        String sql = """
        SELECT
            a.id_archer,
            a.name,
            SUM(i.score) AS monthly_score
        FROM inscriptions i
        INNER JOIN archers a      ON i.id_archer     = a.id_archer
        INNER JOIN tournaments t  ON i.id_tournament = t.id_tournament
        WHERE t.start_date >= NOW() - INTERVAL '1 month'
        GROUP BY a.id_archer, a.name
        ORDER BY monthly_score DESC
        """;
        return jdbc.query(sql, (rs, n) -> new TopArcherDTO(
                rs.getLong("id_archer"),
                rs.getString("name"),
                rs.getInt("monthly_score")
        ));
    }

    public List<LeaderboardDTO> findHistoricalLeaderboard() {
        String sql = """
        SELECT
            posicion_global,
            id_archer,
            nombre,
            torneos_jugados,
            flechas_lanzadas,
            puntaje_total,
            promedio_por_flecha
        FROM mv_leaderboard_historico
        ORDER BY posicion_global ASC
        """;
        return jdbc.query(sql, (rs, n) -> new LeaderboardDTO(
                rs.getInt("posicion_global"),
                rs.getLong("id_archer"),
                rs.getString("nombre"),
                rs.getInt("torneos_jugados"),
                rs.getInt("flechas_lanzadas"),
                rs.getInt("puntaje_total"),
                rs.getBigDecimal("promedio_por_flecha")
        ));
    }

}