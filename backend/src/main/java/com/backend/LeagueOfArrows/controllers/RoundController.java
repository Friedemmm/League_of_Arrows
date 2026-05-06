package com.backend.LeagueOfArrows.controllers;

import com.backend.LeagueOfArrows.dtos.RoundDetailDTO;
import com.backend.LeagueOfArrows.dtos.RoundRequestDTO;
import com.backend.LeagueOfArrows.repositories.RoundRepository;
import com.backend.LeagueOfArrows.services.JwtService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/rounds")
@RequiredArgsConstructor
public class RoundController {

    private final RoundRepository roundRepository;
    private final JwtService      jwtService;

    // ── Register a new round ─────────────────────────────────────────
    @PostMapping
    public ResponseEntity<?> registerRound(
            @RequestBody RoundRequestDTO dto,
            HttpServletRequest request) {

        Long adminUserId = extractUserId(request);
        roundRepository.registerRound(dto, adminUserId);
        return ResponseEntity.ok(Map.of("message", "Ronda registrada exitosamente"));
    }

    // ── Get all rounds (with arrows) for an archer in a tournament ───
    @GetMapping
    public ResponseEntity<List<RoundDetailDTO>> getRounds(
            @RequestParam Long tournamentId,
            @RequestParam Long archerId) {

        return ResponseEntity.ok(roundRepository.getRoundsByTournamentAndArcher(tournamentId, archerId));
    }

    // ── Update a single arrow score ──────────────────────────────────
    @PutMapping("/{roundId}/arrows/{arrowId}")
    public ResponseEntity<?> updateArrow(
            @PathVariable Long roundId,
            @PathVariable Long arrowId,
            @RequestBody Map<String, Integer> body,
            HttpServletRequest request) {

        Integer newScore = body.get("score");
        if (newScore == null || newScore < 0 || newScore > 10) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "El puntaje debe estar entre 0 y 10."));
        }

        Long adminUserId = extractUserId(request);
        roundRepository.updateArrowScore(roundId, arrowId, newScore, adminUserId);
        return ResponseEntity.ok(Map.of("message", "Puntaje actualizado correctamente"));
    }

    // ── Helper ───────────────────────────────────────────────────────
    private Long extractUserId(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            try { return jwtService.extractUserId(authHeader.substring(7)); }
            catch (Exception ignored) {}
        }
        return null;
    }
}
