package com.socialapp.infrastructure.zegocloud;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Tạo ZegoCloud Token04 (server-side token generation).
 *
 * Spec chính xác từ ZegoCloud docs:
 * - ServerSecret là chuỗi HEX 32 ký tự (= 16 bytes sau khi decode hex)
 * - Dùng AES-128-CBC để mã hoá content JSON
 * - Dùng HMAC-SHA256 với key = hex-decoded secret
 * - Binary format: version(4B) + hmac(32B) + expire(8B) + iv_len(2B) + iv(16B) + content_len(2B) + content
 * - Token = "04" + Base64(binary)
 */
@Slf4j
@Component
public class ZegoTokenUtil {

    private static final int    EXPIRE_SECONDS = 3600;
    private static final int    TOKEN_VERSION  = 4;
    private static final int    IV_LENGTH      = 16;

    @Value("${ZEGOCLOUD_APP_ID:0}")
    private long appId;

    /** ServerSecret dạng hex 32 ký tự (ví dụ: "a1b2c3d4e5f6...") */
    @Value("${ZEGOCLOUD_SERVER_SECRET:}")
    private String serverSecretHex;

    public String generateToken(String userId) {
        if (serverSecretHex == null || serverSecretHex.isBlank()) {
            throw new IllegalStateException("ZEGOCLOUD_SERVER_SECRET chưa được cấu hình");
        }
        if (appId == 0) {
            throw new IllegalStateException("ZEGOCLOUD_APP_ID chưa được cấu hình");
        }
        try {
            // Decode hex secret → 16 bytes key (AES-128)
            byte[] keyBytes = hexToBytes(serverSecretHex);

            long now        = System.currentTimeMillis() / 1000;
            long expireTime = now + EXPIRE_SECONDS;
            int  nonce      = new SecureRandom().nextInt();

            // Random IV
            byte[] iv = new byte[IV_LENGTH];
            new SecureRandom().nextBytes(iv);

            // Content JSON (compact, no spaces)
            String contentJson = String.format(
                "{\"app_id\":%d,\"user_id\":\"%s\",\"nonce\":%d,\"ctime\":%d,\"expire\":%d,\"payload\":\"\"}",
                appId, userId, nonce, now, expireTime
            );

            // AES-128-CBC encrypt content
            byte[] encrypted = aesEncrypt(contentJson.getBytes(StandardCharsets.UTF_8), keyBytes, iv);

            // Build binary buffer for HMAC: expire(8) + iv_len(2) + iv(16) + content_len(2) + content
            ByteBuffer hmacBuf = ByteBuffer.allocate(8 + 2 + iv.length + 2 + encrypted.length);
            hmacBuf.putLong(expireTime);
            hmacBuf.putShort((short) iv.length);
            hmacBuf.put(iv);
            hmacBuf.putShort((short) encrypted.length);
            hmacBuf.put(encrypted);

            // HMAC-SHA256
            byte[] mac = hmacSHA256(hmacBuf.array(), keyBytes);

            // Final binary: version(4) + mac(32) + expire(8) + iv_len(2) + iv + content_len(2) + content
            ByteBuffer token = ByteBuffer.allocate(4 + 32 + 8 + 2 + iv.length + 2 + encrypted.length);
            token.putInt(TOKEN_VERSION);
            token.put(mac);
            token.putLong(expireTime);
            token.putShort((short) iv.length);
            token.put(iv);
            token.putShort((short) encrypted.length);
            token.put(encrypted);

            return "04" + Base64.getEncoder().encodeToString(token.array());

        } catch (Exception e) {
            log.error("Failed to generate ZegoCloud token for user: {}", userId, e);
            throw new RuntimeException("Cannot generate ZegoCloud token", e);
        }
    }

    private byte[] aesEncrypt(byte[] plaintext, byte[] key, byte[] iv) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE,
                new SecretKeySpec(key, "AES"),
                new IvParameterSpec(iv));
        return cipher.doFinal(plaintext);
    }

    private byte[] hmacSHA256(byte[] data, byte[] key) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(key, "HmacSHA256"));
        return mac.doFinal(data);
    }

    /** Decode hex string "a1b2..." → byte[] */
    private byte[] hexToBytes(String hex) {
        int len = hex.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(hex.charAt(i), 16) << 4)
                                 + Character.digit(hex.charAt(i + 1), 16));
        }
        return data;
    }
}
