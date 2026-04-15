package com.socialapp.presentation.advice;

import com.socialapp.application.dto.response.ApiResponse;
import com.socialapp.application.dto.response.ErrorCode;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.util.Map;
import java.util.stream.Collectors;

/**
 * Centralized exception handler — tất cả response theo ApiResponse<T> format.
 *
 * Thứ tự handler: từ specific → generic.
 *
 * Mapping:
 *   DomainException                   → code từ exception, status từ ErrorCode enum
 *   MethodArgumentNotValidException   → 400 VALIDATION_ERROR  + fieldErrors map
 *   MissingServletRequestParameter    → 400 MALFORMED_REQUEST
 *   MaxUploadSizeExceededException    → 400 INVALID_FILE_SIZE
 *   AuthenticationException           → 401 UNAUTHORIZED
 *   AccessDeniedException             → 403 FORBIDDEN
 *   IllegalArgumentException          → 404 (fallback — nên dùng DomainException)
 *   IllegalStateException             → 409 (fallback — nên dùng DomainException)
 *   RuntimeException (catch-all)      → 500 INTERNAL_ERROR
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    // ── Custom domain exception ──────────────────────────────
    @ExceptionHandler(DomainException.class)
    public ResponseEntity<ApiResponse<Void>> handleDomain(DomainException ex) {
        return ResponseEntity
                .status(ex.getErrorCode().getStatus())
                .body(ApiResponse.error(ex.getErrorCode(), ex.getMessage()));
    }

    // ── 400 — Validation (@Valid) ────────────────────────────
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidation(
            MethodArgumentNotValidException ex) {

        Map<String, String> fieldErrors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        fe -> fe.getDefaultMessage() != null
                                ? fe.getDefaultMessage() : "Invalid value",
                        (first, second) -> first
                ));

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(
                        ErrorCode.VALIDATION_ERROR,
                        "Request validation failed",
                        fieldErrors
                ));
    }

    // ── 400 — Missing query param ────────────────────────────
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiResponse<Void>> handleMissingParam(
            MissingServletRequestParameterException ex) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(
                        ErrorCode.MALFORMED_REQUEST,
                        "Missing required parameter: " + ex.getParameterName()
                ));
    }

    // ── 400 — File quá lớn ───────────────────────────────────
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ApiResponse<Void>> handleFileTooLarge(
            MaxUploadSizeExceededException ex) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(
                        ErrorCode.INVALID_FILE_SIZE,
                        "File size exceeds the maximum allowed limit"
                ));
    }

    // ── 401 — Chưa xác thực ─────────────────────────────────
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiResponse<Void>> handleAuthentication(
            AuthenticationException ex) {
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error(ErrorCode.UNAUTHORIZED, "Authentication required"));
    }

    // ── 403 — Không có quyền ────────────────────────────────
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> handleAccessDenied(
            AccessDeniedException ex) {
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.error(
                        ErrorCode.FORBIDDEN,
                        "You do not have permission to perform this action"
                ));
    }

    // ── 404 — Fallback (nên dùng DomainException thay thế) ──
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> handleNotFound(IllegalArgumentException ex) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(ErrorCode.USER_NOT_FOUND, ex.getMessage()));
    }

    // ── 409 — Fallback (nên dùng DomainException thay thế) ──
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ApiResponse<Void>> handleConflict(IllegalStateException ex) {
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(ApiResponse.error(ErrorCode.ALREADY_LIKED, ex.getMessage()));
    }

    // ── 500 — Lỗi không mong đợi ────────────────────────────
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiResponse<Void>> handleGeneral(RuntimeException ex) {
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(
                        ErrorCode.INTERNAL_ERROR,
                        "An unexpected error occurred. Please try again later."
                ));
    }
}