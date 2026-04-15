package com.socialapp.domain.service;

import com.socialapp.application.dto.response.ErrorCode;
import com.socialapp.domain.model.aggregate.Chat;
import com.socialapp.domain.model.entity.Call;
import com.socialapp.domain.model.entity.Message;
import com.socialapp.domain.model.valueobject.FileMeta;
import com.socialapp.domain.model.valueobject.MessageContent;
import com.socialapp.domain.model.valueobject.UserId;
import com.socialapp.domain.repository.ChatRepository;
import com.socialapp.presentation.advice.DomainException;

import java.util.UUID;

/**
 * Domain Service: ChatDomainService
 * ─────────────────────────────────────────────────────────────
 * Xử lý nghiệp vụ nhắn tin / gọi điện.
 *
 * Trách nhiệm:
 *   - Tạo đoạn chat
 *   - Gửi tin nhắn text / file
 *   - Sửa / xóa tin nhắn (trong 15 phút)
 *   - Bắt đầu / kết thúc cuộc gọi
 *   - Đánh dấu tin nhắn đã đọc
 */
public class ChatDomainService {

    private final ChatRepository chatRepository;

    public ChatDomainService(ChatRepository chatRepository) {
        this.chatRepository = chatRepository;
    }

    // ── Create Chat ───────────────────────────────────────────

    public Chat createChat(UserId userAId, UserId userBId) {
        Chat chat = new Chat(UUID.randomUUID().toString());
        chat.addMember(userAId);
        chat.addMember(userBId);
        chatRepository.save(chat);
        return chat;
    }

    // ── Send Message ──────────────────────────────────────────

    public Message sendTextMessage(String chatId, UserId senderId, MessageContent content) {
        Chat    chat    = requireChat(chatId);
        Message message = new Message(UUID.randomUUID().toString(), senderId, content);
        chat.sendMessage(message);
        chatRepository.save(chat);
        return message;
    }

    public Message sendFileMessage(String chatId, UserId senderId, FileMeta fileMeta) {
        Chat    chat    = requireChat(chatId);
        Message message = new Message(
                UUID.randomUUID().toString(), senderId, new MessageContent(""));
        message.attachFile(new com.socialapp.domain.model.entity.FileEntity(fileMeta));
        chat.sendMessage(message);
        chatRepository.save(chat);
        return message;
    }

    // ── Edit Message ──────────────────────────────────────────

    public void editMessage(String chatId, String messageId,
                            UserId requesterId, MessageContent newContent) {
        Chat chat = requireChat(chatId);
        Message message = chat.findMessageById(messageId)
                .orElseThrow(() -> new DomainException(
                        ErrorCode.MESSAGE_NOT_FOUND, "Message not found: " + messageId));

        if (!message.getSenderId().equals(requesterId))
            throw new DomainException(ErrorCode.FORBIDDEN,
                    "Only the sender can edit this message");

        message.editContent(newContent);
        chatRepository.save(chat);
    }

    // ── Delete Message ────────────────────────────────────────

    public void deleteMessage(String chatId, String messageId, UserId requesterId) {
        Chat chat = requireChat(chatId);
        Message message = chat.findMessageById(messageId)
                .orElseThrow(() -> new DomainException(
                        ErrorCode.MESSAGE_NOT_FOUND, "Message not found: " + messageId));

        if (!message.getSenderId().equals(requesterId))
            throw new DomainException(ErrorCode.FORBIDDEN,
                    "Only the sender can delete this message");

        message.softDelete();
        chatRepository.save(chat);
    }

    // ── Mark Read ─────────────────────────────────────────────

    public void markMessageRead(String chatId, String messageId) {
        Chat chat = requireChat(chatId);
        chat.findMessageById(messageId).ifPresent(Message::markRead);
        chatRepository.save(chat);
    }

    // ── Call ──────────────────────────────────────────────────

    public Call startCall(String chatId, UserId callerId, boolean isVideoCall) {
        Chat chat = requireChat(chatId);
        Call call = new Call(UUID.randomUUID().toString(), callerId, isVideoCall);
        chat.sendMessage(call);
        chatRepository.save(chat);
        return call;
    }

    public void answerCall(String chatId, String callId) {
        Chat chat = requireChat(chatId);
        chat.findMessageById(callId)
                .filter(m -> m instanceof Call)
                .map(m -> (Call) m)
                .ifPresentOrElse(
                        Call::answer,
                        () -> { throw new DomainException(
                                ErrorCode.MESSAGE_NOT_FOUND, "Call not found: " + callId); }
                );
        chatRepository.save(chat);
    }

    public void endCall(String chatId, String callId) {
        Chat chat = requireChat(chatId);
        chat.findMessageById(callId)
                .filter(m -> m instanceof Call)
                .map(m -> (Call) m)
                .ifPresentOrElse(
                        Call::end,
                        () -> { throw new DomainException(
                                ErrorCode.MESSAGE_NOT_FOUND, "Call not found: " + callId); }
                );
        chatRepository.save(chat);
    }

    // ── Helper ───────────────────────────────────────────────

    private Chat requireChat(String chatId) {
        return chatRepository.findById(chatId)
                .orElseThrow(() -> new DomainException(
                        ErrorCode.CHAT_NOT_FOUND, "Chat not found: " + chatId));
    }
}