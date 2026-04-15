package com.socialapp.domain.model.entity;

import com.socialapp.domain.model.valueobject.MessageContent;
import com.socialapp.domain.model.valueobject.UserId;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/**
 * Entity: Message
 * Identity: id
 * Thuộc Aggregate Chat.
 *
 * Rules:
 *   - Chỉ xóa/sửa được trong vòng 15 phút sau khi gửi
 *   - Chỉ sửa được nội dung text (không sửa file)
 */
public class Message {

    private static final int EDIT_DELETE_WINDOW_MINUTES = 15;

    private final String          id;
    private final UserId          senderId;
    private final LocalDateTime   sentAt;

    private MessageContent        content;
    private boolean               isRead;
    private LocalDateTime         updatedAt;
    private LocalDateTime         deletedAt;
    private FileEntity            attachedFile;

    public Message(String id, UserId senderId, MessageContent content) {
        this.id       = id;
        this.senderId = senderId;
        this.content  = content;
        this.sentAt   = LocalDateTime.now();
        this.isRead   = false;
    }

    /** Constructor load từ DB */
    public Message(String id, UserId senderId, MessageContent content,
                   LocalDateTime sentAt, boolean isRead,
                   LocalDateTime updatedAt, LocalDateTime deletedAt) {
        this.id        = id;
        this.senderId  = senderId;
        this.content   = content;
        this.sentAt    = sentAt;
        this.isRead    = isRead;
        this.updatedAt = updatedAt;
        this.deletedAt = deletedAt;
    }

    // ── Business methods ─────────────────────────────────────

    private boolean withinEditWindow() {
        return ChronoUnit.MINUTES.between(sentAt, LocalDateTime.now()) < EDIT_DELETE_WINDOW_MINUTES;
    }

    /** Rule: chỉ sửa nội dung text, không sửa file, trong 15 phút */
    public void editContent(MessageContent newContent) {
        if (!withinEditWindow())
            throw new IllegalStateException("Cannot edit message after 15 minutes");
        if (deletedAt != null)
            throw new IllegalStateException("Cannot edit a deleted message");
        this.content   = newContent;
        this.updatedAt = LocalDateTime.now();
    }

    /** Rule: chỉ xóa trong 15 phút */
    public void softDelete() {
        if (!withinEditWindow())
            throw new IllegalStateException("Cannot delete message after 15 minutes");
        this.deletedAt = LocalDateTime.now();
    }

    public void markRead()              { this.isRead = true; }
    public void attachFile(FileEntity f){ this.attachedFile = f; }

    public boolean isDeleted()          { return deletedAt != null; }

    // ── Getters ──────────────────────────────────────────────

    public String          getId()           { return id; }
    public UserId          getSenderId()     { return senderId; }
    public MessageContent  getContent()      { return content; }
    public LocalDateTime   getSentAt()       { return sentAt; }
    public boolean         isRead()          { return isRead; }
    public LocalDateTime   getUpdatedAt()    { return updatedAt; }
    public LocalDateTime   getDeletedAt()    { return deletedAt; }
    public FileEntity      getAttachedFile() { return attachedFile; }

    @Override public boolean equals(Object o) {
        if (!(o instanceof Message)) return false;
        return id.equals(((Message) o).id);
    }
    @Override public int hashCode() { return id.hashCode(); }
}