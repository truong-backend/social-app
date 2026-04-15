package com.socialapp.presentation.controller;

import com.socialapp.application.dto.request.PageRequest;
import com.socialapp.application.dto.request.UpdateProfileRequest;
import com.socialapp.application.dto.response.ApiResponse;
import com.socialapp.application.dto.response.PageResponse;
import com.socialapp.application.dto.response.UserResponse;
import com.socialapp.application.usecase.user.GetUserProfileUseCase;
import com.socialapp.application.usecase.user.SearchUserUseCase;
import com.socialapp.application.usecase.user.UpdateProfilePictureUseCase;
import com.socialapp.application.usecase.user.UpdateProfileUseCase;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * REST Controller — User Profile
 *
 * GET    /api/users/me                      — Xem profile bản thân
 * GET    /api/users/{userId}                — Xem profile người khác
 * PUT    /api/users/me                      — Cập nhật thông tin (name / birthdate / username / bio)
 * PUT    /api/users/me/profile-picture      — Upload ảnh đại diện
 * GET    /api/users/search?q=&page=&size=   — Tìm kiếm người dùng theo keyword
 */
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final GetUserProfileUseCase       getUserProfileUseCase;
    private final UpdateProfileUseCase        updateProfileUseCase;
    private final UpdateProfilePictureUseCase updateProfilePictureUseCase;
    private final SearchUserUseCase           searchUserUseCase;

    public UserController(GetUserProfileUseCase getUserProfileUseCase,
                          UpdateProfileUseCase updateProfileUseCase,
                          UpdateProfilePictureUseCase updateProfilePictureUseCase,
                          SearchUserUseCase searchUserUseCase) {
        this.getUserProfileUseCase       = getUserProfileUseCase;
        this.updateProfileUseCase        = updateProfileUseCase;
        this.updateProfilePictureUseCase = updateProfilePictureUseCase;
        this.searchUserUseCase           = searchUserUseCase;
    }

    // GET /api/users/me
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> getMyProfile(
            @AuthenticationPrincipal String userId) {
        return ResponseEntity.ok(ApiResponse.ok(getUserProfileUseCase.execute(userId)));
    }

    // GET /api/users/{userId}
    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse<UserResponse>> getUserProfile(
            @PathVariable String userId) {
        return ResponseEntity.ok(ApiResponse.ok(getUserProfileUseCase.execute(userId)));
    }

    // PUT /api/users/me
    @PutMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> updateProfile(
            @AuthenticationPrincipal String userId,
            @Valid @RequestBody UpdateProfileRequest req) {
        return ResponseEntity.ok(ApiResponse.ok(updateProfileUseCase.execute(userId, req)));
    }

    // PUT /api/users/me/profile-picture
    @PutMapping(value = "/me/profile-picture", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<UserResponse>> updateProfilePicture(
            @AuthenticationPrincipal String userId,
            @RequestPart("file") MultipartFile file) {
        return ResponseEntity.ok(ApiResponse.ok(
                updateProfilePictureUseCase.execute(userId, file)));
    }

    // GET /api/users/search
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<PageResponse<UserResponse>>> searchUsers(
            @RequestParam String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(ApiResponse.ok(
                searchUserUseCase.execute(q, new PageRequest(page, size))));
    }
}