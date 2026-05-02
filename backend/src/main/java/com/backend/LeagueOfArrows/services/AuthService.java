package com.backend.LeagueOfArrows.services;

import com.backend.LeagueOfArrows.dtos.LoginResponse;
import com.backend.LeagueOfArrows.dtos.RegisterRequest;
import com.backend.LeagueOfArrows.entities.ArcherEntity;
import com.backend.LeagueOfArrows.entities.UserEntity;
import com.backend.LeagueOfArrows.repositories.ArcherRepository;
import com.backend.LeagueOfArrows.repositories.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository userRepo;
    private final ArcherRepository archerRepo;
    private final PasswordEncoder encoder;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepo, ArcherRepository archerRepo, PasswordEncoder encoder, JwtService jwtService) {
        this.userRepo = userRepo;
        this.archerRepo = archerRepo;
        this.encoder = encoder;
        this.jwtService = jwtService;
    }

    @Transactional
    public LoginResponse register(RegisterRequest req) {
        if (userRepo.findByEmail(req.getEmail()).isPresent()) {
            throw new IllegalArgumentException("El email ya se encuentra registrado");
        }
        String rol = req.getRol() == null ? "ARQUERO" : req.getRol().toUpperCase();

        UserEntity nuevoUsuario = new UserEntity(null, req.getEmail(), encoder.encode(req.getPassword()), rol);
        nuevoUsuario = userRepo.save(nuevoUsuario);

        // Como rol es ARQUERO, si es arquero se crea el perfil en la tabla de los arqueros
        if ("ARQUERO".equals(rol)) {
            ArcherEntity nuevoArquero = new ArcherEntity(null, nuevoUsuario.getUserId(), req.getName());
            archerRepo.save(nuevoArquero);
        }

        String token = jwtService.generateToken(nuevoUsuario.getUserId(), nuevoUsuario.getEmail(), nuevoUsuario.getRol());
        return new LoginResponse(token, nuevoUsuario.getEmail(), nuevoUsuario.getRol(), nuevoUsuario.getUserId());
    }
}