package com.example.emotion_diary_server.dto;

import org.jspecify.annotations.Nullable;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class LikedMoodboardSummaryDto {
    private @Nullable Long id;
    private @Nullable String ownerUsername;
    private @Nullable String name;

    public static LikedMoodboardSummaryDto from(com.example.emotion_diary_server.model.Moodboard moodboard) {
        LikedMoodboardSummaryDto dto = new LikedMoodboardSummaryDto();
        dto.setId(moodboard.getId());
        dto.setOwnerUsername(moodboard.getOwnerUsername());
        dto.setName(MoodboardResponseDto.resolveDisplayName(moodboard.getName()));
        return dto;
    }
}
