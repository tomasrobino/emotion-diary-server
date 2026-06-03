package com.example.emotion_diary_server.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * Paginated list of moodboards owned by or visible to the caller.
 */
@Getter
@Setter
@NoArgsConstructor
public class MoodboardsPageDto {

    /** Moodboards on the current page. */
    private List<MoodboardResponseDto> items;

    /** Zero-based page index. */
    private int page;

    /** Maximum number of items per page. */
    private int size;

    /** Total moodboards matching the query across all pages. */
    private long totalItems;

    /** Total number of pages available. */
    private int totalPages;

    /** Whether another page exists after the current one. */
    private boolean hasNext;

    /**
     * @param items      moodboards on this page
     * @param page       zero-based page index
     * @param size       page size
     * @param totalItems total matching items
     * @param totalPages total pages
     * @param hasNext    whether a next page exists
     */
    public MoodboardsPageDto(
            List<MoodboardResponseDto> items,
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
