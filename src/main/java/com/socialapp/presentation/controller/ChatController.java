package com.socialapp.presentation.controller;

import com.socialapp.application.dto.request.EditMessageRequest;
import com.socialapp.application.dto.request.SendMessageRequest;
import com.socialapp.application.dto.response.ApiResponse;
import com.socialapp.application.dto.response.CallResponse;
import com.socialapp.application.dto.response.ChatResponse;
import com.socialapp.application.dto.response.MessageResponse;
import com.socialapp.application.usecase.chat.*;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller — Chat & Messages
 *
 * GET    /api/chats                                    — Danh sách tất cả đoạn chat
 * GET    /api/chats/with/{otherUserId}                 — Lấy hoặc tạo chat riêng tư
 * GET    /api/chats/{chatId}/messages                  — Lịch sử tin nhắn của chat
 * POST   /api/chats/messages                           — Gửi tin nhắn văn bản
 * PUT    /api/chats/{chatId}/messages/{messageId}      — Chỉnh sửa tin nhắn
 * DELETE /api/chats/{chatId}/messages/{messageId}      — Xóa tin nhắn (soft-delete)
 * PUT    /api/chats/{chatId}/messages/{messageId}/read — Đánh dấu tin nhắn đã đọc
 *
 * Call:
 * POST   /api/chats/{chatId}/calls                     — Bắt đầu cuộc gọi
 * POST   /api/chats/{chatId}/calls/{callId}/answer     — Trả lời cuộc gọi
 * POST   /api/chats/{chatId}/calls/{callId}/end        — Kết thúc cuộc gọi
 */
@RestController
@RequestMapping("/api/chats")
public class ChatController {

    private final ListChatsUseCase          listChatsUseCase;
    private final GetOrCreateChatUseCase    getOrCreateChatUseCase;
    private final GetChatMessagesUseCase    getChatMessagesUseCase;
    private final SendMessageUseCase        sendMessageUseCase;
    private final EditMessageUseCase        editMessageUseCase;
    private final DeleteMessageUseCase      deleteMessageUseCase;
    private final MarkMessageReadUseCase    markMessageReadUseCase;
    private final StartCallUseCase          startCallUseCase;
    private final AnswerCallUseCase         answerCallUseCase;
    private final EndCallUseCase            endCallUseCase;

    public ChatController(ListChatsUseCase listChatsUseCase,
                          GetOrCreateChatUseCase getOrCreateChatUseCase,
                          GetChatMessagesUseCase getChatMessagesUseCase,
                          SendMessageUseCase sendMessageUseCase,
                          EditMessageUseCase editMessageUseCase,
                          DeleteMessageUseCase deleteMessageUseCase,
                          MarkMessageReadUseCase markMessageReadUseCase,
                          StartCallUseCase startCallUseCase,
                          AnswerCallUseCase answerCallUseCase,
                          EndCallUseCase endCallUseCase) {
        this.listChatsUseCase       = listChatsUseCase;
        this.getOrCreateChatUseCase = getOrCreateChatUseCase;
        this.getChatMessagesUseCase = getChatMessagesUseCase;
        this.sendMessageUseCase     = sendMessageUseCase;
        this.editMessageUseCase     = editMessageUseCase;
        this.deleteMessageUseCase   = deleteMessageUseCase;
        this.markMessageReadUseCase = markMessageReadUseCase;
        this.startCallUseCase       = startCallUseCase;
        this.answerCallUseCase      = answerCallUseCase;
        this.endCallUseCase         = endCallUseCase;
    }

    // ── GET /api/chats ───────────────────────────────────────
    @GetMapping
    public ResponseEntity<ApiResponse<List<ChatResponse>>> listChats(
            @AuthenticationPrincipal String userId) {

        List<ChatResponse> data = listChatsUseCase.execute(userId);
        return ResponseEntity.ok(ApiResponse.ok(data));
    }

    // ── GET /api/chats/with/{otherUserId} ────────────────────
    @GetMapping("/with/{otherUserId}")
    public ResponseEntity<ApiResponse<ChatResponse>> getOrCreateChat(
            @AuthenticationPrincipal String userId,
            @PathVariable String otherUserId) {

        ChatResponse data = getOrCreateChatUseCase.execute(userId, otherUserId);
        return ResponseEntity.ok(ApiResponse.ok(data));
    }

    // ── GET /api/chats/{chatId}/messages ─────────────────────
    @GetMapping("/{chatId}/messages")
    public ResponseEntity<ApiResponse<List<MessageResponse>>> getMessages(
            @AuthenticationPrincipal String userId,
            @PathVariable String chatId) {

        List<MessageResponse> data =
                getChatMessagesUseCase.execute(chatId, userId);

        return ResponseEntity.ok(ApiResponse.ok(data));
    }

    // ── POST /api/chats/messages ─────────────────────────────
    @PostMapping("/messages")
    public ResponseEntity<ApiResponse<MessageResponse>> sendMessage(
            @AuthenticationPrincipal String userId,
            @Valid @RequestBody SendMessageRequest req) {

        MessageResponse data = sendMessageUseCase.execute(userId, req);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.ok(data));
    }

    // ── PUT /api/chats/{chatId}/messages/{messageId} ─────────
    @PutMapping("/{chatId}/messages/{messageId}")
    public ResponseEntity<ApiResponse<MessageResponse>> editMessage(
            @AuthenticationPrincipal String userId,
            @PathVariable String chatId,
            @PathVariable String messageId,
            @Valid @RequestBody EditMessageRequest req) {

        MessageResponse data =
                editMessageUseCase.execute(userId, chatId, messageId, req);

        return ResponseEntity.ok(ApiResponse.ok(data));
    }

    // ── DELETE /api/chats/{chatId}/messages/{messageId} ──────
    @DeleteMapping("/{chatId}/messages/{messageId}")
    public ResponseEntity<ApiResponse<Void>> deleteMessage(
            @AuthenticationPrincipal String userId,
            @PathVariable String chatId,
            @PathVariable String messageId) {

        deleteMessageUseCase.execute(userId, chatId, messageId);
        return ResponseEntity.ok(ApiResponse.ok());
    }

    // ── PUT /api/chats/{chatId}/messages/{messageId}/read ────
    @PutMapping("/{chatId}/messages/{messageId}/read")
    public ResponseEntity<ApiResponse<Void>> markMessageRead(
            @PathVariable String chatId,
            @PathVariable String messageId) {

        markMessageReadUseCase.execute(chatId, messageId);
        return ResponseEntity.ok(ApiResponse.ok());
    }

    // ── POST /api/chats/{chatId}/calls ───────────────────────
    @PostMapping("/{chatId}/calls")
    public ResponseEntity<ApiResponse<CallResponse>> startCall(
            @AuthenticationPrincipal String userId,
            @PathVariable String chatId,
            @RequestParam(defaultValue = "false") boolean isVideo) {

        CallResponse data =
                startCallUseCase.execute(userId, chatId, isVideo);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.ok(data));
    }

    // ── POST /api/chats/{chatId}/calls/{callId}/answer ───────
    @PostMapping("/{chatId}/calls/{callId}/answer")
    public ResponseEntity<ApiResponse<Void>> answerCall(
            @PathVariable String chatId,
            @PathVariable String callId) {

        answerCallUseCase.execute(chatId, callId);
        return ResponseEntity.ok(ApiResponse.ok());
    }

    // ── POST /api/chats/{chatId}/calls/{callId}/end ──────────
    @PostMapping("/{chatId}/calls/{callId}/end")
    public ResponseEntity<ApiResponse<Void>> endCall(
            @PathVariable String chatId,
            @PathVariable String callId) {

        endCallUseCase.execute(chatId, callId);
        return ResponseEntity.ok(ApiResponse.ok());
    }
}