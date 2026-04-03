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
import org.springframework.messaging.simp.SimpMessagingTemplate;
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
    private final SimpMessagingTemplate messagingTemplate; // ← thêm

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

    @GetMapping("/calls/stringee-token")
    public ApiResponse<String> getStringeeToken() {
        String userId = resolveUserId();
        String token = buildStringeeToken(userId);
        return new ApiResponse<>(true, null, token);
    }

    // ── Call endpoints ─────────────────────────────────────────

    /**
     * POST /api/messages/calls — khởi tạo cuộc gọi
     * Push WebSocket incoming_call đến receiver để hiện modal
     */
    @PostMapping("/calls")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<InitiateCallResponse> initiateCall(
            @Valid @RequestBody InitiateCallRequest request) {

        String callerId   = resolveUserId();
        String callerName = callerId; // TODO: đổi thành display name nếu có

        String callId    = "call-" + UUID.randomUUID();
        String messageId = "msg-"  + UUID.randomUUID();
        String chatId    = "chat-" + UUID.randomUUID();

        // Push WebSocket đến receiver — FE subscribe /user/{userId}/queue/incoming_call
        Map<String, Object> payload = new HashMap<>();
        payload.put("callId",      callId);
        payload.put("messageId",   messageId);
        payload.put("chatId",      chatId);
        payload.put("callerId",    callerId);
        payload.put("callerName",  callerName);
        payload.put("isVideoCall", request.isVideoCall());

        messagingTemplate.convertAndSendToUser(
                request.targetUserId(),
                "/queue/incoming_call",
                payload
        );

        System.out.println("Push done");

        return ApiResponse.ok(new InitiateCallResponse(callId, messageId, chatId));
    }

    /** POST /api/messages/calls/{callId}/answer */
    @PostMapping("/calls/{callId}/answer")
    public ApiResponse<Void> answerCall(@PathVariable String callId) {
        return ApiResponse.ok(null);
    }

    /** POST /api/messages/calls/{callId}/end — push call_ended đến bên kia */
    @PostMapping("/calls/{callId}/end")
    public ApiResponse<Void> endCall(
            @PathVariable String callId,
            @RequestBody(required = false) EndCallRequest request) {

        if (request != null && request.targetUserId() != null) {
            Map<String, Object> payload = new HashMap<>();
            payload.put("callId", callId);

            messagingTemplate.convertAndSendToUser(
                    request.targetUserId(),
                    "/queue/call_ended",
                    payload
            );
        }

        return ApiResponse.ok(null);
    }

    // ── JWT builder ────────────────────────────────────────────

    private String buildStringeeToken(String userId) {
        if (stringeeApiKeySid == null || stringeeApiKeySid.isBlank()
                || stringeeApiKeySecret == null || stringeeApiKeySecret.isBlank()) {
            throw new RuntimeException("Missing Stringee API credentials");
        }
        try {
            long now = System.currentTimeMillis() / 1000;
            long exp = now + 3600;
            String headerJson  = "{\"typ\":\"JWT\",\"alg\":\"HS256\"}";
            String header      = base64Url(headerJson);
            String payloadJson = String.format(
                    "{\"jti\":\"%s-%d\",\"iss\":\"%s\",\"exp\":%d,\"userId\":\"%s\"}",
                    UUID.randomUUID(), now, stringeeApiKeySid, exp, userId);
            String payload     = base64Url(payloadJson);
            String data        = header + "." + payload;
            return data + "." + signHmacSHA256(data, stringeeApiKeySecret);
        } catch (Exception e) {
            throw new RuntimeException("Cannot generate Stringee token", e);
        }
    }

    // ── Utils ──────────────────────────────────────────────────

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

    // ── DTOs ───────────────────────────────────────────────────

    public record InitiateCallRequest(String targetUserId, boolean isVideoCall) {}
    public record InitiateCallResponse(String callId, String messageId, String chatId) {}
    public record EndCallRequest(String targetUserId) {}
}