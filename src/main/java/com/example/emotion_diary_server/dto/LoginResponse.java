package com.example.emotion_diary_server.dto;

/**
 * Successful authentication response containing a JWT access token.
 */
public record LoginResponse(
        /** JWT access token value. */
        String token,
        /** Token type sent in the {@code Authorization} header (typically {@code Bearer}). */
        String tokenType,
        /** Token lifetime in milliseconds from issuance. */
        long expiresInMs
) {
    /**
     * Creates a Bearer-token login response.
     *
     * @param token        JWT access token
     * @param expiresInMs  token lifetime in milliseconds
     */
    public LoginResponse(String token, long expiresInMs) {
        this(token, "Bearer", expiresInMs);
    }
}
