package com.backend.LeagueOfArrows.entities;

import lombok.Data;
import java.time.LocalDate;

@Data
public class TournamentEntity {
    private Long idTournament;
    private Long idCategory;
    private LocalDate startDate;
    private LocalDate endDate;
    private boolean isActive;
}
