package com.socialapp.application.dto.response;

import org.springframework.http.HttpStatus;

/**
 * Tập trung toàn bộ error codes của hệ thống.
 *
 * Quy tắc đặt tên: DOMAIN_REASON (SCREAMING_SNAKE_CASE)
 * FE dùng code để switch/case hiển thị message cụ thể,
 * thay vì parse string message (dễ vỡ khi backend thay đổi wording).
 *
 * Mỗi code gắn với 1 HTTP status cố định — GlobalExceptionHandler
 * dùng errorCode.getStatus() để set status code đúng.
 */
public enum ErrorCode {

    // ── 400 Bad Request ──────────────────────────────────────
    VALIDATION_ERROR        (HttpStatus.BAD_REQUEST),
    INVALID_CREDENTIALS     (HttpStatus.BAD_REQUEST),
    INVALID_FILE_TYPE       (HttpStatus.BAD_REQUEST),
    INVALID_FILE_SIZE       (HttpStatus.BAD_REQUEST),
    MALFORMED_REQUEST       (HttpStatus.BAD_REQUEST),

    // ── 401 Unauthorized ─────────────────────────────────────
    UNAUTHORIZED            (HttpStatus.UNAUTHORIZED),
    TOKEN_EXPIRED           (HttpStatus.UNAUTHORIZED),
    TOKEN_INVALID           (HttpStatus.UNAUTHORIZED),

    // ── 403 Forbidden ────────────────────────────────────────
    FORBIDDEN               (HttpStatus.FORBIDDEN),
    NOT_POST_AUTHOR         (HttpStatus.FORBIDDEN),
    NOT_COMMENT_AUTHOR      (HttpStatus.FORBIDDEN),

    // ── 404 Not Found ────────────────────────────────────────
    USER_NOT_FOUND          (HttpStatus.NOT_FOUND),
    POST_NOT_FOUND          (HttpStatus.NOT_FOUND),
    COMMENT_NOT_FOUND       (HttpStatus.NOT_FOUND),
    CHAT_NOT_FOUND          (HttpStatus.NOT_FOUND),
    MESSAGE_NOT_FOUND       (HttpStatus.NOT_FOUND),
    NOTIFICATION_NOT_FOUND  (HttpStatus.NOT_FOUND),

    // ── 409 Conflict — domain rule vi phạm ──────────────────
    ALREADY_LIKED           (HttpStatus.CONFLICT),
    NOT_LIKED_YET           (HttpStatus.CONFLICT),
    ALREADY_FRIENDS         (HttpStatus.CONFLICT),
    FRIEND_REQUEST_EXISTS   (HttpStatus.CONFLICT),
    NOT_FRIENDS             (HttpStatus.CONFLICT),
    USER_BLOCKED            (HttpStatus.CONFLICT),
    EMAIL_ALREADY_VERIFIED  (HttpStatus.CONFLICT),
    USERNAME_TAKEN          (HttpStatus.CONFLICT),
    USERNAME_COOLDOWN       (HttpStatus.CONFLICT),   // 60-day cooldown
    POST_SHARE_NOT_PUBLIC   (HttpStatus.CONFLICT),
    COMMENT_LIMIT_EXCEEDED  (HttpStatus.CONFLICT),
    FILE_LIMIT_EXCEEDED     (HttpStatus.CONFLICT),   // tối đa 10 files/post
    ACCOUNT_LOCKED          (HttpStatus.CONFLICT),
    ACCOUNT_NOT_VERIFIED    (HttpStatus.CONFLICT),
    MESSAGE_EDIT_EXPIRED    (HttpStatus.CONFLICT),   // quá 15 phút
    CALL_ALREADY_ACTIVE     (HttpStatus.CONFLICT),

    // ── 422 Unprocessable ────────────────────────────────────
    EMAIL_NOT_VERIFIED      (HttpStatus.UNPROCESSABLE_ENTITY),
    VERIFY_CODE_EXPIRED     (HttpStatus.UNPROCESSABLE_ENTITY),
    VERIFY_CODE_INVALID     (HttpStatus.UNPROCESSABLE_ENTITY),

    // ── 500 Internal Server Error ────────────────────────────
    INTERNAL_ERROR          (HttpStatus.INTERNAL_SERVER_ERROR),
    FILE_STORAGE_ERROR      (HttpStatus.INTERNAL_SERVER_ERROR),
    EMAIL_SEND_ERROR        (HttpStatus.INTERNAL_SERVER_ERROR);

    // ── ─────────────────────────────────────────────────────

    private final HttpStatus status;

    ErrorCode(HttpStatus status) {
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public int getStatusCode() {
        return status.value();
    }
}