package com.backend.LeagueOfArrows.controllers;

import com.backend.LeagueOfArrows.dtos.RoundRequestDTO;
import com.backend.LeagueOfArrows.repositories.RoundRepository;
import com.backend.LeagueOfArrows.services.JwtService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/rounds")
@RequiredArgsConstructor
public class RoundController {

    private final RoundRepository roundRepository;
    private final JwtService      jwtService;

    @PostMapping
    public ResponseEntity<?> registerRound(
            @RequestBody RoundRequestDTO dto,
            HttpServletRequest request) {

        // Extract the admin's userId from the JWT so the audit trigger can record it
        Long adminUserId = null;
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            try {
                adminUserId = jwtService.extractUserId(authHeader.substring(7));
            } catch (Exception ignored) { }
        }

        roundRepository.registerRound(dto, adminUserId);
        return ResponseEntity.ok(Map.of("message", "Ronda registrada exitosamente"));
    }
}