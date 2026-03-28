package com.socialapp.presentation.controller;

import com.socialapp.application.comment.dto.request.CommentRequestDtos.*;
import com.socialapp.application.comment.dto.response.CommentResponseDtos.*;
import com.socialapp.application.comment.usecase.*;
import com.socialapp.domain.account.repository.AccountRepository;
import com.socialapp.presentation.util.ApiResponse;
import com.socialapp.presentation.util.SecurityUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class CommentController {

    private final CreateCommentUseCase  createCommentUseCase;
    private final ReplyCommentUseCase   replyCommentUseCase;
    private final UpdateCommentUseCase  updateCommentUseCase;
    private final DeleteCommentUseCase  deleteCommentUseCase;
    private final LikeCommentUseCase    likeCommentUseCase;
    private final UnlikeCommentUseCase  unlikeCommentUseCase;
    private final AccountRepository     accountRepository;

    private String resolveUserId() {
        return accountRepository.findById(SecurityUtil.currentAccountId())
                .orElseThrow().getUserId();
    }

    /** POST /api/posts/{postId}/comments */
    @PostMapping(value = "/api/posts/{postId}/comments",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<CommentResponse> create(
            @PathVariable String postId,
            @RequestPart("data") @Valid CreateCommentRequest request,
            @RequestPart(value = "files", required = false) List<MultipartFile> files) {
        return ApiResponse.ok(
                createCommentUseCase.execute(resolveUserId(), postId, request, files));
    }

    /** POST /api/posts/{postId}/comments/{commentId}/replies */
    @PostMapping(value = "/api/posts/{postId}/comments/{commentId}/replies",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<CommentResponse> reply(
            @PathVariable String postId,
            @PathVariable String commentId,
            @RequestPart("data") @Valid ReplyCommentRequest request,
            @RequestPart(value = "files", required = false) List<MultipartFile> files) {
        return ApiResponse.ok(
                replyCommentUseCase.execute(resolveUserId(), postId, commentId, request, files));
    }

    /** PUT /api/comments/{commentId} */
    @PutMapping(value = "/api/comments/{commentId}",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<CommentResponse> update(
            @PathVariable String commentId,
            @RequestPart("data") @Valid UpdateCommentRequest request,
            @RequestPart(value = "files", required = false) List<MultipartFile> files) {
        return ApiResponse.ok(
                updateCommentUseCase.execute(resolveUserId(), commentId, request, files));
    }

    /** DELETE /api/comments/{commentId} */
    @DeleteMapping("/api/comments/{commentId}")
    public ApiResponse<Void> delete(@PathVariable String commentId) {
        boolean isAdmin = SecurityUtil.hasRole("ADMIN");
        var res = deleteCommentUseCase.execute(resolveUserId(), commentId, isAdmin);
        return ApiResponse.ok(res.message());
    }

    /** POST /api/comments/{commentId}/like */
    @PostMapping("/api/comments/{commentId}/like")
    public ApiResponse<Void> like(@PathVariable String commentId) {
        var res = likeCommentUseCase.execute(resolveUserId(), commentId);
        return ApiResponse.ok(res.message());
    }

    /** DELETE /api/comments/{commentId}/like */
    @DeleteMapping("/api/comments/{commentId}/like")
    public ApiResponse<Void> unlike(@PathVariable String commentId) {
        var res = unlikeCommentUseCase.execute(resolveUserId(), commentId);
        return ApiResponse.ok(res.message());
    }
}
