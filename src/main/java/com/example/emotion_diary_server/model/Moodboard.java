package com.example.emotion_diary_server.model;



import com.example.emotion_diary_server.user.User;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;

import jakarta.persistence.Entity;

import jakarta.persistence.FetchType;

import jakarta.persistence.ForeignKey;

import jakarta.persistence.GeneratedValue;

import jakarta.persistence.Id;

import jakarta.persistence.JoinColumn;

import jakarta.persistence.Lob;

import jakarta.persistence.ManyToOne;

import org.jspecify.annotations.Nullable;

import lombok.Getter;

import lombok.NoArgsConstructor;

import lombok.Setter;



@Setter

@Getter

@NoArgsConstructor

@Entity

public class Moodboard {

    @Id

    @GeneratedValue(strategy = jakarta.persistence.GenerationType.IDENTITY)

    private @Nullable Long id;



    @JsonIgnore

    @ManyToOne(fetch = FetchType.LAZY)

    @JoinColumn(

            name = "owner_username",

            referencedColumnName = "username",

            foreignKey = @ForeignKey(name = "fk_moodboard_owner")

    )

    private @Nullable User owner;



    @Column(columnDefinition = "LONGTEXT")

    private @Nullable String content;



    @Column(name = "is_public", nullable = false)

    private boolean isPublic = false;



    @Column(length = 100)

    private @Nullable String name;



    @Lob

    @Column(columnDefinition = "LONGBLOB")

    private @Nullable byte[] thumbnail;



    public Moodboard(User owner, String content) {

        this.owner = owner;

        this.content = content;

    }



    public Moodboard(Long id, User owner, String content) {

        this.id = id;

        this.owner = owner;

        this.content = content;

    }



    public @Nullable String getOwnerUsername() {

        return owner != null ? owner.getUsername() : null;

    }



    @Override

    public String toString() {

        return "Moodboard{id=" + id + ", owner=" + getOwnerUsername() + "}";

    }

}


