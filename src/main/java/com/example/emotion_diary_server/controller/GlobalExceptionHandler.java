package com.example.emotion_diary_server.controller;

import com.example.emotion_diary_server.user.UsernameAlreadyExistsException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;
import java.util.stream.Collectors;

/**
 * Maps common exceptions to JSON error responses with an {@code error} message field.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handles invalid arguments and business rule violations.
     *
     * @param ex exception carrying the message
     * @return 400 Bad Request with {@code {"error": "..."}}
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
    }

    /**
     * Handles explicit HTTP status exceptions.
     *
     * @param ex exception with status and optional reason
     * @return response with the exception's status code and {@code error} body
     */
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, String>> handleResponseStatus(ResponseStatusException ex) {
        String message = ex.getReason() != null ? ex.getReason() : ex.getStatusCode().toString();
        return ResponseEntity.status(ex.getStatusCode()).body(Map.of("error", message));
    }

    /**
     * Handles Bean Validation failures on request bodies.
     *
     * @param ex binding errors from validation
     * @return 400 Bad Request with concatenated field messages
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidation(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining("; "));
        if (message.isBlank()) {
            message = "Validation failed";
        }
        return ResponseEntity.badRequest().body(Map.of("error", message));
    }

    /**
     * Handles failed login and other authentication errors.
     *
     * @param ex authentication failure
     * @return 401 Unauthorized with a generic error message
     */
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<Map<String, String>> handleAuthenticationFailure(AuthenticationException ex) {
        String message = ex instanceof BadCredentialsException
                ? "Invalid username or password"
                : "Authentication failed";
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", message));
    }

    /**
     * Handles duplicate username on registration.
     *
     * @param ex conflict exception
     * @return 409 Conflict with {@code error} message
     */
    @ExceptionHandler(UsernameAlreadyExistsException.class)
    public ResponseEntity<Map<String, String>> handleUsernameAlreadyExists(UsernameAlreadyExistsException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", ex.getMessage()));
    }
}
