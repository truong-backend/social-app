package com.socialapp.presentation.advice;

import com.socialapp.application.shared.exception.*;
import com.socialapp.domain.account.entity.AccountDomainException;
import com.socialapp.domain.comment.entity.CommentDomainException;
import com.socialapp.domain.message.entity.MessageDomainException;
import com.socialapp.domain.post.entity.PostDomainException;
import com.socialapp.domain.relationship.entity.RelationshipDomainException;
import com.socialapp.domain.user.entity.UserDomainException;
import com.socialapp.presentation.util.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // ── Domain exceptions → 400 ───────────────────────────────

    @ExceptionHandler({
            AccountDomainException.class,
            UserDomainException.class,
            PostDomainException.class,
            CommentDomainException.class,
            MessageDomainException.class,
            RelationshipDomainException.class
    })
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<Void> handleDomainException(RuntimeException ex) {
        return ApiResponse.error(ex.getMessage());
    }

    // ── Validation → 400 ─────────────────────────────────────

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<Void> handleValidation(MethodArgumentNotValidException ex) {
        String msg = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> e.getField() + ": " + e.getDefaultMessage())
                .collect(Collectors.joining(", "));
        return ApiResponse.error(msg);
    }

    // ── 404 ───────────────────────────────────────────────────

    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiResponse<Void> handleNotFound(ResourceNotFoundException ex) {
        return ApiResponse.error(ex.getMessage());
    }

    // ── 403 ───────────────────────────────────────────────────

    @ExceptionHandler(ForbiddenException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ApiResponse<Void> handleForbidden(ForbiddenException ex) {
        return ApiResponse.error(ex.getMessage());
    }

    // ── 409 ───────────────────────────────────────────────────

    @ExceptionHandler(ConflictException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiResponse<Void> handleConflict(ConflictException ex) {
        return ApiResponse.error(ex.getMessage());
    }

    // ── 500 ───────────────────────────────────────────────────

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiResponse<Void> handleGeneral(Exception ex) {
        log.error("Unhandled exception", ex);
        return ApiResponse.error("Internal server error");
    }
}