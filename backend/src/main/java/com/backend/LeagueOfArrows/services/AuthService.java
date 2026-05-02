package com.backend.LeagueOfArrows.services;

import com.backend.LeagueOfArrows.dtos.LoginResponse;
import com.backend.LeagueOfArrows.dtos.RegisterRequest;
import com.backend.LeagueOfArrows.entities.ArcherEntity;
import com.backend.LeagueOfArrows.entities.UserEntity;
import com.backend.LeagueOfArrows.repositories.ArcherRepository;
import com.backend.LeagueOfArrows.repositories.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

}