package com.backend.LeagueOfArrows.filter;

import com.backend.LeagueOfArrows.services.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import java.io.IOException;
import java.util.List;


@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter{

    private final JwtService jwtService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
        throws ServletException, IOException{

        // Obtener el token del header
        String authHeader = request.getHeader("Authorization");

        // Filtra el request, segun requiera autenticacion
        if (authHeader == null || !authHeader.startsWith("Bearer")) {
            filterChain.doFilter(request, response);
            return;
        }

        // Extracción del token
        String token = authHeader.substring(7);
        if(!jwtService.isValid(token)){
            filterChain.doFilter(request, response);
            return;
        }
        String email = jwtService.extractEmail(token);
        String role = jwtService.extractRole(token);
        Long userId = jwtService.extractUserId(token);

        // Guardar los datos del usuario en el request para que pueda ser accedido en las rutas
        // Tini esto es lo que tienes que ver :D
        request.setAttribute("userId", userId);


        // Creación de la autenticacion para Spring Security, con 3 parametros
        // Primero: email, segundo: credenciales (null porque vienen del token) y tercer parametro: roles/permisos (spring guarda como ROLE_"RoldeAcceso", para este caso "ROLE_ADMIN" o "ROL_ARCHER"
        var auth = new UsernamePasswordAuthenticationToken(email, null, List.of(new SimpleGrantedAuthority("ROLE_" + role.toUpperCase())));

        // Registrar la autenticacion en el contexto de seguridad
        SecurityContextHolder.getContext().setAuthentication(auth);

        // Pasa al siguiente Request
        filterChain.doFilter(request, response);
    }


}
