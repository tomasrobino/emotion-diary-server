package com.example.emotion_diary_server.user;

import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

import java.util.Objects;

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
}