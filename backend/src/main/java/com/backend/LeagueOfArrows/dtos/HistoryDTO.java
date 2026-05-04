package com.backend.LeagueOfArrows.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HistoryDTO {
    private Long tournamentId;
    private String tournamentName;
    private String categoryName;
    private LocalDate startDate;
    private LocalDate endDate;
    private boolean active;
    private Integer totalScore;
    private Integer position; // es null si el torneo aun no termina
}