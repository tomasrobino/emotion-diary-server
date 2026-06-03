package com.example.emotion_diary_server.dto;

import com.example.emotion_diary_server.model.Moodboard;
import org.jspecify.annotations.Nullable;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Public moodboard entry shown in the discovery feed.
 */
@Getter
@Setter
@NoArgsConstructor
public class PublicMoodboardFeedItemDto {

    /** Unique moodboard identifier. */
    private @Nullable Long id;

    /** Username of the moodboard owner. */
    private @Nullable String ownerUsername;

    /** Display name resolved from persistence. */
    private @Nullable String name;

    /** Whether a thumbnail image is stored. */
    private boolean hasThumbnail;

    /** Total number of likes. */
    private long likeCount;

    /** Optional full canvas content when the feed includes it. */
    private @Nullable MoodboardContentDto content;

    /**
     * Builds a feed item from a moodboard, like count, and optional content.
     *
     * @param moodboard persisted public moodboard
     * @param likeCount current like count
     * @param content   optional canvas content; may be {@code null}
     * @return populated feed item DTO
     */
    public static PublicMoodboardFeedItemDto from(
            Moodboard moodboard,
            long likeCount,
            @Nullable MoodboardContentDto content
    ) {
        PublicMoodboardFeedItemDto dto = new PublicMoodboardFeedItemDto();
        dto.setId(moodboard.getId());
        dto.setOwnerUsername(moodboard.getOwnerUsername());
        dto.setName(MoodboardResponseDto.resolveDisplayName(moodboard.getName()));
        dto.setHasThumbnail(moodboard.getThumbnail() != null && moodboard.getThumbnail().length > 0);
        dto.setLikeCount(likeCount);
        dto.setContent(content);
        return dto;
    }

    /**
     * Builds a feed item without canvas content and with a like count of zero.
     *
     * @param moodboard persisted public moodboard
     * @return populated feed item DTO
     */
    public static PublicMoodboardFeedItemDto from(Moodboard moodboard) {
        return from(moodboard, 0L, null);
    }
}
