package com.example.emotion_diary_server.controller;

import com.example.emotion_diary_server.config.JwtProperties;
import com.example.emotion_diary_server.dto.LoginRequest;
import com.example.emotion_diary_server.dto.LoginResponse;
import com.example.emotion_diary_server.security.JwtService;
import com.example.emotion_diary_server.user.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import org.jspecify.annotations.Nullable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final AuthService authService;
    private final JwtService jwtService;
    private final JwtProperties jwtProperties;

    public AuthController(
            AuthenticationManager authenticationManager,
            AuthService authService,
            JwtService jwtService,
            JwtProperties jwtProperties
    ) {
        this.authenticationManager = authenticationManager;
        this.authService = authService;
        this.jwtService = jwtService;
        this.jwtProperties = jwtProperties;
    }

    @PostMapping("/register")
    public ResponseEntity<LoginResponse> register(@RequestBody @Nullable LoginRequest request) {
        LoginResponse response = authService.register(
                request != null ? request.username() : null,
                request != null ? request.password() : null
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody @Nullable LoginRequest request) {
        String username = request != null && request.username() != null ? request.username().trim().toLowerCase() : "";
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, request != null ? request.password() : null)
        );
        String token = jwtService.generateToken(username);
        return ResponseEntity.ok(new LoginResponse(token, jwtProperties.expirationMs()));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request) {
        String token = extractBearerToken(request);
        if (token != null) {
            authService.logout(token);
        }
        return ResponseEntity.noContent().build();
    }

    private static @Nullable String extractBearerToken(HttpServletRequest request) {
        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }
}
