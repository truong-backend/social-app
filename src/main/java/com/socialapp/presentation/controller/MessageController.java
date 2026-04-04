package com.socialapp.presentation.controller;

import com.socialapp.application.message.dto.request.MessageRequestDtos.*;
import com.socialapp.application.message.dto.response.MessageResponseDtos.*;
import com.socialapp.application.message.usecase.*;
import com.socialapp.domain.account.repository.AccountRepository;
import com.socialapp.presentation.util.ApiResponse;
import com.socialapp.presentation.util.SecurityUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Controller cho messaging (chat & messages).
 */
@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
public class MessageController {

    private final SendMessageUseCase   sendMessageUseCase;
    private final UpdateMessageUseCase updateMessageUseCase;
    private final DeleteMessageUseCase deleteMessageUseCase;
    private final GetChatListUseCase   getChatListUseCase;
    private final GetChatUseCase       getChatUseCase;
    private final SearchChatUseCase    searchChatUseCase;
    private final AccountRepository    accountRepository;

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
}