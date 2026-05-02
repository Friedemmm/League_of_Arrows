package com.backend.LeagueOfArrows.controllers;

import com.backend.LeagueOfArrows.dtos.TournamentDTO;
import com.backend.LeagueOfArrows.services.TournamentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.NoSuchElementException;


@RestController
@RequestMapping("/api/tournaments")
@RequiredArgsConstructor
public class TournamentController {
    private final TournamentService tournamentService;

    @GetMapping
    public ResponseEntity<?> getAllTournaments(){
        return ResponseEntity.ok(tournamentService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getTournamentById(@PathVariable Long id){
        try{
            return ResponseEntity.ok(tournamentService.findById(id));
        } catch (NoSuchElementException e){
            return ResponseEntity.status(404).body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping
    public ResponseEntity<?> createTournament(@RequestBody TournamentDTO tournamentDTO){
        return ResponseEntity.status(201).body(tournamentService.create(tournamentDTO));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateTournament(@PathVariable Long id, @RequestBody TournamentDTO tournamentDTO){
        try{
            return ResponseEntity.ok(tournamentService.update(id, tournamentDTO));
        } catch (NoSuchElementException e){
            return ResponseEntity.status(404).body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteTournament(@PathVariable Long id){
        try{
            tournamentService.delete(id);
            return ResponseEntity.noContent().build();
        } catch (NoSuchElementException e){
            return ResponseEntity.status(404).body(Map.of("error", e.getMessage()));
        }
    }
}
