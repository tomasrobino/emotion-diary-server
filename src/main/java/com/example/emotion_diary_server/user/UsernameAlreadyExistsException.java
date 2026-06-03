package com.example.emotion_diary_server.user;

/**
 * Thrown when registration is attempted with a username that is already taken.
 */
public class UsernameAlreadyExistsException extends RuntimeException {

    /**
     * @param username the conflicting username
     */
    public UsernameAlreadyExistsException(String username) {
        super("Username already taken: " + username);
    }
}
