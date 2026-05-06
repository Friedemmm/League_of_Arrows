package com.backend.LeagueOfArrows.config;

import com.backend.LeagueOfArrows.filter.JwtAuthFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;


@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // Se deshabilita HTTP para manejar rutas por token
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // Definicion de rutas segun roles
                .authorizeHttpRequests(auth -> auth

                        // En caso de querer agregar mas rutas el orden convencional que se sigue es:
                        // Rutas publicas -> Rutas especificas -> Rutas generales

                        // Rutas publicas
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/tournaments").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/categories").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/archers/top-month").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/archers/leaderboard").permitAll()

                        // Rutas especificas
                        .requestMatchers(HttpMethod.GET, "/api/archers/me/history").hasRole("ARQUERO")
                        .requestMatchers(HttpMethod.GET, "/api/tournaments/*/podium").permitAll()

                        // Rutas generales
                        .requestMatchers(HttpMethod.POST, "/api/tournaments/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/tournaments/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/tournaments/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/archers/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/archers/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/archers/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/inscriptions/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/inscriptions/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/rounds/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET,  "/api/audit").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET,  "/api/tournaments/*/rankings").authenticated()


                        // Cualquier otra ruta requiere autenticacion
                        .anyRequest().authenticated()
                )

                // Agregar filtro de autenticacion
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();

    }

    // Encrypt de contraseña :D
    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }
}
