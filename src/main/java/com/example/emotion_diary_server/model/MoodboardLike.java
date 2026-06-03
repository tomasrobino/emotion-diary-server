package com.example.emotion_diary_server.model;



import com.example.emotion_diary_server.user.User;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Entity;

import jakarta.persistence.FetchType;

import jakarta.persistence.ForeignKey;

import jakarta.persistence.GeneratedValue;

import jakarta.persistence.GenerationType;

import jakarta.persistence.Id;

import jakarta.persistence.JoinColumn;

import jakarta.persistence.ManyToOne;

import jakarta.persistence.Table;

import jakarta.persistence.UniqueConstraint;

import org.jspecify.annotations.Nullable;

import lombok.Getter;



/**
 * Records that a user liked a moodboard.
 * Each user may like a given moodboard at most once.
 */
@Getter

@Entity

@Table(

        name = "moodboard_likes",

        uniqueConstraints = @UniqueConstraint(columnNames = {"moodboard_id", "liker_username"})

)

public class MoodboardLike {



    /** Surrogate primary key. */
    @Id

    @GeneratedValue(strategy = GenerationType.IDENTITY)

    private @Nullable Long id;



    /** Moodboard that was liked. */
    @JsonIgnore

    @ManyToOne(optional = false, fetch = FetchType.LAZY)

    @JoinColumn(

            name = "moodboard_id",

            nullable = false,

            foreignKey = @ForeignKey(name = "fk_likes_moodboard")

    )

    private Moodboard moodboard;



    /** User who created the like. */
    @JsonIgnore

    @ManyToOne(optional = false, fetch = FetchType.LAZY)

    @JoinColumn(

            name = "liker_username",

            referencedColumnName = "username",

            nullable = false,

            foreignKey = @ForeignKey(name = "fk_likes_liker")

    )

    private User liker;



    /** JPA no-args constructor. */
    public MoodboardLike() {}



    /**
     * @param moodboard liked moodboard
     * @param liker     user performing the like
     */
    public MoodboardLike(Moodboard moodboard, User liker) {

        this.moodboard = moodboard;

        this.liker = liker;

    }



    /**
     * @return id of the liked moodboard
     */
    public Long getMoodboardId() {

        return moodboard.getId();

    }



    /**
     * @return username of the user who liked the moodboard
     */
    public String getLikerUsername() {

        return liker.getUsername();

    }

}

