package com.backend.LeagueOfArrows.dtos;

import lombok.Data;
import java.util.List;

@Data
public class RoundRequestDTO {
    private Long tournamentId;
    private Long archerId;
    private Integer roundNumber;
    private List<Integer> scores;
}