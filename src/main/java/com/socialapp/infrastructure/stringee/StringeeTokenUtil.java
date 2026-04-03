package com.socialapp.infrastructure.stringee;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.UUID;

/**
 * Tạo JWT access token để FE khởi tạo Stringee client.
 * Logic giống StringeeTokenUtil trong social-network nhưng dùng HMAC thủ công
 * thay vì thư viện jjwt (vì header cần "cty": "stringee-api;v=1").
 */
@Slf4j
@Component
public class StringeeTokenUtil {

    private static final long EXPIRE_SECONDS = 3600L; // 1 giờ

    @Value("${STRINGEE_API_KEY_SID:}")
    private String apiKeySid;

    @Value("${STRINGEE_API_KEY_SECRET:}")
    private String apiKeySecret;

    public String createAccessToken(String userId) {
        if (apiKeySid == null || apiKeySid.isBlank()
                || apiKeySecret == null || apiKeySecret.isBlank()) {
            throw new IllegalStateException("Stringee API credentials are not configured");
        }
        try {
            long now = System.currentTimeMillis() / 1000;
            long exp = now + EXPIRE_SECONDS;

            // Header: {"typ":"JWT","alg":"HS256","cty":"stringee-api;v=1"}
            String headerJson = "{\"typ\":\"JWT\",\"alg\":\"HS256\",\"cty\":\"stringee-api;v=1\"}";
            String header = base64Url(headerJson);

            // Payload
            String jti = apiKeySid + "-" + now + "-" + UUID.randomUUID();
            String payloadJson = String.format(
                    "{\"jti\":\"%s\",\"iss\":\"%s\",\"exp\":%d,\"userId\":\"%s\"}",
                    jti, apiKeySid, exp, userId);
            String payload = base64Url(payloadJson);

            String data = header + "." + payload;
            String signature = signHmacSHA256(data, apiKeySecret);

            return data + "." + signature;
        } catch (Exception e) {
            log.error("Failed to create Stringee token for user: {}", userId, e);
            throw new RuntimeException("Cannot generate Stringee token", e);
        }
    }

    private String base64Url(String input) {
        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(input.getBytes(StandardCharsets.UTF_8));
    }

    private String signHmacSHA256(String data, String secret) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        byte[] raw = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
        return Base64.getUrlEncoder().withoutPadding().encodeToString(raw);
    }
}
