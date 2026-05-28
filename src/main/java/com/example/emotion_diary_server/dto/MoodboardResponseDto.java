package com.example.emotion_diary_server.dto;

import com.example.emotion_diary_server.model.Moodboard;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class MoodboardResponseDto {
    private Long id;
    private String ownerUsername;
    private boolean isPublic;
    private MoodboardContentDto content;

    public static MoodboardResponseDto from(Moodboard moodboard, MoodboardContentDto content) {
        MoodboardResponseDto dto = new MoodboardResponseDto();
        dto.setId(moodboard.getId());
        dto.setOwnerUsername(moodboard.getOwnerUsername());
        dto.setPublic(moodboard.isPublic());
        dto.setContent(content);
        return dto;
    }
}
