package com.socialapp.presentation.advice;

import com.socialapp.application.dto.response.ErrorCode;

/**
 * Exception chuẩn cho mọi domain/business rule violation.
 *
 * Thay thế việc ném IllegalArgumentException / IllegalStateException
 * với message string — giờ có structured ErrorCode để FE switch/case.
 *
 * Cách dùng trong domain service hoặc use case:
 *
 *   // Entity not found
 *   throw new DomainException(ErrorCode.POST_NOT_FOUND,
 *       "Post not found: " + postId);
 *
 *   // Business rule vi phạm
 *   throw new DomainException(ErrorCode.USERNAME_COOLDOWN,
 *       "Username có thể đổi lại sau 60 ngày");
 *
 *   // Ownership check
 *   throw new DomainException(ErrorCode.NOT_COMMENT_AUTHOR,
 *       "Only the comment author can edit this comment");
 *
 * GlobalExceptionHandler sẽ map tự động sang đúng HTTP status
 * thông qua ErrorCode.getStatus().
 */
public class DomainException extends RuntimeException {

    private final ErrorCode errorCode;

    public DomainException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }
}