package com.shopsphere.backend.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    public JwtAuthenticationFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);
        System.out.println("METHOD: " + request.getMethod());
        System.out.println("PATH: " + request.getRequestURI());
        System.out.println("AUTH HEADER PRESENT: " + (authHeader != null));
        try {
            String email = jwtService.extractEmail(token);
            String role = jwtService.extractRole(token);

            if (SecurityContextHolder.getContext().getAuthentication() == null
                    && jwtService.isTokenValid(token, email)) {

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                email,
                                null,
                                List.of(new SimpleGrantedAuthority(role))
                        );

                authentication.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request)
                );

                SecurityContextHolder.getContext().setAuthentication(authentication);
                System.out.println("AUTHENTICATED USER: " + email);
                System.out.println("AUTHENTICATED ROLE: " + role);
            }
        } catch (io.jsonwebtoken.JwtException | IllegalArgumentException ex) {
            SecurityContextHolder.clearContext();
            System.out.println("JWT ERROR: " + ex.getMessage());
        }

        filterChain.doFilter(request, response);
        System.out.println("SECURITY AUTH: " +
                SecurityContextHolder.getContext().getAuthentication());
    }
}