package com.example.emotion_diary_server.controller;

import com.example.emotion_diary_server.user.User;
import com.example.emotion_diary_server.user.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserRepository userRepository;

    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * GET /users/search?q={prefix}
     * Returns up to 10 usernames matching the given prefix (case-insensitive).
     */
    @GetMapping("/search")
    public ResponseEntity<List<String>> searchUsers(@RequestParam("q") String q) {
        String prefix = q.trim();
        if (prefix.isEmpty()) {
            return ResponseEntity.ok(List.of());
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

        return ResponseEntity.ok(new ArrayList<>(usernames));
    }
}
