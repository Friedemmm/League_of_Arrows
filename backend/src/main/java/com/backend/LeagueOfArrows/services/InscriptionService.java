package com.backend.LeagueOfArrows.services;

import com.backend.LeagueOfArrows.dtos.InscriptionDTO;
import com.backend.LeagueOfArrows.entities.InscriptionEntity;
import com.backend.LeagueOfArrows.repositories.InscriptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.NoSuchElementException;



@Service
@RequiredArgsConstructor
public class InscriptionService {

    private final InscriptionRepository inscriptionRepository;

    public List<InscriptionEntity> findAll(){
        return inscriptionRepository.findAll();
    }

    public List<InscriptionEntity> findByArcherId(Long archerId){
        return inscriptionRepository.findByArcherId(archerId);
    }

    public InscriptionEntity create(InscriptionDTO inscriptionDTO){
        Long inscriptionId = inscriptionRepository.save(inscriptionDTO.getArcherId(), inscriptionDTO.getTournamentId());
        return inscriptionRepository.findById(inscriptionId).orElseThrow();
    }

    public void delete(Long id){
        int rows = inscriptionRepository.deteleById(id);
        if(rows == 0) throw new NoSuchElementException("Inscripcion no encontrada");
    }
}
