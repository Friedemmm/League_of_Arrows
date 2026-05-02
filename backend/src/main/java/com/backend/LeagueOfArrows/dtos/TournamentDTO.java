package com.backend.LeagueOfArrows.dtos;

import lombok.Data;
import java.time.LocalDate;

@Data
public class TournamentDTO {
    private Long categoryId;
    private LocalDate startDate;
    private LocalDate endDate;
    private Boolean active;
}
