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

    /** GET /api/messages/chats — danh sách đoạn chat */
    @GetMapping("/chats")
    public ApiResponse<List<ChatResponse>> getChatList() {
        return ApiResponse.ok(getChatListUseCase.execute(resolveUserId()));
    }

    /** GET /api/messages/chats/search?q= */
    @GetMapping("/chats/search")
    public ApiResponse<List<ChatResponse>> searchChats(
            @RequestParam("q") String query) {
        return ApiResponse.ok(searchChatUseCase.execute(resolveUserId(), query));
    }

    /** GET /api/messages/chats/{chatId} — xem tin nhắn */
    @GetMapping("/chats/{chatId}")
    public ApiResponse<List<MessageResponse>> getChat(
            @PathVariable String chatId,
            @RequestParam(defaultValue = "0")  int skip,
            @RequestParam(defaultValue = "20") int limit) {
        return ApiResponse.ok(getChatUseCase.execute(resolveUserId(), chatId, skip, limit));
    }

    /** POST /api/messages/chats/{targetUserId} — gửi tin nhắn */
    @PostMapping(value = "/chats/{targetUserId}",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<MessageResponse> send(
            @PathVariable String targetUserId,
            @RequestPart("data") @Valid SendMessageRequest request,
            @RequestPart(value = "files", required = false) List<MultipartFile> files) {
        return ApiResponse.ok(
                sendMessageUseCase.execute(resolveUserId(), targetUserId, request, files));
    }

    /** PUT /api/messages/{messageId} — chỉnh sửa tin nhắn */
    @PutMapping("/{messageId}")
    public ApiResponse<MessageResponse> update(
            @PathVariable String messageId,
            @Valid @RequestBody UpdateMessageRequest request) {
        return ApiResponse.ok(
                updateMessageUseCase.execute(resolveUserId(), messageId, request));
    }

    /** DELETE /api/messages/{messageId} — xóa tin nhắn */
    @DeleteMapping("/{messageId}")
    public ApiResponse<Void> delete(
            @PathVariable String messageId,
            @Valid @RequestBody DeleteMessageRequest request) {
        var res = deleteMessageUseCase.execute(resolveUserId(), messageId, request);
        return ApiResponse.ok(res.message());
    }


    // ================== STRINGEE TOKEN ==================
    @GetMapping("/calls/stringee-token")
    public ResponseEntity<String> getStringeeToken() {
        String userId = resolveUserId();
        String token = buildStringeeToken(userId);
        return ResponseEntity.ok(token);  // just return the raw string
    }

    /**
     * Build JWT chuẩn cho Stringee
     */
    private String buildStringeeToken(String userId) {

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