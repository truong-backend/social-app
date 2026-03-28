package com.socialapp.presentation.controller;

import com.socialapp.application.user.dto.request.UserRequestDtos.*;
import com.socialapp.application.user.dto.response.UserResponseDtos.*;
import com.socialapp.application.user.usecase.*;
import com.socialapp.domain.account.repository.AccountRepository;
import com.socialapp.domain.shared.valueobject.Email;
import com.socialapp.presentation.util.ApiResponse;
import com.socialapp.presentation.util.SecurityUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final GetProfileUseCase         getProfileUseCase;
    private final ChangeNameUseCase         changeNameUseCase;
    private final ChangeUsernameUseCase     changeUsernameUseCase;
    private final ChangeBirthdateUseCase    changeBirthdateUseCase;
    private final ChangeBioUseCase          changeBioUseCase;
    private final UpdateProfilePictureUseCase updateProfilePictureUseCase;
    private final SearchUserUseCase         searchUserUseCase;
    private final AccountRepository         accountRepository;

    // ── Helper: accountId → userId ────────────────────────────
    private String resolveUserId() {
        String accountId = SecurityUtil.currentAccountId();
        return accountRepository.findById(accountId)
                .orElseThrow().getUserId();
    }

    /** GET /api/users/{targetId} */
    @GetMapping("/{targetId}")
    public ApiResponse<UserProfileResponse> getProfile(
            @PathVariable String targetId) {
        String userId = resolveUserId();
        return ApiResponse.ok(getProfileUseCase.execute(userId, targetId));
    }

    /** GET /api/users/me */
    @GetMapping("/me")
    public ApiResponse<UserProfileResponse> getMyProfile() {
        String userId = resolveUserId();
        return ApiResponse.ok(getProfileUseCase.execute(userId, userId));
    }

    /** GET /api/users/search?q=keyword */
    @GetMapping("/search")
    public ApiResponse<List<UserSummaryResponse>> search(
            @RequestParam("q") String keyword) {
        String userId = resolveUserId();
        return ApiResponse.ok(searchUserUseCase.execute(keyword, userId));
    }

    /** PATCH /api/users/me/name */
    @PatchMapping("/me/name")
    public ApiResponse<Void> changeName(
            @Valid @RequestBody ChangeNameRequest request) {
        var res = changeNameUseCase.execute(resolveUserId(), request);
        return ApiResponse.ok(res.message());
    }

    /** PATCH /api/users/me/username */
    @PatchMapping("/me/username")
    public ApiResponse<Void> changeUsername(
            @Valid @RequestBody ChangeUsernameRequest request) {
        var res = changeUsernameUseCase.execute(resolveUserId(), request);
        return ApiResponse.ok(res.message());
    }

    /** PATCH /api/users/me/birthdate */
    @PatchMapping("/me/birthdate")
    public ApiResponse<Void> changeBirthdate(
            @Valid @RequestBody ChangeBirthdateRequest request) {
        var res = changeBirthdateUseCase.execute(resolveUserId(), request);
        return ApiResponse.ok(res.message());
    }

    /** PATCH /api/users/me/bio */
    @PatchMapping("/me/bio")
    public ApiResponse<Void> changeBio(
            @Valid @RequestBody ChangeBioRequest request) {
        var res = changeBioUseCase.execute(resolveUserId(), request);
        return ApiResponse.ok(res.message());
    }

    /** PATCH /api/users/me/profile-picture (multipart) */
    @PatchMapping(value = "/me/profile-picture",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<Void> updateProfilePicture(
            @RequestPart("file") MultipartFile file) {
        var res = updateProfilePictureUseCase.execute(resolveUserId(), file);
        return ApiResponse.ok(res.message());
    }
}
