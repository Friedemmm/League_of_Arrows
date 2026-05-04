package com.backend.LeagueOfArrows.entities;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ArcherEntity {

    private Long archerId;
    private Long userId;
    private String name;
    private Long categoryId;
}
