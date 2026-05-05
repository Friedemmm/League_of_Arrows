package com.backend.LeagueOfArrows.controllers;

import com.backend.LeagueOfArrows.dtos.RoundRequestDTO;
import com.backend.LeagueOfArrows.repositories.RoundRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/rounds")
@RequiredArgsConstructor
public class RoundController {

    private final RoundRepository roundRepository;

    @PostMapping
    public ResponseEntity<?> registerRound(@RequestBody RoundRequestDTO dto) {
        roundRepository.registerRound(dto);
        return ResponseEntity.ok(Map.of("message", "Ronda registrada exitosamente"));
    }
}