package com.backend.LeagueOfArrows.controllers;

import com.backend.LeagueOfArrows.dtos.InscriptionDTO;
import com.backend.LeagueOfArrows.services.InscriptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/inscriptions")
@RequiredArgsConstructor
public class InscriptionController {

    private final InscriptionService inscriptionService;

    @GetMapping
    public ResponseEntity<?> getAllInscriptions(){
        return ResponseEntity.ok(inscriptionService.findAll());
    }

    @PostMapping
    public ResponseEntity<?> createInscription(@RequestBody InscriptionDTO inscriptionDTO){
        return ResponseEntity.status(201).body(inscriptionService.create(inscriptionDTO));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteInscription(@PathVariable Long id){
        try{
            inscriptionService.delete(id);
            return ResponseEntity.noContent().build();
        } catch (NoSuchElementException e){
            return ResponseEntity.status(404).body(Map.of("error", e.getMessage()));
        }
    }
}
