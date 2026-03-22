package com.example.emotion_diary_server.service;

import com.example.emotion_diary_server.user.User;
import com.example.emotion_diary_server.user.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (userRepository.findByUsername("admin").isEmpty()) {
            User user = new User();
            user.setUsername("admin");
            user.setPassword(Objects.requireNonNull(passwordEncoder.encode("admin123")));
            userRepository.save(user);
            System.out.println("Default user created: admin / admin123");
        }
    }
}