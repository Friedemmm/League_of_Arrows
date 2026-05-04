package com.backend.LeagueOfArrows.controllers;


import com.backend.LeagueOfArrows.dtos.ArcherDTO;
import com.backend.LeagueOfArrows.dtos.TopArcherDTO;
import com.backend.LeagueOfArrows.services.ArcherService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import java.util.Map;
import java.util.NoSuchElementException;
import com.backend.LeagueOfArrows.dtos.HistoryDTO;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;


@RestController
@RequestMapping("/api/archers")
@RequiredArgsConstructor
public class ArcherController {

    private final ArcherService archerService;


    @GetMapping
    public ResponseEntity<?> findAll(){
        return ResponseEntity.ok(archerService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> findById(@PathVariable Long id){
        try{
            return ResponseEntity.ok(archerService.findById(id));
        } catch (NoSuchElementException e){
            return ResponseEntity.status(404).body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping
    public ResponseEntity<?> save(@RequestBody ArcherDTO archerDTO){
        return ResponseEntity.status(201).body(archerService.save(archerDTO));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody ArcherDTO archerDTO){
        try{
            return ResponseEntity.ok(archerService.update(id, archerDTO));
        } catch (NoSuchElementException e){
            return ResponseEntity.status(404).body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id){
        try{
            archerService.delete(id);
            return ResponseEntity.noContent().build();
        } catch (NoSuchElementException e){
            return ResponseEntity.status(404).body(Map.of("error", e.getMessage()));
        }
    }
    @GetMapping("/me/history")
    public ResponseEntity<List<HistoryDTO>> getMyHistory(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return ResponseEntity.ok(archerService.getArcherHistory(userId));
    }
    @GetMapping("/top-month")
    public ResponseEntity<List<TopArcherDTO>> getTopArchersLastMonth() {
        return ResponseEntity.ok(archerService.getTopArchersLastMonth());
    }


}
