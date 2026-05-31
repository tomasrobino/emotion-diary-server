package com.example.emotion_diary_server.user;

import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Service
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

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

    public boolean existsByUsernameIgnoreCase(String username) {
        return userRepository.findByUsernameIgnoreCase(username).isPresent();
    }

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