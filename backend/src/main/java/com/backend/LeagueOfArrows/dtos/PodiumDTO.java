package com.backend.LeagueOfArrows.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PodiumDTO {
    private Integer position;
    private Long archerId;
    private String archerName;
    private Integer finalScore;
}