package com.example.emotion_diary_server.dto;

import com.example.emotion_diary_server.model.Moodboard;
import org.jspecify.annotations.Nullable;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class MoodboardResponseDto {
    public static final String DEFAULT_NAME = "Sin título";

    private @Nullable Long id;
    private @Nullable String ownerUsername;
    private boolean isPublic;
    private @Nullable String name;
    private @Nullable MoodboardContentDto content;
    private boolean hasThumbnail;
    private long likeCount;

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

    public static MoodboardResponseDto from(Moodboard moodboard, MoodboardContentDto content) {
        return from(moodboard, content, 0L);
    }

    public static String resolveDisplayName(@Nullable String name) {
        if (name == null || name.isBlank()) {
            return DEFAULT_NAME;
        }
        return name.trim();
    }
}
