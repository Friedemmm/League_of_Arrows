package com.backend.LeagueOfArrows.entities;

import lombok.Data;
import java.time.LocalDate;

@Data
public class TournamentEntity {
    private Long tournamentId;
    private String nombre;
    private Long categoryId;
    private LocalDate startDate;
    private LocalDate endDate;
    private Boolean active;
}
