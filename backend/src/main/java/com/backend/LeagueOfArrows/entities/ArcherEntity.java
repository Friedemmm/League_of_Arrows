package com.backend.LeagueOfArrows.entities;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ArcherEntity {
    private Long   archerId;
    private Long   userId;
    private String name;
    private Long   categoryId;
    // Enriched by JOIN — only populated by findAll()
    private String categoryName;
    private String email;
}
