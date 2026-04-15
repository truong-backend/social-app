package com.socialapp.domain.model.aggregate;

import com.socialapp.domain.model.entity.Call;
import com.socialapp.domain.model.entity.Message;
import com.socialapp.domain.model.valueobject.UserId;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Aggregate Root: Chat
 * ─────────────────────────────────────────────────────────────
 * Quản lý đoạn hội thoại + danh sách thành viên + tin nhắn.
 *
 * Rules:
 *   - Chỉ thành viên của chat mới gửi được tin nhắn
 *   - Xóa / sửa tin nhắn chỉ trong 15 phút (enforce trong Message entity)
 * ─────────────────────────────────────────────────────────────
 * Quan hệ trong graph:
 *   User --IS_MEMBER_OF--> Chat
 *   User --SENT--> Message
 *   Chat --HAS_MESSAGE--> Message
 *   Message <|-- Call  (Subclass of)
 */
public class Chat {

    // ── Identity ─────────────────────────────────────────────
    private final String id;

    // ── Timestamps ────────────────────────────────────────────
    private final LocalDateTime createdAt;

    // ── Members ───────────────────────────────────────────────
    private final List<UserId>  memberIds = new ArrayList<>();

    // ── Messages (contains both Message & Call) ───────────────
    private final List<Message> messages  = new ArrayList<>();

    // ── Constructors ─────────────────────────────────────────

    public Chat(String id) {
        this.id        = id;
        this.createdAt = LocalDateTime.now();
    }

    public Chat(String id, LocalDateTime createdAt) {
        this.id        = id;
        this.createdAt = createdAt;
    }

    // ── Business Methods ─────────────────────────────────────

    public void addMember(UserId userId) {
        boolean exists = memberIds.stream().anyMatch(m -> m.equals(userId));
        if (!exists) memberIds.add(userId);
    }

    public void removeMember(UserId userId) {
        memberIds.removeIf(m -> m.equals(userId));
    }

    public boolean isMember(UserId userId) {
        return memberIds.stream().anyMatch(m -> m.equals(userId));
    }

    /** Rule: chỉ member mới gửi được tin nhắn */
    public void sendMessage(Message message) {
        if (!isMember(message.getSenderId()))
            throw new IllegalStateException("Sender is not a member of this chat");
        messages.add(message);
    }

    /**
     * Dùng nội bộ khi reconstruct từ persistence — không enforce member check.
     * Không dùng trong business logic.
     */
    public void loadMessage(Message message) {
        messages.add(message);
    }

    public Optional<Message> findMessageById(String messageId) {
        return messages.stream().filter(m -> m.getId().equals(messageId)).findFirst();
    }

    // ── Getters ──────────────────────────────────────────────

    public String            getId()        { return id; }
    public LocalDateTime     getCreatedAt() { return createdAt; }
    public List<UserId>      getMembers()   { return Collections.unmodifiableList(memberIds); }
    public List<Message>     getMessages()  { return Collections.unmodifiableList(messages); }

    @Override public boolean equals(Object o) {
        if (!(o instanceof Chat)) return false;
        return id.equals(((Chat) o).id);
    }
    @Override public int hashCode() { return id.hashCode(); }
}