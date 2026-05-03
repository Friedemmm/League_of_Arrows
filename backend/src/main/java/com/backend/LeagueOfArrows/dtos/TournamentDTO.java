package com.backend.LeagueOfArrows.dtos;

import lombok.Data;
import java.time.LocalDate;

@Data
public class TournamentDTO {
    private Long categoryId;
    private String nombre;
    private LocalDate startDate;
    private LocalDate endDate;
    private Boolean active;
}
