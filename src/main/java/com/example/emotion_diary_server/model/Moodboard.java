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



/**
 * User-owned collage of emotional content, optionally shared publicly.
 */
@Setter

@Getter

@NoArgsConstructor

@Entity

public class Moodboard {

    /** Surrogate primary key. */
    @Id

    @GeneratedValue(strategy = jakarta.persistence.GenerationType.IDENTITY)

    private @Nullable Long id;



    /** User who owns this moodboard. */
    @JsonIgnore

    @ManyToOne(fetch = FetchType.LAZY)

    @JoinColumn(

            name = "owner_username",

            referencedColumnName = "username",

            foreignKey = @ForeignKey(name = "fk_moodboard_owner")

    )

    private @Nullable User owner;



    /** Serialized moodboard payload (JSON). */
    @Column(columnDefinition = "LONGTEXT")

    private @Nullable String content;



    /** When {@code true}, other users may view this moodboard without an explicit grant. */
    @Column(name = "is_public", nullable = false)

    private boolean isPublic = false;



    /** Optional display name shown in listings. */
    @Column(length = 100)

    private @Nullable String name;



    /** Optional preview image bytes. */
    @Lob

    @Column(columnDefinition = "LONGBLOB")

    private @Nullable byte[] thumbnail;



    /**
     * @param owner   moodboard owner
     * @param content initial serialized content
     */
    public Moodboard(User owner, String content) {

        this.owner = owner;

        this.content = content;

    }



    /**
     * @param id      existing primary key (e.g. when rehydrating)
     * @param owner   moodboard owner
     * @param content serialized content
     */
    public Moodboard(Long id, User owner, String content) {

        this.id = id;

        this.owner = owner;

        this.content = content;

    }



    /**
     * @return owner username, or {@code null} if {@link #owner} is not loaded
     */
    public @Nullable String getOwnerUsername() {

        return owner != null ? owner.getUsername() : null;

    }



    @Override

    public String toString() {

        return "Moodboard{id=" + id + ", owner=" + getOwnerUsername() + "}";

    }

}

