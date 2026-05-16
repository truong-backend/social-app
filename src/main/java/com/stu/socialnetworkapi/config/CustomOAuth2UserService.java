package com.stu.socialnetworkapi.config;

import com.stu.socialnetworkapi.entity.Account;
import com.stu.socialnetworkapi.entity.User;
import com.stu.socialnetworkapi.repository.neo4j.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2UserService<OAuth2UserRequest, OAuth2User> delegate = new DefaultOAuth2UserService();
        OAuth2User oauth2User = delegate.loadUser(userRequest);

        Map<String, Object> attributes = oauth2User.getAttributes();
        String email      = (String) attributes.get("email");
        String givenName  = (String) attributes.getOrDefault("given_name", "User");
        String familyName = (String) attributes.getOrDefault("family_name", "");

        if (email == null || email.isBlank()) {
            throw new OAuth2AuthenticationException("Email not found from Google");
        }

        // Tìm account theo email, nếu chưa có thì tạo mới
        accountRepository.findByEmail(email).orElseGet(() -> {
            UUID newId = UUID.randomUUID();

            User newUser = User.builder()
                    .id(newId)
                    .username(newId.toString())
                    .givenName(givenName)
                    .familyName(familyName)
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

        return oauth2User;
    }
}