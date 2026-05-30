package com.example.emotion_diary_server.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class PublicMoodboardsPageDto {
    private List<PublicMoodboardFeedItemDto> items;
    private int page;
    private int size;
    private long totalItems;
    private int totalPages;
    private boolean hasNext;

    public PublicMoodboardsPageDto(
            List<PublicMoodboardFeedItemDto> items,
            int page,
            int size,
            long totalItems,
            int totalPages,
            boolean hasNext
    ) {
        this.items = items;
        this.page = page;
        this.size = size;
        this.totalItems = totalItems;
        this.totalPages = totalPages;
        this.hasNext = hasNext;
    }
}
