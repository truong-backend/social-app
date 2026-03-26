package com.socialapp.domain.message.entity;

import com.socialapp.domain.message.exception.MessageDomainException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Entity: Message
 */
public class Message {

    // ── Identity ──────────────────────────────────────────────
    private final String id;
    private final String senderId;
    private final String chatId;

    // ── Content ───────────────────────────────────────────────
    private String content;
    private final List<String> attachedFilePaths;

    // ── State ─────────────────────────────────────────────────
    private boolean isRead;
    private LocalDateTime deletedForEveryoneAt;
    private LocalDateTime deletedForSenderAt;

    // ── Timestamps ────────────────────────────────────────────
    private final LocalDateTime sentAt;
    private LocalDateTime updatedAt;

    // ── Private constructor ───────────────────────────────────
    private Message(String id, String senderId, String chatId,
                    String content, List<String> attachedFilePaths,
                    boolean isRead,
                    LocalDateTime deletedForEveryoneAt, LocalDateTime deletedForSenderAt,
                    LocalDateTime sentAt, LocalDateTime updatedAt) {
        this.id                     = id;
        this.senderId               = senderId;
        this.chatId                 = chatId;
        this.content                = content;
        this.attachedFilePaths      = new ArrayList<>(attachedFilePaths);
        this.isRead                 = isRead;
        this.deletedForEveryoneAt   = deletedForEveryoneAt;
        this.deletedForSenderAt     = deletedForSenderAt;
        this.sentAt                 = sentAt;
        this.updatedAt              = updatedAt;
    }

    // ── Factory Methods ───────────────────────────────────────

    public static Message create(String senderId, String chatId,
                                 String content, List<String> filePaths) {
        return new Message(UUID.randomUUID().toString(), senderId, chatId,
                content, filePaths != null ? filePaths : List.of(),
                false, null, null,
                LocalDateTime.now(), LocalDateTime.now());
    }

    public static Message reconstitute(String id, String senderId, String chatId,
                                       String content, List<String> attachedFilePaths,
                                       boolean isRead,
                                       LocalDateTime deletedForEveryoneAt,
                                       LocalDateTime deletedForSenderAt,
                                       LocalDateTime sentAt, LocalDateTime updatedAt) {
        return new Message(id, senderId, chatId, content, attachedFilePaths,
                isRead, deletedForEveryoneAt, deletedForSenderAt,
                sentAt, updatedAt);
    }

    // ── Domain Behaviors ──────────────────────────────────────

    public void updateContent(String requesterId, String newContent) {
        validateSender(requesterId);
        if (isDeletedForEveryone())
            throw new MessageDomainException("Cannot edit a deleted message");
        this.content   = newContent;
        this.updatedAt = LocalDateTime.now();
    }

    public void deleteForEveryone(String requesterId) {
        validateSender(requesterId);
        if (isDeletedForEveryone())
            throw new MessageDomainException("Message already deleted");
        this.deletedForEveryoneAt = LocalDateTime.now();
    }

    public void deleteForSender(String requesterId) {
        validateSender(requesterId);
        this.deletedForSenderAt = LocalDateTime.now();
    }

    public void markAsRead() {
        this.isRead = true;
    }

    // ── Queries ───────────────────────────────────────────────

    public boolean isDeletedForEveryone() { return deletedForEveryoneAt != null; }
    public boolean isDeletedForSender()   { return deletedForSenderAt != null; }

    private void validateSender(String requesterId) {
        if (!senderId.equals(requesterId))
            throw new MessageDomainException("Only the sender can perform this action");
    }

    // ── Getters ───────────────────────────────────────────────

    public String getId()                        { return id; }
    public String getSenderId()                  { return senderId; }
    public String getChatId()                    { return chatId; }
    public String getContent()                   { return content; }
    public List<String> getAttachedFilePaths()   { return Collections.unmodifiableList(attachedFilePaths); }
    public boolean isRead()                      { return isRead; }
    public LocalDateTime getDeletedForEveryoneAt(){ return deletedForEveryoneAt; }
    public LocalDateTime getDeletedForSenderAt() { return deletedForSenderAt; }
    public LocalDateTime getSentAt()             { return sentAt; }
    public LocalDateTime getUpdatedAt()          { return updatedAt; }
}