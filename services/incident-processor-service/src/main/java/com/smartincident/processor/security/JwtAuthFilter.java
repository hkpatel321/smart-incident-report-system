package com.smartincident.processor.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * JWT authentication filter — runs once per request.
 * Extracts Bearer token, validates it, and populates the SecurityContext.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            if (jwtUtil.isValid(token)) {
                String email = jwtUtil.extractEmail(token);
                String role = jwtUtil.extractRole(token);

                // Build Spring Security authority: ROLE_ADMIN or ROLE_DEVELOPER
                SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + role);
                UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(email, null,
                        List.of(authority));
                SecurityContextHolder.getContext().setAuthentication(auth);
                log.debug("Authenticated user: {} with role: {}", email, role);
            } else {
                log.warn("Invalid JWT token from {}", request.getRequestURI());
            }
        }

        filterChain.doFilter(request, response);
    }
}
