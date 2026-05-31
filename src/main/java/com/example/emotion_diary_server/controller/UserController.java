package com.example.emotion_diary_server.controller;

import com.example.emotion_diary_server.user.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * GET /users/search?q={prefix}
     * Returns up to 10 usernames matching the given prefix (case-insensitive).
     */
    @GetMapping("/search")
    public ResponseEntity<List<String>> searchUsers(@RequestParam("q") String q) {
        return ResponseEntity.ok(userService.searchUsersByPrefix(q));
    }
}
