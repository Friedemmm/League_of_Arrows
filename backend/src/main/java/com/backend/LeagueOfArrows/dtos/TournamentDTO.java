package com.backend.LeagueOfArrows.dtos;

import lombok.Data;
import java.time.LocalDate;

@Data
public class TournamentDTO {
    private String name;
    private Long categoryId;
    private LocalDate startDate;
    private LocalDate endDate;
    private Boolean active;
}
