package com.backend.LeagueOfArrows.repositories;

import com.backend.LeagueOfArrows.entities.UserEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.util.List;
import java.util.Optional;

@Repository
public class UserRepository {

    private final JdbcTemplate jdbc;

    public UserRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private final RowMapper<UserEntity> mapper = (rs, n) -> new UserEntity(
            rs.getLong("id_usuario"),
            rs.getString("email"),
            rs.getString("contrasena"),
            rs.getString("rol")
    );

    public Optional<UserEntity> findByEmail(String email) {
        String sql = "SELECT id_usuario, email, contrasena, rol FROM usuario WHERE email = ?";
        List<UserEntity> result = jdbc.query(sql, mapper, email);
        return result.isEmpty() ? Optional.empty() : Optional.of(result.get(0));
    }

    public Optional<UserEntity> findById(Long id) {
        String sql = "SELECT id_usuario, email, contrasena, rol FROM usuario WHERE id_usuario = ?";
        List<UserEntity> result = jdbc.query(sql, mapper, id);
        return result.isEmpty() ? Optional.empty() : Optional.of(result.get(0));
    }

    public UserEntity save(UserEntity u) {
        String sql = "INSERT INTO usuario (email, contrasena, rol) VALUES (?, ?, ?)";
        KeyHolder kh = new GeneratedKeyHolder();
        jdbc.update(con -> {
            PreparedStatement ps = con.prepareStatement(sql, new String[]{"id_usuario"});
            ps.setString(1, u.getEmail());
            ps.setString(2, u.getPassword());
            ps.setString(3, u.getRol());
            return ps;
        }, kh);
        u.setUserId(kh.getKey().longValue());
        return u;
    }
}