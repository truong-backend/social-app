package com.stu.socialnetworkapi.config;

import com.stu.socialnetworkapi.entity.Account;
import com.stu.socialnetworkapi.repository.neo4j.AccountRepository;
import com.stu.socialnetworkapi.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtUtil jwtUtil;
    private final AccountRepository accountRepository;

    @Value("${app.oauth2.frontend-redirect-url}")
    private String frontendRedirectUrl;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {

        OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();
        String email = oauth2User.getAttribute("email");

        Account account = accountRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Account not found after OAuth2 login: " + email));

        // Nếu chưa verified thì set verified (edge case)
        if (!account.isVerified()) {
            account.setVerified(true);
            accountRepository.save(account);
        }

        // Tạo access token
        String accessToken = jwtUtil.generateAccessToken(
                account.getId(),
                account.getUser().getUsername(),
                account.getRole()
        );

        // Tạo refresh token lưu vào Redis + set cookie
        jwtUtil.generateAndStoreRefreshToken(
                account.getId(),
                account.getRole(),
                account.getUser().getUsername(),
                response
        );

        // Redirect về FE kèm accessToken trên URL
        String redirectUrl = UriComponentsBuilder.fromUriString(frontendRedirectUrl)
                .queryParam("accessToken", accessToken)
                .build().toUriString();

        getRedirectStrategy().sendRedirect(request, response, redirectUrl);
    }
}