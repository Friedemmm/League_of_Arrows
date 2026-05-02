package com.backend.LeagueOfArrows.controllers;

import com.backend.LeagueOfArrows.dtos.LoginResponse;
import com.backend.LeagueOfArrows.dtos.RegisterRequest;
import com.backend.LeagueOfArrows.services.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<LoginResponse> register(@RequestBody RegisterRequest req) {
        return ResponseEntity.ok(authService.register(req));
    }
}