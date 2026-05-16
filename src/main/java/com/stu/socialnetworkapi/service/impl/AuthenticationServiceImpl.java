package com.stu.socialnetworkapi.service.impl;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.stu.socialnetworkapi.dto.request.GoogleLoginRequest;
import com.stu.socialnetworkapi.dto.request.LoginRequest;
import com.stu.socialnetworkapi.dto.response.AuthenticationResponse;
import com.stu.socialnetworkapi.entity.Account;
import com.stu.socialnetworkapi.entity.User;
import com.stu.socialnetworkapi.enums.AccountRole;
import com.stu.socialnetworkapi.exception.ApiException;
import com.stu.socialnetworkapi.exception.ErrorCode;
import com.stu.socialnetworkapi.repository.neo4j.AccountRepository;
import com.stu.socialnetworkapi.repository.redis.LoginAttemptRepository;
import com.stu.socialnetworkapi.service.itf.AuthenticationService;
import com.stu.socialnetworkapi.util.JwtUtil;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthenticationServiceImpl implements AuthenticationService {
    private static final int MAX_ATTEMPTS = 5;

    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;
    private final LoginAttemptRepository loginAttemptRepository;
    private final JwtUtil jwtUtil;

    @Value("${google.client-id}")
    private String googleClientId;

    @Override
    public AuthenticationResponse authenticate(LoginRequest request, HttpServletResponse response) {
        Account account = accountRepository.findByEmailAndRoleIs(request.email(), AccountRole.USER)
                .orElseThrow(() -> new ApiException(ErrorCode.ACCOUNT_NOT_FOUND));
        validateLogin(account);
        boolean matches = passwordEncoder.matches(request.password(), account.getPassword());
        if (!matches) processLoginFailed(account);

        return processLoginSucceed(account, response);
    }

    @Override
    public AuthenticationResponse authenticateAdmin(LoginRequest request, HttpServletResponse response) {
        Account account = accountRepository.findByEmailAndRoleIs(request.email(), AccountRole.ADMIN)
                .orElseThrow(() -> new ApiException(ErrorCode.ACCOUNT_NOT_FOUND));
        validateLogin(account);
        boolean matches = passwordEncoder.matches(request.password(), account.getPassword());
        if (!matches) processLoginFailed(account);

        return processLoginSucceed(account, response);
    }

    @Override
    public AuthenticationResponse refresh(String refreshToken) {
        String accessToken = jwtUtil.refreshAccessToken(refreshToken);
        return new AuthenticationResponse(accessToken);
    }

    @Override
    public void logout(String token, HttpServletResponse response) {
        jwtUtil.revokeRefreshToken(token, response);
    }

    @Override
    public AuthenticationResponse loginWithGoogle(GoogleLoginRequest request, HttpServletResponse response) {
        // Bước 1: Verify Google ID Token với Google server
        final String email;
        final String finalGivenName;
        final String finalFamilyName;

        try {
            NetHttpTransport transport = new NetHttpTransport();
            JsonFactory jsonFactory = GsonFactory.getDefaultInstance();
            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(transport, jsonFactory)
                    .setAudience(Collections.singletonList(googleClientId))
                    .build();

            GoogleIdToken idToken = verifier.verify(request.googleToken());
            if (idToken == null) {
                throw new ApiException(ErrorCode.INVALID_TOKEN);
            }

            GoogleIdToken.Payload payload = idToken.getPayload();
            email = payload.getEmail();

            String rawGivenName = (String) payload.get("given_name");
            String rawFamilyName = (String) payload.get("family_name");

            // Gán vào biến final để dùng trong lambda
            finalGivenName = (rawGivenName == null || rawGivenName.isBlank()) ? "User" : rawGivenName;
            finalFamilyName = (rawFamilyName == null) ? "" : rawFamilyName;

        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            throw new ApiException(ErrorCode.INVALID_TOKEN);
        }

        // Bước 2: Tìm account theo email
        // - Nếu chưa có → tạo mới (không cần verify email vì Google đã xác thực)
        // - Nếu đã có → login thẳng
        Account account = accountRepository.findByEmail(email).orElseGet(() -> {
            UUID newId = UUID.randomUUID();

            User newUser = User.builder()
                    .id(newId)
                    .username(newId.toString())
                    .givenName(finalGivenName)
                    .familyName(finalFamilyName)
                    .build();

            Account newAccount = Account.builder()
                    .id(newId)
                    .email(email)
                    .password(passwordEncoder.encode(UUID.randomUUID().toString()))
                    .user(newUser)
                    .isVerified(true)
                    .build();

            return accountRepository.save(newAccount);
        });

        // Bước 3: Nếu account tồn tại nhưng chưa verified thì set verified
        if (!account.isVerified()) {
            account.setVerified(true);
            accountRepository.save(account);
        }

        // Bước 4: Tạo JWT token
        String accessToken = jwtUtil.generateAccessToken(
                account.getId(),
                account.getUser().getUsername(),
                account.getRole()
        );
        jwtUtil.generateAndStoreRefreshToken(
                account.getId(),
                account.getRole(),
                account.getUser().getUsername(),
                response
        );

        return new AuthenticationResponse(accessToken);
    }
    private void validateLogin(Account account) {
        if (!account.isVerified())
            throw new ApiException(ErrorCode.ACCOUNT_NOT_VERIFIED);

        boolean isLocked = loginAttemptRepository.isAccountLocked(account.getId());
        if (isLocked)
            throw new ApiException(
                    ErrorCode.ACCOUNT_LOCKED,
                    Map.of("time", loginAttemptRepository.getLockoutTime(account.getId())));
    }

    private AuthenticationResponse processLoginSucceed(Account account, HttpServletResponse response) {
        loginAttemptRepository.loginSucceeded(account.getId());
        String username = account.getUser().getUsername();
        String accessToken = jwtUtil.generateAccessToken(account.getId(), username, account.getRole());
        jwtUtil.generateAndStoreRefreshToken(account.getId(), account.getRole(), username, response);
        return new AuthenticationResponse(accessToken);
    }

    private void processLoginFailed(Account account) {
        loginAttemptRepository.loginFailed(account.getId());
        int failedAttempts = loginAttemptRepository.getFailedAttempts(account.getId());

        if (failedAttempts == 5)
            throw new ApiException(ErrorCode.ACCOUNT_LOCKED, Map.of("time", loginAttemptRepository.getLockoutTime(account.getId())));
        else
            throw new ApiException(ErrorCode.AUTHENTICATION_FAILED, Map.of("remainingAttempts", MAX_ATTEMPTS - failedAttempts));
    }
}