package com.example.emotion_diary_server.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Response returned after a successful media asset upload.
 */
@Getter
@AllArgsConstructor
public class MediaUploadResponseDto {

    /** Identifier of the stored media asset. */
    private final Long assetId;

    /** MIME type of the uploaded file (e.g. {@code image/png}). */
    private final String contentType;

    /** Size of the uploaded file in bytes. */
    private final long sizeBytes;
}
