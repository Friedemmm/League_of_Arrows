package com.backend.LeagueOfArrows.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TopArcherDTO {
    private Long archerId;
    private String name;
    private Integer monthlyScore;
}