package com.backend.LeagueOfArrows.entities;


import lombok.Data;

@Data
public class InscriptionEntity {
    private Long inscriptionId;
    private Long tournamentId;
    private Long archerId;
    private Integer score;
}
