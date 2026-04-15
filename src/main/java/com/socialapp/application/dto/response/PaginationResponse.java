package com.socialapp.application.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

/**
 * Pagination wrapper tối ưu cho feed (post/comment/notification).
 *
 * Hỗ trợ 2 chế độ:
 *
 *  1. OFFSET-based  — dùng cho trang admin, search (cần nhảy trang)
 *     PaginationResponse.ofOffset(content, page, size, totalElements)
 *     → FE dùng: page, size, totalPages, totalElements
 *
 *  2. CURSOR-based  — dùng cho feed, comment, notification (infinite scroll)
 *     PaginationResponse.ofCursor(content, nextCursor)
 *     → FE dùng: nextCursor (null = hết data), fetch tiếp bằng ?cursor=xxx
 *
 * JSON trả về:
 * {
 *   "items":         [...],
 *   "hasNext":       true,
 *   "nextCursor":    "2024-01-15T10:30:00",  // chỉ có khi cursor mode
 *   "page":          0,                       // chỉ có khi offset mode
 *   "size":          20,
 *   "totalPages":    5,                       // chỉ có khi offset mode
 *   "totalElements": 98                       // chỉ có khi offset mode
 * }
 *
 * Lý do dùng "items" thay vì "content":
 *   - Ngắn hơn, FE viết res.data.items thay vì res.data.content
 *   - Không bị nhầm với Spring Page "content"
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record PaginationResponse<T>(
        List<T>  items,
        boolean  hasNext,
        String   nextCursor,    // cursor mode only
        Integer  page,          // offset mode only
        Integer  size,
        Integer  totalPages,    // offset mode only
        Long     totalElements  // offset mode only
) {

    // ── Cursor-based (feed, comment, notification) ───────────

    /**
     * @param content    danh sách items
     * @param nextCursor ISO timestamp hoặc ID của item cuối — null nếu hết data
     */
    public static <T> PaginationResponse<T> ofCursor(List<T> content, String nextCursor) {
        return new PaginationResponse<>(
                content,
                nextCursor != null,
                nextCursor,
                null, null, null, null
        );
    }

    // ── Offset-based (admin list, search) ────────────────────

    /**
     * @param content       danh sách items trang hiện tại
     * @param page          trang hiện tại (0-based)
     * @param size          số item mỗi trang
     * @param totalElements tổng số items (dùng COUNT query)
     */
    public static <T> PaginationResponse<T> ofOffset(List<T> content,
                                                     int page,
                                                     int size,
                                                     long totalElements) {
        int totalPages = size > 0 ? (int) Math.ceil((double) totalElements / size) : 0;
        return new PaginationResponse<>(
                content,
                page < totalPages - 1,
                null,
                page,
                size,
                totalPages,
                totalElements
        );
    }

    /**
     * Offset-based không cần total count (tránh COUNT query đắt tiền).
     * hasNext = content.size() == size (nếu đủ items thì còn trang tiếp).
     */
    public static <T> PaginationResponse<T> ofOffset(List<T> content, int page, int size) {
        return new PaginationResponse<>(
                content,
                content.size() == size,
                null,
                page,
                size,
                null,
                null
        );
    }
}