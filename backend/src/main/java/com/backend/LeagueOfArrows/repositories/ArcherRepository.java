package com.backend.LeagueOfArrows.repositories;

import com.backend.LeagueOfArrows.entities.ArcherEntity;
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
}