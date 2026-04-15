package com.socialapp.presentation.controller;

import com.socialapp.application.dto.request.CreatePostRequest;
import com.socialapp.application.dto.request.EditPostRequest;
import com.socialapp.application.dto.request.PageRequest;
import com.socialapp.application.dto.response.ApiResponse;
import com.socialapp.application.dto.response.PageResponse;
import com.socialapp.application.dto.response.PostResponse;
import com.socialapp.application.usecase.post.AttachFileToPostUseCase;
import com.socialapp.application.usecase.post.CreatePostUseCase;
import com.socialapp.application.usecase.post.DeletePostUseCase;
import com.socialapp.application.usecase.post.EditPostUseCase;
import com.socialapp.application.usecase.post.GetFeedUseCase;
import com.socialapp.application.usecase.post.GetUserPostsUseCase;
import com.socialapp.application.usecase.post.LikePostUseCase;
import com.socialapp.application.usecase.post.SearchPostUseCase;
import com.socialapp.application.usecase.post.SharePostUseCase;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * REST Controller — Posts
 *
 * POST   /api/posts                                   — Tạo bài viết mới
 * PUT    /api/posts/{postId}                          — Chỉnh sửa bài viết
 * DELETE /api/posts/{postId}                          — Xóa mềm bài viết
 * POST   /api/posts/{postId}/files                    — Đính kèm file vào bài viết
 * POST   /api/posts/{postId}/like                     — Thích bài viết
 * DELETE /api/posts/{postId}/like                     — Bỏ thích bài viết
 * POST   /api/posts/{postId}/share                    — Chia sẻ bài viết
 * GET    /api/posts/feed?page=&size=                  — Newsfeed của người dùng
 * GET    /api/posts/users/{userId}?page=&size=        — Bài viết của một user cụ thể
 * GET    /api/posts/search?q=&page=&size=             — Tìm kiếm bài viết theo keyword
 *
 * Domain rules (enforced in PostDomainService):
 *   - Chỉ author mới được chỉnh sửa / xóa bài viết
 *   - Xóa là soft-delete (deletedAt được set, bài vẫn còn trong DB)
 */
@RestController
@RequestMapping("/api/posts")
public class PostController {

    private final CreatePostUseCase       createPostUseCase;
    private final EditPostUseCase         editPostUseCase;
    private final DeletePostUseCase       deletePostUseCase;
    private final AttachFileToPostUseCase attachFileToPostUseCase;
    private final LikePostUseCase         likePostUseCase;
    private final SharePostUseCase        sharePostUseCase;
    private final GetFeedUseCase          getFeedUseCase;
    private final GetUserPostsUseCase     getUserPostsUseCase;
    private final SearchPostUseCase       searchPostUseCase;

    public PostController(CreatePostUseCase createPostUseCase,
                          EditPostUseCase editPostUseCase,
                          DeletePostUseCase deletePostUseCase,
                          AttachFileToPostUseCase attachFileToPostUseCase,
                          LikePostUseCase likePostUseCase,
                          SharePostUseCase sharePostUseCase,
                          GetFeedUseCase getFeedUseCase,
                          GetUserPostsUseCase getUserPostsUseCase,
                          SearchPostUseCase searchPostUseCase) {
        this.createPostUseCase       = createPostUseCase;
        this.editPostUseCase         = editPostUseCase;
        this.deletePostUseCase       = deletePostUseCase;
        this.attachFileToPostUseCase = attachFileToPostUseCase;
        this.likePostUseCase         = likePostUseCase;
        this.sharePostUseCase        = sharePostUseCase;
        this.getFeedUseCase          = getFeedUseCase;
        this.getUserPostsUseCase     = getUserPostsUseCase;
        this.searchPostUseCase       = searchPostUseCase;
    }

    // POST /api/posts
    @PostMapping
    public ResponseEntity<ApiResponse<PostResponse>> createPost(
            @AuthenticationPrincipal String userId,
            @Valid @RequestBody CreatePostRequest req) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.ok(createPostUseCase.execute(userId, req)));
    }

    // PUT /api/posts/{postId}
    @PutMapping("/{postId}")
    public ResponseEntity<ApiResponse<PostResponse>> editPost(
            @AuthenticationPrincipal String userId,
            @PathVariable String postId,
            @Valid @RequestBody EditPostRequest req) {
        return ResponseEntity.ok(ApiResponse.ok(editPostUseCase.execute(userId, postId, req)));
    }

    // DELETE /api/posts/{postId}
    @DeleteMapping("/{postId}")
    public ResponseEntity<ApiResponse<Void>> deletePost(
            @AuthenticationPrincipal String userId,
            @PathVariable String postId) {
        deletePostUseCase.execute(userId, postId);
        return ResponseEntity.ok(ApiResponse.ok());
    }

    // POST /api/posts/{postId}/files
    @PostMapping(value = "/{postId}/files", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<PostResponse>> attachFile(
            @AuthenticationPrincipal String userId,
            @PathVariable String postId,
            @RequestPart("file") MultipartFile file) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.ok(attachFileToPostUseCase.execute(userId, postId, file)));
    }

    // POST /api/posts/{postId}/like
    @PostMapping("/{postId}/like")
    public ResponseEntity<ApiResponse<Void>> likePost(
            @AuthenticationPrincipal String userId,
            @PathVariable String postId) {
        likePostUseCase.like(userId, postId);
        return ResponseEntity.ok(ApiResponse.ok());
    }

    // DELETE /api/posts/{postId}/like
    @DeleteMapping("/{postId}/like")
    public ResponseEntity<ApiResponse<Void>> unlikePost(
            @AuthenticationPrincipal String userId,
            @PathVariable String postId) {
        likePostUseCase.unlike(userId, postId);
        return ResponseEntity.ok(ApiResponse.ok());
    }

    // POST /api/posts/{postId}/share
    @PostMapping("/{postId}/share")
    public ResponseEntity<ApiResponse<PostResponse>> sharePost(
            @AuthenticationPrincipal String userId,
            @PathVariable String postId) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.ok(sharePostUseCase.execute(userId, postId)));
    }

    // GET /api/posts/feed
    @GetMapping("/feed")
    public ResponseEntity<ApiResponse<PageResponse<PostResponse>>> getFeed(
            @AuthenticationPrincipal String userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(ApiResponse.ok(
                getFeedUseCase.execute(userId, new PageRequest(page, size))));
    }

    // GET /api/posts/users/{userId}
    @GetMapping("/users/{userId}")
    public ResponseEntity<ApiResponse<PageResponse<PostResponse>>> getUserPosts(
            @PathVariable String userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(ApiResponse.ok(
                getUserPostsUseCase.execute(userId, new PageRequest(page, size))));
    }

    // GET /api/posts/search
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<PageResponse<PostResponse>>> searchPosts(
            @RequestParam String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(ApiResponse.ok(
                searchPostUseCase.execute(q, new PageRequest(page, size))));
    }
}