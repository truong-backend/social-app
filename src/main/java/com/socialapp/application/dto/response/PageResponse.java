package com.socialapp.application.dto.response;

import java.util.List;

public record PageResponse<T>(
        List<T> content,
        int     page,
        int     size,
        boolean hasNext
) {
    public static <T> PageResponse<T> of(List<T> content, int page, int size) {
        return new PageResponse<>(content, page, size, content.size() == size);
    }
}