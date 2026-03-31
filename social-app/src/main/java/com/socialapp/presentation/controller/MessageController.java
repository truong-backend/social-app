package com.socialapp.presentation.controller;

import com.socialapp.application.message.dto.request.MessageRequestDtos.*;
import com.socialapp.application.message.dto.response.MessageResponseDtos.*;
import com.socialapp.application.message.usecase.*;
import com.socialapp.domain.account.repository.AccountRepository;
import com.socialapp.presentation.util.ApiResponse;
import com.socialapp.presentation.util.SecurityUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.*;

@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
public class MessageController {

    private final SendMessageUseCase sendMessageUseCase;
    private final UpdateMessageUseCase updateMessageUseCase;
    private final DeleteMessageUseCase deleteMessageUseCase;
    private final GetChatListUseCase getChatListUseCase;
    private final GetChatUseCase getChatUseCase;
    private final SearchChatUseCase searchChatUseCase;
    private final AccountRepository accountRepository;

    @Value("${STRINGEE_API_KEY_SID:}")
    private String stringeeApiKeySid;

    @Value("${STRINGEE_API_KEY_SECRET:}")
    private String stringeeApiKeySecret;

    private String resolveUserId() {
        return accountRepository.findById(SecurityUtil.currentAccountId())
                .orElseThrow()
                .getUserId();
    }

    // ================== STRINGEE TOKEN ==================
    @GetMapping("/calls/stringee-token")
    public ResponseEntity<String> getStringeeToken() {
        String userId = resolveUserId();
        String token = buildStringeeToken(userId);
        return ResponseEntity.ok(token);  // just return the raw string
    }

    /**
     * ✅ Build JWT chuẩn cho Stringee
     */
    private String buildStringeeToken(String userId) {

        // ❗ BẮT BUỘC phải có KEY + SECRET
        if (stringeeApiKeySid == null || stringeeApiKeySid.isBlank()
                || stringeeApiKeySecret == null || stringeeApiKeySecret.isBlank()) {
            throw new RuntimeException("Missing Stringee API credentials");
        }

        try {
            long now = System.currentTimeMillis() / 1000;
            long exp = now + 3600;

            // Header
            String headerJson = "{\"typ\":\"JWT\",\"alg\":\"HS256\"}";
            String header = base64Url(headerJson);

            // Payload
            String payloadJson = String.format(
                    "{\"jti\":\"%s-%d\",\"iss\":\"%s\",\"exp\":%d,\"userId\":\"%s\"}",
                    UUID.randomUUID(),
                    now,
                    stringeeApiKeySid,
                    exp,
                    userId
            );
            String payload = base64Url(payloadJson);

            // Data
            String data = header + "." + payload;

            // Signature
            String signature = signHmacSHA256(data, stringeeApiKeySecret);

            return data + "." + signature;

        } catch (Exception e) {
            throw new RuntimeException("Cannot generate Stringee token", e);
        }
    }

    // ================== UTILS ==================

    private String base64Url(String input) {
        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(input.getBytes(StandardCharsets.UTF_8));
    }

    private String signHmacSHA256(String data, String secret) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(
                secret.getBytes(StandardCharsets.UTF_8),
                "HmacSHA256"
        ));

        byte[] raw = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));

        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(raw);
    }
}