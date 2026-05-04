package com.backend.LeagueOfArrows.services;

import com.backend.LeagueOfArrows.dtos.ArcherDTO;
import com.backend.LeagueOfArrows.dtos.HistoryDTO;
import com.backend.LeagueOfArrows.dtos.TopArcherDTO;
import com.backend.LeagueOfArrows.entities.ArcherEntity;
import com.backend.LeagueOfArrows.repositories.ArcherRepository;
import com.backend.LeagueOfArrows.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.List;


@Service
@RequiredArgsConstructor
public class ArcherService  {

    private final ArcherRepository archerRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public List<ArcherEntity> findAll(){
        return archerRepository.findAll();
    }

    public ArcherEntity findById(Long id){
        return archerRepository.findById(id).orElseThrow(()-> new NoSuchElementException("Arquero no encontrado"));
    }

    // Creacion de un nuevo arquero
    public ArcherEntity save(ArcherDTO archerDTO){
        String hash = passwordEncoder.encode(archerDTO.getPassword());
        Long userId = userRepository.saveUser(archerDTO.getEmail(), hash, "ARQUERO");
        Long archerId = archerRepository.save(userId, archerDTO.getName());
        return archerRepository.findById(archerId).orElseThrow();
    }

    public ArcherEntity update(Long id, ArcherDTO archerDTO){
        archerRepository.findById(id).orElseThrow(()-> new NoSuchElementException("Arquero no encontrado"));
        archerRepository.update(id, archerDTO.getName());
        return archerRepository.findById(id).orElseThrow();
    }

    public void delete(Long id){
        int rows = archerRepository.deleteById(id);
        if(rows == 0) throw new NoSuchElementException("Arquero no encontrado");
    }
    public List<HistoryDTO> getArcherHistory(Long userId) {
        // Depende de usuario logueado, busca el arquero asociado al usuario logueado
        ArcherEntity archer = archerRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Arquero no encontrado para este usuario"));

        // Con el id del arquero busca su historial
        return archerRepository.findHistoryByArcherId(archer.getArcherId());
    }
    public List<TopArcherDTO> getTopArchersLastMonth() {
        return archerRepository.findTopArchersLastMonth();
    }
}
