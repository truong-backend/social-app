package com.socialapp.presentation.controller;

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

    private final RegisterUseCase         registerUseCase;
    private final LoginUseCase            loginUseCase;
    private final VerifyEmailUseCase      verifyEmailUseCase;
    private final ResendVerifyCodeUseCase resendVerifyCodeUseCase;
    private final ChangePasswordUseCase   changePasswordUseCase;

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
}