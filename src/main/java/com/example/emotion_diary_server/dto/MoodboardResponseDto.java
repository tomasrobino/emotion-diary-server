package com.example.emotion_diary_server.dto;

import com.example.emotion_diary_server.model.Moodboard;
import org.jspecify.annotations.Nullable;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Moodboard summary and content returned by the API.
 */
@Getter
@Setter
@NoArgsConstructor
public class MoodboardResponseDto {
    /** Display name used when the stored name is null or blank. */
    public static final String DEFAULT_NAME = "Sin título";

    /** Unique moodboard identifier. */
    private @Nullable Long id;

    /** Username of the moodboard owner. */
    private @Nullable String ownerUsername;

    /** Whether the moodboard is visible on the public feed. */
    private boolean isPublic;

    /** Display name; falls back to {@link #DEFAULT_NAME} when unset. */
    private @Nullable String name;

    /** Canvas layout and elements. */
    private @Nullable MoodboardContentDto content;

    /** Whether a thumbnail image is stored for this moodboard. */
    private boolean hasThumbnail;

    /** Total number of likes on this moodboard. */
    private long likeCount;

    /**
     * Builds a response DTO from a moodboard entity, content, and like count.
     *
     * @param moodboard persisted moodboard
     * @param content   parsed canvas content
     * @param likeCount current like count
     * @return populated response DTO
     */
    public static MoodboardResponseDto from(Moodboard moodboard, MoodboardContentDto content, long likeCount) {
        MoodboardResponseDto dto = new MoodboardResponseDto();
        dto.setId(moodboard.getId());
        dto.setOwnerUsername(moodboard.getOwnerUsername());
        dto.setPublic(moodboard.isPublic());
        dto.setName(resolveDisplayName(moodboard.getName()));
        dto.setContent(content);
        dto.setHasThumbnail(
                moodboard.getThumbnail() != null && moodboard.getThumbnail().length > 0
        );
        dto.setLikeCount(likeCount);
        return dto;
    }

    /**
     * Builds a response DTO with a like count of zero.
     *
     * @param moodboard persisted moodboard
     * @param content   parsed canvas content
     * @return populated response DTO
     */
    public static MoodboardResponseDto from(Moodboard moodboard, MoodboardContentDto content) {
        return from(moodboard, content, 0L);
    }

    /**
     * Returns a trimmed display name or the default when the stored name is missing.
     *
     * @param name raw name from persistence; may be {@code null}
     * @return non-blank display name
     */
    public static String resolveDisplayName(@Nullable String name) {
        if (name == null || name.isBlank()) {
            return DEFAULT_NAME;
        }
        return name.trim();
    }
}
