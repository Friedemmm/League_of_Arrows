package com.backend.LeagueOfArrows.repositories;

import com.backend.LeagueOfArrows.entities.UserEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class UserRepository {

    private final JdbcTemplate jdbc;


    private final RowMapper<UserEntity> userRowMapper = (rs, rowNum) -> {
        UserEntity user = new UserEntity();
        user.setUserId(rs.getLong("id_user"));
        user.setEmail(rs.getString("email"));
        user.setPassword(rs.getString("password"));
        user.setRol(rs.getString("rol"));
        return user;
    };

    // Busca usuario por email, si no encuentra devuelve Optional.empty()
    public Optional<UserEntity> findByEmail(String email) {
        var list = jdbc.query("SELECT * FROM users WHERE email = ?", userRowMapper, email);
        return list.stream().findFirst();
    }

    // Guarda un nuevo usuario en la base de datos
    public long saveUser(String email, String password, String role) {
        return jdbc.queryForObject("INSERT INTO users (email, password, rol) VALUES (?, ?, ?) RETURNING id_user", Long.class, email, password, role);
    }


}