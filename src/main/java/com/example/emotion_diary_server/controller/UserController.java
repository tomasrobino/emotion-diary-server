package com.example.emotion_diary_server.controller;

import com.example.emotion_diary_server.user.UserService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST endpoints for discovering other users (e.g. when granting moodboard access).
 */
@Tag(name = "Users")
@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    /**
     * @param userService user lookup service
     */
    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * GET /users/search — find usernames by prefix.
     * <p>
     * Requires authentication. Returns at most 10 matches, case-insensitive.
     *
     * @param q username prefix to search
     * @return 200 OK with list of matching usernames (may be empty)
     */
    @GetMapping("/search")
    public ResponseEntity<List<String>> searchUsers(@RequestParam("q") String q) {
        return ResponseEntity.ok(userService.searchUsersByPrefix(q));
    }
}
