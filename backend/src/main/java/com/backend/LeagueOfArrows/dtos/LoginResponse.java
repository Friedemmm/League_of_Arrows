package com.backend.LeagueOfArrows.dtos;


import lombok.Data;

@Data
public class LoginResponse {
    private String token;
    private String role;
    private Long userId;

    public LoginResponse(String token, String role, Long userId){
        this.token = token;
        this.role = role;
        this.userId = userId;
    }
}
