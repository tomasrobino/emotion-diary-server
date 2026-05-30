package com.example.emotion_diary_server.service;

import com.example.emotion_diary_server.user.User;
import com.example.emotion_diary_server.user.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final MoodboardSeedService moodboardSeedService;

    @Value("${app.seed.moodboards.enabled:false}")
    private boolean seedMoodboardsEnabled;

    @Value("${app.seed.moodboards.user:aaaa}")
    private String seedMoodboardsUser;

    @Value("${app.seed.moodboards.count:0}")
    private int seedMoodboardsCount;

    public DataInitializer(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            MoodboardSeedService moodboardSeedService
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.moodboardSeedService = moodboardSeedService;
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

        if (seedMoodboardsEnabled && seedMoodboardsCount > 0) {
            if (userRepository.findByUsername(seedMoodboardsUser).isEmpty()) {
                User user = new User();
                user.setUsername(seedMoodboardsUser);
                user.setPassword(Objects.requireNonNull(passwordEncoder.encode("password123")));
                userRepository.save(user);
                System.out.println("Seed user created: " + seedMoodboardsUser + " / password123");
            }
            int created = moodboardSeedService.seedMoodboards(seedMoodboardsUser, seedMoodboardsCount);
            System.out.println("Seeded " + created + " moodboards for user " + seedMoodboardsUser);
        }
    }
}
