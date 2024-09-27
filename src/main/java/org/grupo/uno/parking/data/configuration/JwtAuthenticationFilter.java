package org.grupo.uno.parking.data.configuration;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import org.grupo.uno.parking.data.service.JwtService;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collection;
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    public JwtAuthenticationFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        final String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            try {
                final String jwt = authHeader.substring(7);
                final String userEmail = jwtService.extractUsername(jwt);
                Collection<GrantedAuthority> authorities = jwtService.extractRoles(jwt);

                // Si el usuario tiene roles válidos, lo configuramos en el contexto de seguridad
                if (userEmail != null && authorities != null) {
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userEmail,
                            null,
                            authorities
                    );

                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            } catch (Exception exception) {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token inválido");
                return; // Salir si hay un error
            }
        }

        filterChain.doFilter(request, response);
    }
}
