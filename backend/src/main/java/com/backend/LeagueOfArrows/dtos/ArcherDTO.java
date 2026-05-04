package com.backend.LeagueOfArrows.dtos;


import lombok.Data;

@Data
public class ArcherDTO {
    private String name;
    private String email;
    private String password;
    private Long categoryId;
}
