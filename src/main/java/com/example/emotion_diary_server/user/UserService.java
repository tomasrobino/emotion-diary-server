package com.example.emotion_diary_server.user;

import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Spring Security integration for users and username search helpers.
 */
@Service
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;

    /**
     * @param userRepository repository for loading users
     */
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Loads credentials for Spring Security authentication.
     *
     * @param username username from the authentication request (matched case-insensitively)
     * @return Spring Security user details with role {@code USER}
     * @throws UsernameNotFoundException if no user exists for the username
     */
    @Override
    public UserDetails loadUserByUsername(String username)
            throws UsernameNotFoundException {

        User user = userRepository.findByUsernameIgnoreCase(username)
                .orElseThrow(() ->
                        new UsernameNotFoundException("User not found"));

        return org.springframework.security.core.userdetails.User
                .withUsername(Objects.requireNonNull(user.getUsername()))
                .password(Objects.requireNonNull(user.getPassword()))
                .roles("USER")
                .build();
    }

    /**
     * Checks whether a username is already registered (case-insensitive).
     *
     * @param username username to check
     * @return {@code true} if a user with that username exists
     */
    public boolean existsByUsernameIgnoreCase(String username) {
        return userRepository.findByUsernameIgnoreCase(username).isPresent();
    }

    /**
     * Searches usernames by prefix for autocomplete, including an exact match when applicable.
     *
     * @param query prefix or full username to search
     * @return up to ten distinct matching usernames in stable order; empty if the query is blank
     */
    public List<String> searchUsersByPrefix(String query) {
        String prefix = query.trim();
        if (prefix.isEmpty()) {
            return List.of();
        }

        Set<String> usernames = new LinkedHashSet<>();
        userRepository
                .findTop10ByUsernameStartingWithIgnoreCaseOrderByUsernameAsc(prefix)
                .stream()
                .map(User::getUsername)
                .filter(username -> username != null && !username.isBlank())
                .forEach(usernames::add);

        userRepository.findByUsernameIgnoreCase(prefix)
                .map(User::getUsername)
                .filter(username -> username != null && !username.isBlank())
                .ifPresent(usernames::add);

        return new ArrayList<>(usernames);
    }
}
