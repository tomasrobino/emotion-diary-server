package com.example.emotion_diary_server.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class MediaUploadResponseDto {
    private final Long assetId;
    private final String contentType;
    private final long sizeBytes;
}
