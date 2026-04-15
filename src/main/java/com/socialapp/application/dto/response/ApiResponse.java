package com.socialapp.application.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Wrapper chuẩn cho mọi API response trong hệ thống.
 *
 * Cấu trúc:
 * {
 *   "success": true,
 *   "data": { ... },          // null khi error
 *   "error": null             // null khi success
 * }
 *
 * FE check: if (res.success) use(res.data) else show(res.error.message)
 *
 * @param <T> kiểu data payload — dùng Void cho các action không có data trả về
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiResponse<T>(
        boolean success,
        T       data,
        Error   error
) {

    // ── Success factories ────────────────────────────────────

    /** Trả data kèm success=true */
    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(true, data, null);
    }

    /** Dùng cho action không có data trả về (like, follow, delete...) */
    public static ApiResponse<Void> ok() {
        return new ApiResponse<>(true, null, null);
    }

    // ── Error factories ──────────────────────────────────────

    public static <T> ApiResponse<T> error(ErrorCode code, String message) {
        return new ApiResponse<>(false, null, new Error(code.name(), message));
    }

    public static <T> ApiResponse<T> error(ErrorCode code, String message,
                                           java.util.Map<String, String> fieldErrors) {
        return new ApiResponse<>(false, null, new Error(code.name(), message, fieldErrors));
    }

    // ── Nested error object ──────────────────────────────────

    /**
     * Error payload.
     * fieldErrors chỉ xuất hiện khi validation thất bại (400),
     * null ở các lỗi khác — @JsonInclude loại bỏ khỏi JSON.
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record Error(
            String                      code,         // e.g. "VALIDATION_ERROR"
            String                      message,      // human-readable, dùng được để toast
            java.util.Map<String, String> fieldErrors // chỉ có khi 400 validation
    ) {
        /** Constructor không có fieldErrors */
        public Error(String code, String message) {
            this(code, message, null);
        }
    }
}