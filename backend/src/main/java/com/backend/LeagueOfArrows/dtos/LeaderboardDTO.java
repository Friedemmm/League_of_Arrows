package com.backend.LeagueOfArrows.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LeaderboardDTO {
    private Integer globalPosition;
    private Long    archerId;
    private String  name;
    private Integer tournamentsPlayed;
    private Integer arrowsFired;
    private Integer totalScore;
    private BigDecimal avgPointsPerArrow;
}
