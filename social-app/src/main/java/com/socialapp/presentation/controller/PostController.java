package com.socialapp.presentation.controller;

import com.socialapp.application.post.dto.request.PostRequestDtos.*;
import com.socialapp.application.post.dto.response.PostResponseDtos.*;
import com.socialapp.application.post.usecase.*;
import com.socialapp.application.post.usecase.postInteraction.*;
import com.socialapp.application.post.usecase.postMutation.*;
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
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {

    private final CreatePostUseCase         createPostUseCase;
    private final GetPostUseCase            getPostUseCase;
    private final UpdatePostContentUseCase updatePostContentUseCase;
    private final UpdatePostPrivacyUseCase  updatePostPrivacyUseCase;
    private final DeletePostUseCase         deletePostUseCase;
    private final LikePostUseCase           likePostUseCase;
    private final UnlikePostUseCase unlikePostUseCase;
    private final SharePostUseCase          sharePostUseCase;
    private final AccountRepository         accountRepository;

    private String resolveUserId() {
        return accountRepository.findById(SecurityUtil.currentAccountId())
                .orElseThrow().getUserId();
    }

    /** POST /api/posts (multipart) */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<PostResponse> create(
            @RequestPart("data") @Valid CreatePostRequest request,
            @RequestPart(value = "files", required = false) List<MultipartFile> files) {
        return ApiResponse.ok(createPostUseCase.execute(resolveUserId(), request, files));
    }

    /** GET /api/posts/{postId} */
    @GetMapping("/{postId}")
    public ApiResponse<PostResponse> get(@PathVariable String postId) {
        return ApiResponse.ok(getPostUseCase.execute(resolveUserId(), postId));
    }

    /** PUT /api/posts/{postId}/content (multipart) */
    @PutMapping(value = "/{postId}/content",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<PostResponse> updateContent(
            @PathVariable String postId,
            @RequestPart("data") @Valid UpdatePostContentRequest request,
            @RequestPart(value = "files", required = false) List<MultipartFile> files) {
        return ApiResponse.ok(
                updatePostContentUseCase.execute(resolveUserId(), postId, request, files));
    }

    /** PATCH /api/posts/{postId}/privacy */
    @PatchMapping("/{postId}/privacy")
    public ApiResponse<Void> updatePrivacy(
            @PathVariable String postId,
            @Valid @RequestBody UpdatePostPrivacyRequest request) {
        var res = updatePostPrivacyUseCase.execute(resolveUserId(), postId, request);
        return ApiResponse.ok(res.message());
    }

    /** DELETE /api/posts/{postId} */
    @DeleteMapping("/{postId}")
    public ApiResponse<Void> delete(@PathVariable String postId) {
        boolean isAdmin = SecurityUtil.hasRole("ADMIN");
        var res = deletePostUseCase.execute(resolveUserId(), postId, isAdmin);
        return ApiResponse.ok(res.message());
    }

    /** POST /api/posts/{postId}/like */
    @PostMapping("/{postId}/like")
    public ApiResponse<Void> like(@PathVariable String postId) {
        var res = likePostUseCase.execute(resolveUserId(), postId);
        return ApiResponse.ok(res.message());
    }

    /** DELETE /api/posts/{postId}/like */
    @DeleteMapping("/{postId}/like")
    public ApiResponse<Void> unlike(@PathVariable String postId) {
        var res = unlikePostUseCase.execute(resolveUserId(), postId);
        return ApiResponse.ok(res.message());
    }

    /** POST /api/posts/{postId}/share (multipart) */
    @PostMapping(value = "/{postId}/share",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<PostResponse> share(
            @PathVariable String postId,
            @RequestPart("data") @Valid SharePostRequest request) {
        return ApiResponse.ok(sharePostUseCase.execute(resolveUserId(), postId, request));
    }
}
