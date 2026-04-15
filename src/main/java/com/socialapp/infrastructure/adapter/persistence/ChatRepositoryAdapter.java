package com.socialapp.infrastructure.adapter.persistence;

import com.socialapp.domain.model.aggregate.Chat;
import com.socialapp.domain.model.entity.Call;
import com.socialapp.domain.model.entity.FileEntity;
import com.socialapp.domain.model.entity.Message;
import com.socialapp.domain.model.valueobject.FileMeta;
import com.socialapp.domain.model.valueobject.MessageContent;
import com.socialapp.domain.model.valueobject.UserId;
import com.socialapp.domain.repository.ChatRepository;
import com.socialapp.infrastructure.adapter.persistence.neo4j.node.ChatNode;
import com.socialapp.infrastructure.adapter.persistence.neo4j.node.FileNode;
import com.socialapp.infrastructure.adapter.persistence.neo4j.node.MessageNode;
import com.socialapp.infrastructure.adapter.persistence.neo4j.repository.ChatNeo4jRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class ChatRepositoryAdapter implements ChatRepository {

    private final ChatNeo4jRepository chatRepo;

    public ChatRepositoryAdapter(ChatNeo4jRepository chatRepo) {
        this.chatRepo = chatRepo;
    }

    // ── Domain → Node ────────────────────────────────────────────────────

    private ChatNode toNode(Chat chat) {
        ChatNode node = new ChatNode(chat.getId(), chat.getCreatedAt());

        // @Relationship HAS_MESSAGE (ChatNode → MessageNode)
        List<MessageNode> msgNodes = chat.getMessages().stream()
                .map(this::toMessageNode)
                .toList();
        node.setMessages(msgNodes);

        return node;
    }

    private MessageNode toMessageNode(Message msg) {
        MessageNode mn = new MessageNode(
                msg.getId(),
                msg.getContent().getValue(),
                msg.getSentAt(),
                msg.isRead(),
                msg.getUpdatedAt(),
                msg.getDeletedAt(),
                null, null, null, null, null, null
        );

        // @Relationship ATTACH_FILE (MessageNode → FileNode)
        if (msg.getAttachedFile() != null) {
            FileMeta meta = msg.getAttachedFile().getMeta();
            mn.setAttachedFile(new FileNode(
                    meta.getPath(), meta.getName(),
                    meta.getContentType(), meta.getSizeBytes()));
        }

        // Call fields
        if (msg instanceof Call call) {
            mn.setCallId(call.getCallId());
            mn.setCallAt(call.getCallAt());
            mn.setEndAt(call.getEndAt());
            mn.setIsAnswered(call.isAnswered());
            mn.setIsEnded(call.isEnded());
            mn.setIsVideoCall(call.isVideoCall());
        }

        return mn;
    }

    // ── Node → Domain ────────────────────────────────────────────────────

    private Chat toDomain(ChatNode node) {
        Chat chat = new Chat(node.getId(), node.getCreatedAt());

        // @Relationship HAS_MESSAGE → SDN4j load tự động qua ChatNode.messages
        if (node.getMessages() != null) {
            node.getMessages().stream()
                    .map(this::toMessageDomain)
                    .forEach(chat::loadMessage);
        }

        return chat;
    }

    private Chat toDomain(ChatNode node, List<UserId> memberIds) {
        Chat chat = toDomain(node);
        memberIds.forEach(chat::addMember);
        return chat;
    }

    private Message toMessageDomain(MessageNode mn) {
        // sender: UserNode.sendUsers (@Relationship SENT) — MessageNode không lưu senderId.
        UserId senderPlaceholder = new UserId("unknown");

        if (mn.isCall()) {
            Call call = new Call(mn.getId(), senderPlaceholder,
                    Boolean.TRUE.equals(mn.getIsVideoCall()));
            if (mn.getCallId() != null) call.setThirdPartyCallId(mn.getCallId());
            if (Boolean.TRUE.equals(mn.getIsAnswered())) call.answer();
            if (Boolean.TRUE.equals(mn.getIsEnded()))    call.end();
            return call;
        }

        Message msg = new Message(
                mn.getId(),
                senderPlaceholder,
                new MessageContent(mn.getContent() != null ? mn.getContent() : ""),
                mn.getSentAt(),
                mn.isRead(),
                mn.getUpdatedAt(),
                mn.getDeletedAt()
        );

        // @Relationship ATTACH_FILE → SDN4j load tự động
        if (mn.getAttachedFile() != null) {
            FileNode fn = mn.getAttachedFile();
            msg.attachFile(new FileEntity(
                    new FileMeta(fn.getPath(), fn.getName(),
                            fn.getContentType(), fn.getSizeBytes())));
        }

        return msg;
    }

    // ── Repository impl ──────────────────────────────────────────────────

    @Override
    public Optional<Chat> findById(String id) {
        return chatRepo.findById(id).map(this::toDomain);
    }

    /**
     * findByMemberId: @Query Cypher traverse IS_MEMBER_OF, kèm HAS_MESSAGE.
     * UserNeo4jRepository không cần thiết ở đây — ChatNeo4jRepository handle trực tiếp.
     * Loại bỏ: userRepo.findById() + navigation qua UserNode.chats.
     */
    @Override
    public List<Chat> findByMemberId(UserId userId) {
        return chatRepo.findByMemberId(userId.getValue())
                .stream()
                .map(chatNode -> {
                    List<UserId> memberIds = chatRepo
                            .findMemberIdsByChatId(chatNode.getId())
                            .stream()
                            .map(UserId::new)
                            .toList();
                    return toDomain(chatNode, memberIds);
                })
                .toList();
    }

    /**
     * findPrivateChat: @Query Cypher tìm chat chung của 2 user.
     * Loại bỏ: load cả 2 UserNode + set intersection in-memory.
     */
    @Override
    public Optional<Chat> findPrivateChat(UserId userA, UserId userB) {
        return chatRepo.findPrivateChat(userA.getValue(), userB.getValue())
                .map(chatNode -> toDomain(chatNode, List.of(userA, userB)));
    }

    /**
     * save: lưu ChatNode + messages, sau đó tạo IS_MEMBER_OF relationship
     * bằng Cypher MERGE — không load UserNode.
     *
     * Loại bỏ anti-pattern: userRepo.findById() → mutate .chats list → userRepo.save()
     * (tốn 2 round-trip per member + eager-load toàn bộ UserNode relationships).
     */
    @Override
    public void save(Chat chat) {
        chatRepo.save(toNode(chat));

        // Tạo IS_MEMBER_OF relationships trực tiếp qua Cypher MERGE
        chat.getMembers().forEach(memberId ->
                chatRepo.addMember(chat.getId(), memberId.getValue())
        );
    }

    @Override
    public void delete(String id) {
        chatRepo.deleteById(id);
    }
}