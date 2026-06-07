package com.stu.socialnetworkapi.service.impl;

import com.stu.socialnetworkapi.config.WebSocketChannelPrefix;
import com.stu.socialnetworkapi.dto.projection.PinnedMessageProjection;
import com.stu.socialnetworkapi.dto.request.CreateGroupRequest;
import com.stu.socialnetworkapi.dto.request.GroupMemberRequest;
import com.stu.socialnetworkapi.dto.request.UpdateGroupRequest;
import com.stu.socialnetworkapi.dto.response.ChatResponse;
import com.stu.socialnetworkapi.dto.response.GroupMemberResponse;
import com.stu.socialnetworkapi.dto.response.MessageCommand;
import com.stu.socialnetworkapi.entity.Chat;
import com.stu.socialnetworkapi.entity.File;
import com.stu.socialnetworkapi.entity.User;
import com.stu.socialnetworkapi.enums.GroupRole;
import com.stu.socialnetworkapi.exception.ApiException;
import com.stu.socialnetworkapi.exception.ErrorCode;
import com.stu.socialnetworkapi.mapper.ChatMapper;
import com.stu.socialnetworkapi.repository.neo4j.ChatRepository;
import com.stu.socialnetworkapi.repository.neo4j.UserRepository;
import com.stu.socialnetworkapi.repository.redis.InChatRepository;
import com.stu.socialnetworkapi.service.itf.FileService;
import com.stu.socialnetworkapi.service.itf.GroupService;
import com.stu.socialnetworkapi.service.itf.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class GroupServiceImpl implements GroupService {

    private final UserService userService;
    private final FileService fileService;
    private final ChatMapper chatMapper;
    private final ChatRepository chatRepository;
    private final UserRepository userRepository;
    private final InChatRepository inChatRepository;
    private final SimpMessagingTemplate messagingTemplate;

    // ── CREATE ────────────────────────────────────────────────────────────────

    @Override
    public ChatResponse createGroup(CreateGroupRequest request) {
        User creator = userService.getCurrentUserRequiredAuthentication();

        List<User> members = new ArrayList<>();
        members.add(creator);

        for (String username : request.memberUsernames()) {
            if (!username.equals(creator.getUsername())) {
                User member = userService.getUser(username);
                members.add(member);
            }
        }

        if (members.size() < 2) {
            throw new ApiException(ErrorCode.GROUP_MUST_HAVE_AT_LEAST_2_MEMBERS);
        }

        Chat group = Chat.builder()
                .isGroup(true)
                .groupName(request.name())
                .members(members)
                .build();

        Chat saved = chatRepository.save(group);

        // Set creator as OWNER
        chatRepository.setMemberRole(creator.getId(), saved.getId(), GroupRole.OWNER.name());

        // Invalidate cache for all members
        members.forEach(m -> inChatRepository.invalidateUserChat(m.getId()));

        // Notify all members
        notifyGroupEvent(saved.getId(), members, "GROUP_CREATED",
                creator.getGivenName() + " đã tạo nhóm " + request.name());

        return buildGroupChatResponse(saved, creator, GroupRole.OWNER);
    }

    // ── UPDATE ────────────────────────────────────────────────────────────────

    @Override
    public ChatResponse updateGroup(UpdateGroupRequest request) {
        User currentUser = userService.getCurrentUserRequiredAuthentication();
        validateRole(currentUser.getId(), request.chatId(), GroupRole.ADMIN, GroupRole.OWNER);

        String avatarFileId = null;
        if (request.avatar() != null && !request.avatar().isEmpty()) {
            File file = fileService.upload(request.avatar());
            avatarFileId = file.getId().toString();
        }

        chatRepository.updateGroupInfo(request.chatId(), request.name(), avatarFileId);

        Chat chat = chatRepository.findById(request.chatId())
                .orElseThrow(() -> new ApiException(ErrorCode.CHAT_NOT_FOUND));

        GroupRole myRole = GroupRole.valueOf(
                chatRepository.getMemberRole(currentUser.getId(), request.chatId()));

        // Notify group members
        notifyGroupEvent(request.chatId(), chat.getMembers(), "GROUP_UPDATED",
                currentUser.getGivenName() + " đã cập nhật thông tin nhóm");

        return buildGroupChatResponse(chat, currentUser, myRole);
    }

    // ── MEMBERS ───────────────────────────────────────────────────────────────

    @Override
    public void addMembers(GroupMemberRequest request) {
        User currentUser = userService.getCurrentUserRequiredAuthentication();
        // Any member can add others
        validateIsMember(currentUser.getId(), request.chatId());

        Chat chat = chatRepository.findById(request.chatId())
                .orElseThrow(() -> new ApiException(ErrorCode.CHAT_NOT_FOUND));

        List<String> added = new ArrayList<>();
        for (String username : request.usernames()) {
            chatRepository.addMember(username, request.chatId());
            added.add(username);
        }

        inChatRepository.invalidateUserChat(currentUser.getId());

        // Notify all existing members
        notifyGroupEvent(request.chatId(), chat.getMembers(), "MEMBERS_ADDED",
                currentUser.getGivenName() + " đã thêm " + String.join(", ", added) + " vào nhóm");
    }

    @Override
    public void removeMember(GroupMemberRequest request) {
        User currentUser = userService.getCurrentUserRequiredAuthentication();
        validateRole(currentUser.getId(), request.chatId(), GroupRole.ADMIN, GroupRole.OWNER);

        User target = userService.getUser(request.username());

        // Owner can remove anyone; Admin cannot remove Owner
        String targetRole = chatRepository.getMemberRole(target.getId(), request.chatId());
        if (GroupRole.OWNER.name().equals(targetRole)) {
            throw new ApiException(ErrorCode.CANNOT_REMOVE_OWNER);
        }

        // Admin cannot remove another Admin unless they're OWNER
        String myRole = chatRepository.getMemberRole(currentUser.getId(), request.chatId());
        if (GroupRole.ADMIN.name().equals(targetRole) && !GroupRole.OWNER.name().equals(myRole)) {
            throw new ApiException(ErrorCode.INSUFFICIENT_GROUP_PERMISSION);
        }

        chatRepository.removeMember(target.getId(), request.chatId());
        inChatRepository.invalidateUserChat(target.getId());

        Chat chat = chatRepository.findById(request.chatId())
                .orElseThrow(() -> new ApiException(ErrorCode.CHAT_NOT_FOUND));

        notifyGroupEvent(request.chatId(), chat.getMembers(), "MEMBER_REMOVED",
                currentUser.getGivenName() + " đã xóa " + target.getGivenName() + " khỏi nhóm");
    }

    @Override
    public void leaveGroup(UUID chatId) {
        User currentUser = userService.getCurrentUserRequiredAuthentication();
        validateIsMember(currentUser.getId(), chatId);

        String role = chatRepository.getMemberRole(currentUser.getId(), chatId);
        if (GroupRole.OWNER.name().equals(role)) {
            throw new ApiException(ErrorCode.OWNER_MUST_TRANSFER_BEFORE_LEAVE);
        }

        chatRepository.removeMember(currentUser.getId(), chatId);
        inChatRepository.invalidateUserChat(currentUser.getId());

        Chat chat = chatRepository.findById(chatId)
                .orElseThrow(() -> new ApiException(ErrorCode.CHAT_NOT_FOUND));

        notifyGroupEvent(chatId, chat.getMembers(), "MEMBER_LEFT",
                currentUser.getGivenName() + " đã rời nhóm");
    }

    @Override
    public void dissolveGroup(UUID chatId) {
        User currentUser = userService.getCurrentUserRequiredAuthentication();
        validateRole(currentUser.getId(), chatId, GroupRole.OWNER);

        Chat chat = chatRepository.findById(chatId)
                .orElseThrow(() -> new ApiException(ErrorCode.CHAT_NOT_FOUND));

        // Notify before dissolving
        notifyGroupEvent(chatId, chat.getMembers(), "GROUP_DISSOLVED",
                currentUser.getGivenName() + " đã giải tán nhóm");

        chat.getMembers().forEach(m -> inChatRepository.invalidateUserChat(m.getId()));

        chatRepository.dissolveGroup(chatId);
    }

    // ── ROLES ─────────────────────────────────────────────────────────────────

    @Override
    public void promoteToAdmin(GroupMemberRequest request) {
        User currentUser = userService.getCurrentUserRequiredAuthentication();
        validateRole(currentUser.getId(), request.chatId(), GroupRole.OWNER);

        User target = userService.getUser(request.username());
        chatRepository.setMemberRole(target.getId(), request.chatId(), GroupRole.ADMIN.name());

        Chat chat = chatRepository.findById(request.chatId())
                .orElseThrow(() -> new ApiException(ErrorCode.CHAT_NOT_FOUND));

        notifyGroupEvent(request.chatId(), chat.getMembers(), "ROLE_CHANGED",
                target.getGivenName() + " được nâng lên Admin");
    }

    @Override
    public void demoteToMember(GroupMemberRequest request) {
        User currentUser = userService.getCurrentUserRequiredAuthentication();
        validateRole(currentUser.getId(), request.chatId(), GroupRole.OWNER);

        User target = userService.getUser(request.username());
        chatRepository.setMemberRole(target.getId(), request.chatId(), GroupRole.MEMBER.name());

        Chat chat = chatRepository.findById(request.chatId())
                .orElseThrow(() -> new ApiException(ErrorCode.CHAT_NOT_FOUND));

        notifyGroupEvent(request.chatId(), chat.getMembers(), "ROLE_CHANGED",
                target.getGivenName() + " đã được hạ xuống Member");
    }

    @Override
    public void transferOwnership(GroupMemberRequest request) {
        User currentUser = userService.getCurrentUserRequiredAuthentication();
        validateRole(currentUser.getId(), request.chatId(), GroupRole.OWNER);

        User target = userService.getUser(request.username());

        chatRepository.setMemberRole(currentUser.getId(), request.chatId(), GroupRole.MEMBER.name());
        chatRepository.setMemberRole(target.getId(), request.chatId(), GroupRole.OWNER.name());

        Chat chat = chatRepository.findById(request.chatId())
                .orElseThrow(() -> new ApiException(ErrorCode.CHAT_NOT_FOUND));

        notifyGroupEvent(request.chatId(), chat.getMembers(), "OWNERSHIP_TRANSFERRED",
                currentUser.getGivenName() + " đã chuyển quyền trưởng nhóm cho " + target.getGivenName());
    }

    // ── MEMBERS LIST ──────────────────────────────────────────────────────────

    @Override
    public List<GroupMemberResponse> getMembers(UUID chatId) {
        User currentUser = userService.getCurrentUserRequiredAuthentication();
        validateIsMember(currentUser.getId(), chatId);

        return chatRepository.getGroupMembers(chatId).stream()
                .map(p -> GroupMemberResponse.builder()
                        .userId(p.userId().toString())
                        .username(p.username())
                        .givenName(p.givenName())
                        .familyName(p.familyName())
                        .profilePictureUrl(File.getPath(p.profilePictureId()))
                        .role(GroupRole.valueOf(p.role()))
                        .joinedAt(p.joinedAt())
                        .build())
                .toList();
    }

    // ── PIN ───────────────────────────────────────────────────────────────────

    @Override
    public void pinMessage(UUID chatId, UUID messageId) {
        User currentUser = userService.getCurrentUserRequiredAuthentication();
        validateRole(currentUser.getId(), chatId, GroupRole.ADMIN, GroupRole.OWNER);
        chatRepository.pinMessage(chatId, messageId);

        Chat chat = chatRepository.findById(chatId)
                .orElseThrow(() -> new ApiException(ErrorCode.CHAT_NOT_FOUND));

        notifyGroupEvent(chatId, chat.getMembers(), "MESSAGE_PINNED",
                currentUser.getGivenName() + " đã ghim một tin nhắn");
    }

    @Override
    public void unpinMessage(UUID chatId, UUID messageId) {
        User currentUser = userService.getCurrentUserRequiredAuthentication();
        validateRole(currentUser.getId(), chatId, GroupRole.ADMIN, GroupRole.OWNER);
        chatRepository.unpinMessage(chatId, messageId);
    }

    @Override
    public List<PinnedMessageProjection> getPinnedMessages(UUID chatId) {
        User currentUser = userService.getCurrentUserRequiredAuthentication();
        validateIsMember(currentUser.getId(), chatId);
        return chatRepository.getPinnedMessages(chatId);
    }

    // ── HELPERS ───────────────────────────────────────────────────────────────

    private void validateIsMember(UUID userId, UUID chatId) {
        if (!inChatRepository.isInChat(userId, chatId)) {
            throw new ApiException(ErrorCode.NOT_MEMBER_OF_GROUP);
        }
    }

    private void validateRole(UUID userId, UUID chatId, GroupRole... allowedRoles) {
        validateIsMember(userId, chatId);
        String roleStr = chatRepository.getMemberRole(userId, chatId);
        if (roleStr == null) throw new ApiException(ErrorCode.NOT_MEMBER_OF_GROUP);
        GroupRole role = GroupRole.valueOf(roleStr);
        for (GroupRole allowed : allowedRoles) {
            if (role == allowed) return;
        }
        throw new ApiException(ErrorCode.INSUFFICIENT_GROUP_PERMISSION);
    }

    private void notifyGroupEvent(UUID chatId, List<User> members, String eventType, String description) {
        if (members == null) return;
        MessageCommand command = MessageCommand.builder()
                .command(MessageCommand.Command.valueOf("GROUP_EVENT"))
                .build();
        // Broadcast to all members via WebSocket
        members.forEach(member ->
                messagingTemplate.convertAndSendToUser(
                        member.getUsername(),
                        WebSocketChannelPrefix.CHAT_TOPIC,
                        command
                )
        );
    }

    private ChatResponse buildGroupChatResponse(Chat chat, User currentUser, GroupRole myRole) {
        return ChatResponse.builder()
                .chatId(chat.getId())
                .name(chat.getGroupName())
                .isGroup(true)
                .groupAvatarUrl(chat.getGroupAvatarFileId())
                .myRole(myRole)
                .memberCount(chat.getMembers() != null ? chat.getMembers().size() : 0)
                .build();
    }
}