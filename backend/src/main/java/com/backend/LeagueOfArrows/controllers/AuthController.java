package com.backend.LeagueOfArrows.controllers;

import com.backend.LeagueOfArrows.dtos.LoginRequest;
import com.backend.LeagueOfArrows.dtos.LoginResponse;
import com.backend.LeagueOfArrows.entities.UserEntity;
import com.backend.LeagueOfArrows.repositories.UserRepository;
import com.backend.LeagueOfArrows.services.JwtService;
import lombok.RequiredArgsConstructor;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request){

        // Buscar el usuario por email
        UserEntity user = userRepository.findByEmail(request.getEmail()).orElse(null);

        //
        if (user == null || !passwordEncoder.matches(request.getPassword(), user.getPassword())){
            return ResponseEntity.status(401).body(Map.of("error", "Credenciales incorrectas"));
        }

        // Generar el token si logra iniciar sesion
        String token = jwtService.generateToken(user.getEmail(), user.getRol(), user.getUserId());

        return ResponseEntity.ok(new LoginResponse(token, user.getRol(), user.getUserId()));
    }
    
}