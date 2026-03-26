package com.smartincident.processor.controller;

import com.smartincident.processor.entity.User;
import com.smartincident.processor.repository.UserRepository;
import com.smartincident.processor.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Authentication controller — register, login, me.
 * All paths under /api/auth/** are public (no JWT required).
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    /**
     * Register a new user.
     * Body: { name, email, password, role? (defaults to DEVELOPER) }
     */
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        String password = body.get("password");
        String name = body.get("name");
        String role = body.getOrDefault("role", "DEVELOPER").toUpperCase();

        if (email == null || password == null || name == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "name, email, and password are required"));
        }

        if (!role.equals("ADMIN") && !role.equals("DEVELOPER")) {
            return ResponseEntity.badRequest().body(Map.of("error", "role must be ADMIN or DEVELOPER"));
        }

        if (userRepository.existsByEmail(email)) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("error", "Email already registered"));
        }

        User user = User.builder()
                .name(name)
                .email(email)
                .password(passwordEncoder.encode(password))
                .role(role)
                .build();

        User saved = userRepository.save(user);
        log.info("Registered new user: {} ({})", email, role);

        String token = jwtUtil.generateToken(saved.getEmail(), saved.getRole());
        return ResponseEntity.status(HttpStatus.CREATED).body(buildAuthResponse(saved, token));
    }

    /**
     * Login with email and password.
     * Body: { email, password }
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        String password = body.get("password");

        if (email == null || password == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "email and password are required"));
        }

        return userRepository.findByEmail(email)
                .filter(user -> passwordEncoder.matches(password, user.getPassword()))
                .map(user -> {
                    String token = jwtUtil.generateToken(user.getEmail(), user.getRole());
                    log.info("User logged in: {} ({})", email, user.getRole());
                    return ResponseEntity.ok(buildAuthResponse(user, token));
                })
                .orElse(ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Invalid email or password")));
    }

    /**
     * Get current authenticated user profile.
     */
    @GetMapping("/me")
    public ResponseEntity<?> me(Authentication auth) {
        if (auth == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        String email = (String) auth.getPrincipal();
        return userRepository.findByEmail(email)
                .map(user -> ResponseEntity.ok(Map.of(
                        "id", user.getId(),
                        "name", user.getName(),
                        "email", user.getEmail(),
                        "role", user.getRole())))
                .orElse(ResponseEntity.notFound().build());
    }

    private Map<String, Object> buildAuthResponse(User user, String token) {
        return Map.of(
                "token", token,
                "user", Map.of(
                        "id", user.getId(),
                        "name", user.getName(),
                        "email", user.getEmail(),
                        "role", user.getRole()));
    }
}
