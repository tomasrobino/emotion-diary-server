package com.example.emotion_diary_server.dto;

import com.example.emotion_diary_server.model.Moodboard;
import org.jspecify.annotations.Nullable;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class PublicMoodboardFeedItemDto {
    private @Nullable Long id;
    private @Nullable String ownerUsername;
    private @Nullable String name;
    private boolean hasThumbnail;

    public static PublicMoodboardFeedItemDto from(Moodboard moodboard) {
        PublicMoodboardFeedItemDto dto = new PublicMoodboardFeedItemDto();
        dto.setId(moodboard.getId());
        dto.setOwnerUsername(moodboard.getOwnerUsername());
        dto.setName(MoodboardResponseDto.resolveDisplayName(moodboard.getName()));
        dto.setHasThumbnail(moodboard.getThumbnail() != null && moodboard.getThumbnail().length > 0);
        return dto;
    }
}
