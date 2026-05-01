package com.backend.LeagueOfArrows.entities;


import lombok.Data;

@Data
public class UserEntity {
    private Long idUsuario;
    private String email;
    private String password;
    private String rol; //Arquero o Administrador
}
