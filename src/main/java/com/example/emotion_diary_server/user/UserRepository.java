package com.example.emotion_diary_server.user;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data repository for {@link User} persistence and username lookups.
 */
public interface UserRepository  extends JpaRepository<User, Long> {

    /**
     * Finds a user by exact username match (case-sensitive).
     *
     * @param username stored username
     * @return the user if present
     */
    Optional<User> findByUsername(String username);

    /**
     * Finds a user by username, ignoring case.
     *
     * @param username username to match
     * @return the user if present
     */
    Optional<User> findByUsernameIgnoreCase(String username);

    /**
     * Returns up to ten users whose usernames start with the given prefix, ordered alphabetically.
     *
     * @param username prefix to match (case-insensitive)
     * @return matching users, at most ten
     */
    List<User> findTop10ByUsernameStartingWithIgnoreCaseOrderByUsernameAsc(String username);
}
