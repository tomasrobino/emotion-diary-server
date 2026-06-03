package com.example.emotion_diary_server.dto;

import com.example.emotion_diary_server.model.Moodboard;
import org.jspecify.annotations.Nullable;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Compact summary of a moodboard the current user has liked.
 */
@Getter
@Setter
@NoArgsConstructor
public class LikedMoodboardSummaryDto {

    /** Unique moodboard identifier. */
    private @Nullable Long id;

    /** Username of the moodboard owner. */
    private @Nullable String ownerUsername;

    /** Display name resolved from persistence. */
    private @Nullable String name;

    /** Whether a thumbnail image is stored. */
    private boolean hasThumbnail;

    /** Total number of likes on the moodboard. */
    private long likeCount;

    /**
     * Builds a liked-moodboard summary with an explicit like count.
     *
     * @param moodboard persisted moodboard
     * @param likeCount current like count
     * @return populated summary DTO
     */
    public static LikedMoodboardSummaryDto from(Moodboard moodboard, long likeCount) {
        LikedMoodboardSummaryDto dto = new LikedMoodboardSummaryDto();
        dto.setId(moodboard.getId());
        dto.setOwnerUsername(moodboard.getOwnerUsername());
        dto.setName(MoodboardResponseDto.resolveDisplayName(moodboard.getName()));
        dto.setHasThumbnail(moodboard.getThumbnail() != null && moodboard.getThumbnail().length > 0);
        dto.setLikeCount(likeCount);
        return dto;
    }

    /**
     * Builds a liked-moodboard summary with a like count of zero.
     *
     * @param moodboard persisted moodboard
     * @return populated summary DTO
     */
    public static LikedMoodboardSummaryDto from(Moodboard moodboard) {
        return from(moodboard, 0L);
    }
}
