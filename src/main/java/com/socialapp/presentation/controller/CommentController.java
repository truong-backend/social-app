package com.socialapp.presentation.controller;

import com.socialapp.application.dto.request.CreateCommentRequest;
import com.socialapp.application.dto.request.EditCommentRequest;
import com.socialapp.application.dto.response.ApiResponse;
import com.socialapp.application.dto.response.CommentResponse;
import com.socialapp.application.usecase.comment.AddCommentUseCase;
import com.socialapp.application.usecase.comment.AttachFileToCommentUseCase;
import com.socialapp.application.usecase.comment.DeleteCommentUseCase;
import com.socialapp.application.usecase.comment.EditCommentUseCase;
import com.socialapp.application.usecase.comment.LikeCommentUseCase;
import com.socialapp.application.usecase.comment.ReplyCommentUseCase;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * REST Controller — Comments
 *
 * POST   /api/posts/{postId}/comments                               — Thêm bình luận
 * PUT    /api/posts/{postId}/comments/{commentId}                   — Chỉnh sửa bình luận
 * DELETE /api/posts/{postId}/comments/{commentId}                   — Xóa bình luận
 * POST   /api/posts/{postId}/comments/{commentId}/files             — Đính kèm file vào bình luận
 * POST   /api/posts/{postId}/comments/{commentId}/like              — Thích bình luận
 * DELETE /api/posts/{postId}/comments/{commentId}/like              — Bỏ thích bình luận
 * POST   /api/posts/{postId}/comments/{commentId}/replies           — Trả lời bình luận
 *
 * Domain rules:
 *   - Chỉ author mới được sửa / xóa bình luận của mình
 *   - Mỗi bình luận chỉ đính kèm được 1 file (overwrite nếu gọi lại)
 */
@RestController
@RequestMapping("/api/posts/{postId}/comments")
public class CommentController {

    private final AddCommentUseCase          addCommentUseCase;
    private final EditCommentUseCase         editCommentUseCase;
    private final DeleteCommentUseCase       deleteCommentUseCase;
    private final AttachFileToCommentUseCase attachFileToCommentUseCase;
    private final LikeCommentUseCase         likeCommentUseCase;
    private final ReplyCommentUseCase        replyCommentUseCase;

    public CommentController(AddCommentUseCase addCommentUseCase,
                             EditCommentUseCase editCommentUseCase,
                             DeleteCommentUseCase deleteCommentUseCase,
                             AttachFileToCommentUseCase attachFileToCommentUseCase,
                             LikeCommentUseCase likeCommentUseCase,
                             ReplyCommentUseCase replyCommentUseCase) {
        this.addCommentUseCase          = addCommentUseCase;
        this.editCommentUseCase         = editCommentUseCase;
        this.deleteCommentUseCase       = deleteCommentUseCase;
        this.attachFileToCommentUseCase = attachFileToCommentUseCase;
        this.likeCommentUseCase         = likeCommentUseCase;
        this.replyCommentUseCase        = replyCommentUseCase;
    }

    // POST /api/posts/{postId}/comments
    @PostMapping
    public ResponseEntity<ApiResponse<CommentResponse>> addComment(
            @AuthenticationPrincipal String userId,
            @PathVariable String postId,
            @Valid @RequestBody CreateCommentRequest req) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.ok(addCommentUseCase.execute(userId, postId, req)));
    }

    // PUT /api/posts/{postId}/comments/{commentId}
    @PutMapping("/{commentId}")
    public ResponseEntity<ApiResponse<CommentResponse>> editComment(
            @AuthenticationPrincipal String userId,
            @PathVariable String postId,
            @PathVariable String commentId,
            @Valid @RequestBody EditCommentRequest req) {
        return ResponseEntity.ok(ApiResponse.ok(
                editCommentUseCase.execute(userId, postId, commentId, req)));
    }

    // DELETE /api/posts/{postId}/comments/{commentId}
    @DeleteMapping("/{commentId}")
    public ResponseEntity<ApiResponse<Void>> deleteComment(
            @AuthenticationPrincipal String userId,
            @PathVariable String postId,
            @PathVariable String commentId) {
        deleteCommentUseCase.execute(userId, postId, commentId);
        return ResponseEntity.ok(ApiResponse.ok());
    }

    // POST /api/posts/{postId}/comments/{commentId}/files
    @PostMapping(value = "/{commentId}/files", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<CommentResponse>> attachFile(
            @AuthenticationPrincipal String userId,
            @PathVariable String postId,
            @PathVariable String commentId,
            @RequestPart("file") MultipartFile file) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.ok(
                        attachFileToCommentUseCase.execute(userId, postId, commentId, file)));
    }

    // POST /api/posts/{postId}/comments/{commentId}/like
    @PostMapping("/{commentId}/like")
    public ResponseEntity<ApiResponse<Void>> likeComment(
            @AuthenticationPrincipal String userId,
            @PathVariable String postId,
            @PathVariable String commentId) {
        likeCommentUseCase.like(userId, postId, commentId);
        return ResponseEntity.ok(ApiResponse.ok());
    }

    // DELETE /api/posts/{postId}/comments/{commentId}/like
    @DeleteMapping("/{commentId}/like")
    public ResponseEntity<ApiResponse<Void>> unlikeComment(
            @AuthenticationPrincipal String userId,
            @PathVariable String postId,
            @PathVariable String commentId) {
        likeCommentUseCase.unlike(userId, postId, commentId);
        return ResponseEntity.ok(ApiResponse.ok());
    }

    // POST /api/posts/{postId}/comments/{commentId}/replies
    @PostMapping("/{commentId}/replies")
    public ResponseEntity<ApiResponse<CommentResponse>> replyComment(
            @AuthenticationPrincipal String userId,
            @PathVariable String postId,
            @PathVariable String commentId,
            @Valid @RequestBody CreateCommentRequest req) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.ok(
                        replyCommentUseCase.execute(userId, postId, commentId, req)));
    }
}
