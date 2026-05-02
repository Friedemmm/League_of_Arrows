package com.backend.LeagueOfArrows.services;

import com.backend.LeagueOfArrows.entities.TournamentEntity;
import com.backend.LeagueOfArrows.repositories.TournamentRepository;
import com.backend.LeagueOfArrows.dtos.TournamentDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.NoSuchElementException;
@Service
@RequiredArgsConstructor
public class TournamentService {

    private final TournamentRepository tournamentRepository;

    public List<TournamentEntity> findAll(){
        return tournamentRepository.findAll();
    }

    public TournamentEntity findById(Long id){
        return tournamentRepository.findById(id).orElseThrow(()-> new NoSuchElementException("Torneo no encontrado"));
    }

    public TournamentEntity create(TournamentDTO tournamentDTO){
        TournamentEntity tournament = new TournamentEntity();
        tournament.setCategoryId(tournamentDTO.getCategoryId());
        tournament.setStartDate(tournamentDTO.getStartDate());
        tournament.setEndDate(tournamentDTO.getEndDate());
        tournament.setActive(tournamentDTO.getActive() != null ? tournamentDTO.getActive() : true);
        Long id = tournamentRepository.save(tournament);
        return tournamentRepository.findById(id).orElseThrow();
    }

    public TournamentEntity update(Long id, TournamentDTO tournamentDTO){
        TournamentEntity tournament = tournamentRepository.findById(id).orElseThrow(()-> new NoSuchElementException("Torneo no encontrado"));
        tournament.setCategoryId(tournamentDTO.getCategoryId());
        tournament.setStartDate(tournamentDTO.getStartDate());
        tournament.setEndDate(tournamentDTO.getEndDate());
        tournament.setActive(tournamentDTO.getActive() != null ? tournamentDTO.getActive() : true);
        tournamentRepository.update(tournament);
        return tournamentRepository.findById(id).orElseThrow();
    }

    public void delete(Long id){
        int rows = tournamentRepository.deleteById(id);
        if(rows == 0) throw new NoSuchElementException("Torneo no encontrado");
    }
}
