package com.example.emotion_diary_server.user;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class User {

    @Id
    @GeneratedValue
    private Long id;

    @Column(unique = true)
    private String username;
    private String password;
}