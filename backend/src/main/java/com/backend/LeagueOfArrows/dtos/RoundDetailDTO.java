package com.backend.LeagueOfArrows.dtos;

import lombok.Data;
import java.util.List;

@Data
public class RoundDetailDTO {
    private Long roundId;
    private Integer roundNumber;
    private List<ArrowDTO> arrows;

    @Data
    public static class ArrowDTO {
        private Long arrowId;
        private Integer arrowNumber;
        private Integer score;
    }
}
