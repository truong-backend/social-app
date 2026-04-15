package com.socialapp.application.dto.request;

public record PageRequest(
        int page,   // 0-based
        int size
) {
    public PageRequest {
        if (page < 0)  page = 0;
        if (size <= 0) size = 10;
        if (size > 100) size = 100;
    }

    public int offset() {
        return page * size;
    }
}