package com.backend.LeagueOfArrows.entities;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserEntity {
    private Long userId;
    private String email;
    private String password;
    private String rol; //// ARQUERO OR ADMIN
}
