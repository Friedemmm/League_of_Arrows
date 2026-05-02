package com.backend.LeagueOfArrows.repositories;

import com.backend.LeagueOfArrows.entities.ArcherEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.util.List;
import java.util.Optional;

@Repository
public class ArcherRepository {

    private final JdbcTemplate jdbc;

    public ArcherRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private final RowMapper<ArcherEntity> mapper = (rs, n) -> new ArcherEntity(
            rs.getLong("id_arquero"),
            rs.getLong("id_usuario"),
            rs.getString("nombre")
    );

    public List<ArcherEntity> findAll() {
        return jdbc.query(
                "SELECT id_arquero, id_usuario, nombre FROM arquero", mapper);
    }

    public Optional<ArcherEntity> findById(Long id) {
        List<ArcherEntity> result = jdbc.query(
                "SELECT id_arquero, id_usuario, nombre FROM arquero WHERE id_arquero = ?",
                mapper, id);
        return result.isEmpty() ? Optional.empty() : Optional.of(result.get(0));
    }

    public Optional<ArcherEntity> findByUserId(Long userId) {
        List<ArcherEntity> result = jdbc.query(
                "SELECT id_arquero, id_usuario, nombre FROM arquero WHERE id_usuario = ?",
                mapper, userId);
        return result.isEmpty() ? Optional.empty() : Optional.of(result.get(0));
    }

    public ArcherEntity save(ArcherEntity a) {
        String sql = "INSERT INTO arquero (id_usuario, nombre) VALUES (?, ?)";
        KeyHolder kh = new GeneratedKeyHolder();
        jdbc.update(con -> {
            PreparedStatement ps = con.prepareStatement(sql, new String[]{"id_arquero"});
            ps.setLong(1, a.getUserId());
            ps.setString(2, a.getName());
            return ps;
        }, kh);
        a.setArcherId(kh.getKey().longValue());
        return a;
    }

    public int update(ArcherEntity a) {
        return jdbc.update(
                "UPDATE arquero SET nombre = ? WHERE id_arquero = ?",
                a.getName(), a.getArcherId());
    }

    public int deleteById(Long id) {
        return jdbc.update("DELETE FROM arquero WHERE id_arquero = ?", id);
    }
}