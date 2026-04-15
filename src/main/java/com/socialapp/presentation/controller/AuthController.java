package com.socialapp.presentation.controller;

<<<<<<< HEAD
import com.socialapp.application.dto.request.ChangePasswordRequest;
import com.socialapp.application.dto.request.LoginRequest;
import com.socialapp.application.dto.request.RegisterRequest;
import com.socialapp.application.dto.request.VerifyEmailRequest;
import com.socialapp.application.dto.response.AccountResponse;
import com.socialapp.application.dto.response.ApiResponse;
import com.socialapp.application.dto.response.AuthResponse;
import com.socialapp.application.usecase.auth.ChangePasswordUseCase;
import com.socialapp.application.usecase.auth.LoginUseCase;
import com.socialapp.application.usecase.auth.RegisterUseCase;
import com.socialapp.application.usecase.auth.ResendVerifyCodeUseCase;
import com.socialapp.application.usecase.auth.VerifyEmailUseCase;
=======
import com.socialapp.application.account.dto.request.AccountRequestDtos.*;
import com.socialapp.application.account.dto.response.AccountResponseDtos.*;
import com.socialapp.application.account.usecase.ChangeEmailUseCase;
import com.socialapp.application.account.usecase.DeleteAccountUseCase;
import com.socialapp.application.account.usecase.Register.ConfirmEmailUseCase;
import com.socialapp.application.account.usecase.Register.RegisterUseCase;
import com.socialapp.application.account.usecase.login.*;
import com.socialapp.application.account.usecase.logout.LogoutUseCase;
import com.socialapp.presentation.util.ApiResponse;
import com.socialapp.presentation.util.SecurityUtil;
>>>>>>> origin/master
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller — Authentication & Account
 *
 * Public endpoints:
 *   POST /api/auth/register          — Đăng ký tài khoản mới
 *   POST /api/auth/login             — Đăng nhập, nhận JWT
 *   POST /api/auth/verify-email      — Xác thực email bằng code
 *   POST /api/auth/resend-verify     — Gửi lại mã xác thực
 *
 * Authenticated endpoints:
 *   PUT  /api/auth/change-password   — Đổi mật khẩu (yêu cầu JWT)
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

<<<<<<< HEAD
    private final RegisterUseCase         registerUseCase;
    private final LoginUseCase            loginUseCase;
    private final VerifyEmailUseCase      verifyEmailUseCase;
    private final ResendVerifyCodeUseCase resendVerifyCodeUseCase;
    private final ChangePasswordUseCase   changePasswordUseCase;
=======
    private final RegisterUseCase registerUseCase;
    private final ConfirmEmailUseCase confirmEmailUseCase;
    private final LoginUseCase loginUseCase;
    private final LogoutUseCase logoutUseCase;
    private final PrepareResetPasswordUseCase prepareResetUseCase;
    private final ConfirmResetCodeUseCase confirmResetCodeUseCase;
    private final UpdatePasswordUseCase updatePasswordUseCase;
    private final RefreshTokenUseCase refreshTokenUseCase;
    private final DeleteAccountUseCase deleteAccountUseCase;
    private final ChangeEmailUseCase changeEmailUseCase;
>>>>>>> origin/master

    public AuthController(RegisterUseCase registerUseCase,
                          LoginUseCase loginUseCase,
                          VerifyEmailUseCase verifyEmailUseCase,
                          ResendVerifyCodeUseCase resendVerifyCodeUseCase,
                          ChangePasswordUseCase changePasswordUseCase) {
        this.registerUseCase         = registerUseCase;
        this.loginUseCase            = loginUseCase;
        this.verifyEmailUseCase      = verifyEmailUseCase;
        this.resendVerifyCodeUseCase = resendVerifyCodeUseCase;
        this.changePasswordUseCase   = changePasswordUseCase;
    }

    // POST /api/auth/register
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AccountResponse>> register(
            @Valid @RequestBody RegisterRequest req) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.ok(registerUseCase.execute(req)));
    }

    // POST /api/auth/login
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Valid @RequestBody LoginRequest req) {
        return ResponseEntity.ok(ApiResponse.ok(loginUseCase.execute(req)));
    }

    // POST /api/auth/verify-email
    @PostMapping("/verify-email")
    public ResponseEntity<ApiResponse<Void>> verifyEmail(
            @Valid @RequestBody VerifyEmailRequest req) {
        verifyEmailUseCase.execute(req);
        return ResponseEntity.ok(ApiResponse.ok());
    }

    // POST /api/auth/resend-verify
    @PostMapping("/resend-verify")
    public ResponseEntity<ApiResponse<Void>> resendVerify(
            @RequestParam String accountId) {
        resendVerifyCodeUseCase.execute(accountId);
        return ResponseEntity.ok(ApiResponse.ok());
    }

    // PUT /api/auth/change-password
    @PutMapping("/change-password")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @AuthenticationPrincipal String accountId,
            @Valid @RequestBody ChangePasswordRequest req) {
        changePasswordUseCase.execute(accountId, req);
        return ResponseEntity.ok(ApiResponse.ok());
    }

    /** POST /api/auth/refresh */
    @PostMapping("/refresh")
    public ApiResponse<AuthResponse> refresh(
            @RequestHeader("X-Refresh-Token") String refreshToken) {
        return ApiResponse.ok(refreshTokenUseCase.execute(refreshToken));
    }

    // Thêm endpoint:
    /** DELETE /api/auth/account */
    @DeleteMapping("/account")
    public ApiResponse<Void> deleteAccount(
            @Valid @RequestBody DeleteAccountRequest request) {
        String accountId = SecurityUtil.currentAccountId();
        deleteAccountUseCase.execute(accountId, request.password());
        return ApiResponse.ok("Account deleted");
    }

    /** PATCH /api/auth/email */
    @PatchMapping("/email")
    public ApiResponse<Void> changeEmail(
            @Valid @RequestBody ChangeEmailRequest request) {
        String accountId = SecurityUtil.currentAccountId();
        changeEmailUseCase.execute(accountId, request.newEmail(), request.password());
        return ApiResponse.ok("Email updated. Please verify your new email.");
    }
}