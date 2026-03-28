package com.socialapp.presentation.controller;

import com.socialapp.application.account.dto.request.AccountRequestDtos.*;
import com.socialapp.application.account.dto.response.AccountResponseDtos.*;
import com.socialapp.application.account.usecase.Register.ConfirmEmailUseCase;
import com.socialapp.application.account.usecase.Register.RegisterUseCase;
import com.socialapp.application.account.usecase.login.ConfirmResetCodeUseCase;
import com.socialapp.application.account.usecase.login.LoginUseCase;
import com.socialapp.application.account.usecase.login.PrepareResetPasswordUseCase;
import com.socialapp.application.account.usecase.login.UpdatePasswordUseCase;
import com.socialapp.application.account.usecase.logout.LogoutUseCase;
import com.socialapp.presentation.util.ApiResponse;
import com.socialapp.presentation.util.SecurityUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final RegisterUseCase registerUseCase;
    private final ConfirmEmailUseCase confirmEmailUseCase;
    private final LoginUseCase loginUseCase;
    private final LogoutUseCase logoutUseCase;
    private final PrepareResetPasswordUseCase prepareResetUseCase;
    private final ConfirmResetCodeUseCase confirmResetCodeUseCase;
    private final UpdatePasswordUseCase updatePasswordUseCase;

    /** POST /api/auth/register */
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<RegisterResponse> register(
            @Valid @RequestBody RegisterRequest request) {
        return ApiResponse.ok(registerUseCase.execute(request));
    }

    /** POST /api/auth/confirm-email */
    @PostMapping("/confirm-email")
    public ApiResponse<AuthResponse> confirmEmail(
            @Valid @RequestBody ConfirmEmailRequest request) {
        String accountId = SecurityUtil.currentAccountId();
        return ApiResponse.ok(confirmEmailUseCase.execute(accountId, request));
    }

    /** POST /api/auth/login */
    @PostMapping("/login")
    public ApiResponse<AuthResponse> login(
            @Valid @RequestBody LoginRequest request) {
        return ApiResponse.ok(loginUseCase.execute(request));
    }

    /** POST /api/auth/logout */
    @PostMapping("/logout")
    public ApiResponse<Void> logout(
            @RequestHeader("Authorization") String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        logoutUseCase.execute(token);
        return ApiResponse.ok("Logged out successfully");
    }

    /** POST /api/auth/prepare-reset-password */
    @PostMapping("/prepare-reset-password")
    public ApiResponse<Void> prepareReset(
            @Valid @RequestBody PrepareResetPasswordRequest request) {
        var res = prepareResetUseCase.execute(request);
        return ApiResponse.ok(res.message());
    }

    /** POST /api/auth/confirm-reset-code */
    @PostMapping("/confirm-reset-code")
    public ApiResponse<Void> confirmResetCode(
            @Valid @RequestBody ConfirmResetCodeRequest request) {
        String accountId = SecurityUtil.currentAccountId();
        var res = confirmResetCodeUseCase.execute(accountId, request);
        return ApiResponse.ok(res.message());
    }

    /** PUT /api/auth/update-password */
    @PutMapping("/update-password")
    public ApiResponse<Void> updatePassword(
            @Valid @RequestBody UpdatePasswordRequest request) {
        String accountId = SecurityUtil.currentAccountId();
        var res = updatePasswordUseCase.execute(accountId, request);
        return ApiResponse.ok(res.message());
    }
}