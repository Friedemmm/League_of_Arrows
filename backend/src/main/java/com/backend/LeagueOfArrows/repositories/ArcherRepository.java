package com.backend.LeagueOfArrows.repositories;

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
        archer.setArcherId(rs.getLong("id_arquero"));
        archer.setUserId(rs.getLong("id_usuario"));
        archer.setName(rs.getString("nombre"));
        return archer;
    };

    public List<ArcherEntity> findAll() {
        return jdbc.query(
                "SELECT * FROM arquero", archerRowMapper);
    }


    public Optional<ArcherEntity> findById(Long id) {
        var list = jdbc.query("SELECT * FROM arquero WHERE id_arquero = ?", archerRowMapper, id);
        return list.stream().findFirst();
    }

    public Optional<ArcherEntity> findByUserId(Long userId) {
        var list = jdbc.query("SELECT * FROM arquero WHERE id_usuario = ?", archerRowMapper, userId);
        return list.stream().findFirst();
    }


    public Long save(Long userId, String name) {
        return jdbc.queryForObject("INSERT INTO arquero (id_usuario, nombre) VALUES (?, ?) RETURNING id_arquero", Long.class, userId, name);
    }


    public int update(Long id, String name) {
        return jdbc.update(
                "UPDATE arquero SET nombre = ? WHERE id_arquero = ?",
                 name,id);
    }

    public int deleteById(Long id) {
        return jdbc.update("DELETE FROM arquero WHERE id_arquero = ?", id);
    }

    public List<HistoryDTO> findHistoryByArcherId(Long archerId) {
        String sql =
            """
            SELECT
                t.id_torneo,
                t.nombre,
                c.nombre      AS categoria,
                t.fecha_inicio,
                t.fecha_fin,
                t.activo,
                i.puntaje_total,
                r.posicion
            FROM inscripcion i
            INNER JOIN torneo t     ON i.id_torneo    = t.id_torneo
            INNER JOIN categoria c  ON t.id_categoria = c.id_categoria
            LEFT JOIN ranking r     ON r.id_torneo    = i.id_torneo
                                   AND r.id_arquero   = i.id_arquero
            WHERE i.id_arquero = ?
            ORDER BY t.fecha_inicio DESC
            """;

        return jdbc.query(sql, (rs, n) -> new HistoryDTO(
                rs.getLong("id_torneo"),
                rs.getString("nombre"),
                rs.getString("categoria"),
                rs.getDate("fecha_inicio").toLocalDate(),
                rs.getDate("fecha_fin").toLocalDate(),
                rs.getBoolean("activo"),
                rs.getInt("puntaje_total"),
                rs.getObject("posicion") != null ? rs.getInt("posicion") : null
        ), archerId);
    }

    public List<TopArcherDTO> findTopArchersLastMonth() {
        String sql =
            """
            SELECT
                a.id_arquero,
                a.nombre,
                SUM(i.puntaje_total) AS puntaje_mes
            FROM inscripcion i
            INNER JOIN arquero a  ON i.id_arquero = a.id_arquero
            INNER JOIN torneo t   ON i.id_torneo  = t.id_torneo
            WHERE t.fecha_inicio >= NOW() - INTERVAL '1 month'
            GROUP BY a.id_arquero, a.nombre
            ORDER BY puntaje_mes DESC
            """;

        return jdbc.query(sql, (rs, n) -> new TopArcherDTO(
                rs.getLong("id_arquero"),
                rs.getString("nombre"),
                rs.getInt("puntaje_mes")
        ));
    }

}