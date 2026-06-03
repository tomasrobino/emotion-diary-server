package com.example.emotion_diary_server.controller;

import com.example.emotion_diary_server.config.JwtProperties;
import com.example.emotion_diary_server.dto.LoginRequest;
import com.example.emotion_diary_server.dto.LoginResponse;
import com.example.emotion_diary_server.security.JwtService;
import com.example.emotion_diary_server.user.AuthService;
import io.swagger.v3.oas.annotations.tags.Tag;
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

/**
 * REST endpoints for user registration, login, and logout.
 */
@Tag(name = "Authentication")
@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final AuthService authService;
    private final JwtService jwtService;
    private final JwtProperties jwtProperties;

    /**
     * Creates the authentication controller.
     *
     * @param authenticationManager Spring Security authentication manager
     * @param authService           registration and logout logic
     * @param jwtService            JWT creation
     * @param jwtProperties         token expiration settings
     */
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

    /**
     * POST /auth/register — create a new account and return a JWT.
     * <p>
     * Public (no authentication required). Username is normalized to lowercase.
     *
     * @param request username and password; may be null (treated as empty)
     * @return 201 Created with {@link LoginResponse} containing token and expiration
     * @throws com.example.emotion_diary_server.user.UsernameAlreadyExistsException when username is taken (409 via advice)
     */
    @PostMapping("/register")
    public ResponseEntity<LoginResponse> register(@RequestBody @Nullable LoginRequest request) {
        LoginResponse response = authService.register(
                request != null ? request.username() : null,
                request != null ? request.password() : null
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * POST /auth/login — authenticate and return a JWT.
     * <p>
     * Public. Username is trimmed and lowercased before authentication.
     *
     * @param request username and password
     * @return 200 OK with {@link LoginResponse}
     * @throws org.springframework.security.core.AuthenticationException on invalid credentials (401 via advice)
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody @Nullable LoginRequest request) {
        String username = request != null && request.username() != null ? request.username().trim().toLowerCase() : "";
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, request != null ? request.password() : null)
        );
        String token = jwtService.generateToken(username);
        return ResponseEntity.ok(new LoginResponse(token, jwtProperties.expirationMs()));
    }

    /**
     * POST /auth/logout — revoke the current JWT.
     * <p>
     * Requires authentication. Reads Bearer token from {@code Authorization} header when present.
     *
     * @param request HTTP request carrying optional Bearer token
     * @return 204 No Content
     */
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
