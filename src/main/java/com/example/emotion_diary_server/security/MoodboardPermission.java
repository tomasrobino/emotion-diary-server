package com.example.emotion_diary_server.security;

import com.example.emotion_diary_server.model.Moodboard;
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
 * Represents an explicit permission grant for a moodboard.
 */
@Getter
@Entity
@Table(
        name = "moodboard_permissions",
        uniqueConstraints = @UniqueConstraint(columnNames = {"moodboard_id", "permitted_username"})
)
public class MoodboardPermission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private @Nullable Long id;

    @JsonIgnore
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(
            name = "moodboard_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_permissions_moodboard")
    )
    private Moodboard moodboard;

    @JsonIgnore
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(
            name = "owner_username",
            referencedColumnName = "username",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_permissions_owner")
    )
    private User owner;

    @JsonIgnore
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(
            name = "permitted_username",
            referencedColumnName = "username",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_permissions_permitted")
    )
    private User permitted;

    public MoodboardPermission() {}

    public MoodboardPermission(Moodboard moodboard, User owner, User permitted) {
        this.moodboard = moodboard;
        this.owner = owner;
        this.permitted = permitted;
    }

    public Long getMoodboardId() {
        return moodboard.getId();
    }

    public String getOwnerUsername() {
        return owner.getUsername();
    }

    public String getPermittedUsername() {
        return permitted.getUsername();
    }
}
