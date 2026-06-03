package com.example.emotion_diary_server.user;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.jspecify.annotations.Nullable;

/**
 * JPA entity representing an application user account.
 */
@Entity
@Table(name = "users")
@Getter
@Setter
public class User {

    /** Surrogate primary key assigned by the database. */
    @Id
    @GeneratedValue
    private @Nullable Long id;

    /** Unique login name; stored in lowercase after registration normalization. */
    @Column(unique = true)
    private @Nullable String username;

    /** BCrypt-hashed password; never exposed in JSON responses. */
    @JsonIgnore
    private @Nullable String password;
}
