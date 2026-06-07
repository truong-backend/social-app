package com.stu.socialnetworkapi.service.impl;

import com.stu.socialnetworkapi.config.WebSocketChannelPrefix;
import com.stu.socialnetworkapi.dto.request.*;
import com.stu.socialnetworkapi.dto.response.MessageCommand;
import com.stu.socialnetworkapi.dto.response.MessageResponse;
import com.stu.socialnetworkapi.entity.Chat;
import com.stu.socialnetworkapi.entity.File;
import com.stu.socialnetworkapi.entity.Message;
import com.stu.socialnetworkapi.entity.User;
import com.stu.socialnetworkapi.enums.MessageType;
import com.stu.socialnetworkapi.exception.ApiException;
import com.stu.socialnetworkapi.exception.ErrorCode;
import com.stu.socialnetworkapi.exception.WebSocketException;
import com.stu.socialnetworkapi.mapper.MessageMapper;
import com.stu.socialnetworkapi.repository.neo4j.MessageRepository;
import com.stu.socialnetworkapi.repository.redis.InChatRepository;
import com.stu.socialnetworkapi.repository.redis.IsTypingRepository;
import com.stu.socialnetworkapi.service.itf.ChatService;
import com.stu.socialnetworkapi.service.itf.FileService;
import com.stu.socialnetworkapi.service.itf.MessageService;
import com.stu.socialnetworkapi.service.itf.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.stu.socialnetworkapi.repository.neo4j.ChatRepository;

import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class MessageServiceImpl implements MessageService {
    private final UserService userService;
    private final FileService fileService;
    private final ChatService chatService;
    private final MessageMapper messageMapper;
    private final MessageRepository messageRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final InChatRepository inChatRepository;
    private final IsTypingRepository isTypingRepository;
    private final ChatRepository chatRepository;

    @Override
    public MessageResponse sendMessage(TextMessageRequest request) {
        User sender = userService.getCurrentUserRequiredAuthentication();
        User receiver = userService.getUser(request.username());
        Chat chat = chatService.getOrCreateDirectChat(sender, receiver);
        String content = request.text().trim();
        if (content.isEmpty()) throw new ApiException(ErrorCode.TEXT_MESSAGE_CONTENT_REQUIRED);
        if (content.length() > Message.MAX_CONTENT_LENGTH)
            throw new ApiException(ErrorCode.INVALID_MESSAGE_CONTENT_LENGTH);

        Message message = Message.builder()
                .chat(chat)
                .content(content)
                .sender(sender)
                .isRead(inChatRepository.isSubscribed(receiver.getId(), chat.getId()))
                .build();

        messageRepository.save(message);
        MessageResponse response = messageMapper.toMessageResponse(message);
        sendMessageNotification(chat.getId(), receiver.getId(), response);
        return response;
    }

    @Override
    public MessageResponse sendGroupMessage(GroupTextMessageRequest request, UUID chatId) {
        User sender = userService.getCurrentUserRequiredAuthentication();

        inChatRepository.invalidateUserChat(sender.getId());

        if (!inChatRepository.isInChat(sender.getId(), chatId)) {
            throw new ApiException(ErrorCode.NOT_MEMBER_OF_GROUP);
        }

        Chat chat = chatRepository.findById(chatId)
                .orElseThrow(() -> new ApiException(ErrorCode.CHAT_NOT_FOUND));

        String content = request.text().trim();
        if (content.isEmpty()) throw new ApiException(ErrorCode.TEXT_MESSAGE_CONTENT_REQUIRED);
        if (content.length() > Message.MAX_CONTENT_LENGTH)
            throw new ApiException(ErrorCode.INVALID_MESSAGE_CONTENT_LENGTH);

        Message message = Message.builder()
                .chat(chat)
                .content(content)
                .sender(sender)
                .isRead(false)
                .build();

        messageRepository.save(message);
        MessageResponse response = messageMapper.toMessageResponse(message);

        // ✅ FIX duplicate: broadcast 1 lần duy nhất lên /chat/{chatId}
        sendGroupMessageNotification(chatId, sender.getId(), chat.getMembers(), response);

        return response;
    }

    @Override
    public MessageResponse sendMessage(TextMessageRequest request, UUID userId) {
        User sender = userService.getUser(userId);
        User receiver = userService.getUser(request.username());
        Chat chat = chatService.getOrCreateDirectChat(sender, receiver);
        String content = request.text().trim();
        if (content.isEmpty()) throw new WebSocketException(ErrorCode.TEXT_MESSAGE_CONTENT_REQUIRED);
        if (content.length() > Message.MAX_CONTENT_LENGTH)
            throw new WebSocketException(ErrorCode.INVALID_MESSAGE_CONTENT_LENGTH);

        Message message = Message.builder()
                .chat(chat)
                .content(content)
                .sender(sender)
                .isRead(inChatRepository.isSubscribed(receiver.getId(), chat.getId()))
                .build();

        messageRepository.save(message);
        MessageResponse response = messageMapper.toMessageResponse(message);
        sendMessageNotification(chat.getId(), receiver.getId(), response);
        return response;
    }

    @Override
    public MessageResponse sendFile(FileMessageRequest request) {
        User sender = userService.getCurrentUserRequiredAuthentication();
        User receiver = userService.getUser(request.username());
        Chat chat = chatService.getOrCreateDirectChat(sender, receiver);
        File file = fileService.upload(request.attachment());
        Message message = Message.builder()
                .chat(chat)
                .sender(sender)
                .type(MessageType.FILE)
                .attachedFile(file)
                .isRead(inChatRepository.isSubscribed(receiver.getId(), chat.getId()))
                .build();
        messageRepository.save(message);
        MessageResponse response = messageMapper.toMessageResponse(message);
        sendMessageNotification(chat.getId(), receiver.getId(), response);
        return response;
    }

    @Override
    public MessageResponse sendGif(GifMessageRequest request) {
        User sender = userService.getCurrentUserRequiredAuthentication();
        User receiver = userService.getUser(request.username());
        Chat chat = chatService.getOrCreateDirectChat(sender, receiver);
        Message message = Message.builder()
                .content(request.url())
                .chat(chat)
                .sender(sender)
                .type(MessageType.GIF)
                .isRead(inChatRepository.isSubscribed(receiver.getId(), chat.getId()))
                .build();
        messageRepository.save(message);
        MessageResponse response = messageMapper.toMessageResponse(message);
        sendMessageNotification(chat.getId(), receiver.getId(), response);
        return response;
    }

    @Override
    public MessageResponse sendVoice(VoiceMessageRequest request) {
        User sender = userService.getCurrentUserRequiredAuthentication();
        User receiver = userService.getUser(request.username());
        Chat chat = chatService.getOrCreateDirectChat(sender, receiver);
        File file = fileService.upload(request.voiceFile());
        Message message = Message.builder()
                .chat(chat)
                .sender(sender)
                .type(MessageType.VOICE)
                .attachedFile(file)
                .isRead(inChatRepository.isSubscribed(receiver.getId(), chat.getId()))
                .build();
        messageRepository.save(message);
        MessageResponse response = messageMapper.toMessageResponse(message);
        sendMessageNotification(chat.getId(), receiver.getId(), response);
        return response;
    }

    // ── Group file message (FileMessageRequest - giữ lại cho backward compat) ─
    @Override
    public MessageResponse sendGroupFile(FileMessageRequest request, UUID chatId) {
        User sender = userService.getCurrentUserRequiredAuthentication();

        inChatRepository.invalidateUserChat(sender.getId());

        if (!inChatRepository.isInChat(sender.getId(), chatId)) {
            throw new ApiException(ErrorCode.NOT_MEMBER_OF_GROUP);
        }

        Chat chat = chatRepository.findById(chatId)
                .orElseThrow(() -> new ApiException(ErrorCode.CHAT_NOT_FOUND));

        File file = fileService.upload(request.attachment());

        Message message = Message.builder()
                .chat(chat)
                .sender(sender)
                .type(MessageType.FILE)
                .attachedFile(file)
                .isRead(false)
                .build();

        messageRepository.save(message);
        MessageResponse response = messageMapper.toMessageResponse(message);

        // ✅ FIX duplicate: broadcast 1 lần duy nhất lên /chat/{chatId}
        sendGroupMessageNotification(chatId, sender.getId(), chat.getMembers(), response);

        return response;
    }

    // ✅ FIX: Group file message dùng GroupFileMessageRequest (không cần username)
    @Override
    public MessageResponse sendGroupFile(GroupFileMessageRequest request, UUID chatId) {
        User sender = userService.getCurrentUserRequiredAuthentication();

        inChatRepository.invalidateUserChat(sender.getId());

        if (!inChatRepository.isInChat(sender.getId(), chatId)) {
            throw new ApiException(ErrorCode.NOT_MEMBER_OF_GROUP);
        }

        Chat chat = chatRepository.findById(chatId)
                .orElseThrow(() -> new ApiException(ErrorCode.CHAT_NOT_FOUND));

        File file = fileService.upload(request.attachment());

        Message message = Message.builder()
                .chat(chat)
                .sender(sender)
                .type(MessageType.FILE)
                .attachedFile(file)
                .isRead(false)
                .build();

        messageRepository.save(message);
        MessageResponse response = messageMapper.toMessageResponse(message);

        // ✅ FIX duplicate: broadcast 1 lần duy nhất lên /chat/{chatId}
        sendGroupMessageNotification(chatId, sender.getId(), chat.getMembers(), response);

        return response;
    }

    @Override
    public List<MessageResponse> getHistory(UUID chatId, Neo4jPageable pageable) {
        UUID userId = userService.getCurrentUserIdRequiredAuthentication();
        if (!inChatRepository.isInChat(userId, chatId)) {
            throw new ApiException(ErrorCode.UNAUTHORIZED);
        }
        List<MessageResponse> messages = messageRepository.findAllByChatIdOrderBySentAtDesc(chatId, pageable.getSkip(), pageable.getLimit()).stream()
                .map(messageMapper::toMessageResponse)
                .toList();

        if (pageable.getSkip() <= pageable.getLimit())
            messageRepository.markAsRead(chatId, userId);
        return messages;
    }

    @Override
    public void editMessage(EditMessageRequest request) {
        Message message = messageRepository.findById(request.messagesId())
                .orElseThrow(() -> new ApiException(ErrorCode.MESSAGE_NOT_FOUND));
        validateEditMessage(message, request.text());
        String content = request.text().trim();
        message.setContent(content);
        message.setUpdateAt(ZonedDateTime.now());
        messageRepository.save(message);
        MessageCommand command = MessageCommand.builder()
                .id(String.valueOf(message.getId()))
                .command(MessageCommand.Command.EDIT)
                .message(content)
                .build();
        sendMessageCommand(message.getChat().getId(), command);
    }

    private void sendMessageCommand(UUID chatId, MessageCommand command) {
        messagingTemplate.convertAndSend(WebSocketChannelPrefix.CHAT_CHANNEL_PREFIX + "/" + chatId, command);
    }

    @Override
    public void deleteMessage(UUID messageId) {
        User user = userService.getCurrentUserRequiredAuthentication();
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new ApiException(ErrorCode.MESSAGE_NOT_FOUND));
        Chat chat = message.getChat();
        validateDeleteMessage(message, user);
        message.setDeleteAt(ZonedDateTime.now());
        message.setContent("deleted");
        messageRepository.save(message);
        if (message.getAttachedFile() != null) {
            fileService.deleteFile(message.getAttachedFile());
        }
        MessageCommand command = MessageCommand.builder()
                .id(String.valueOf(messageId))
                .command(MessageCommand.Command.DELETE)
                .build();
        sendMessageCommand(chat.getId(), command);
    }

    @Override
    public void typing(UserTypingRequest request) {
        MessageCommand command = MessageCommand.builder()
                .command(request.isTyping() ? MessageCommand.Command.TYPING : MessageCommand.Command.STOP_TYPING)
                .id(String.valueOf(request.userId()))
                .build();
        if (request.isTyping())
            isTypingRepository.save(request.userId(), request.chatId());
        else isTypingRepository.delete(request.userId(), request.chatId());
        sendMessageCommand(request.chatId(), command);
    }

    private static void validateDeleteMessage(Message message, User user) {
        if (!message.getSender().getId().equals(user.getId())) {
            throw new ApiException(ErrorCode.UNAUTHORIZED);
        }
        if (ZonedDateTime.now().isAfter(message.getSentAt().plusMinutes(Message.MINUTES_TO_DELETE_MESSAGE))) {
            throw new ApiException(ErrorCode.CAN_NOT_DELETE_MESSAGE);
        }
    }

    private void validateEditMessage(Message message, String newContent) {
        User user = userService.getCurrentUserRequiredAuthentication();
        String content = newContent.trim();
        if (!message.getSender().getId().equals(user.getId()))
            throw new ApiException(ErrorCode.UNAUTHORIZED);
        if (MessageType.CALL.equals(message.getType()))
            throw new ApiException(ErrorCode.CAN_NOT_EDIT_CALL);
        if (MessageType.FILE.equals(message.getType()))
            throw new ApiException(ErrorCode.CAN_NOT_EDIT_FILE_MESSAGE);
        if (ZonedDateTime.now().isAfter(message.getSentAt().plusMinutes(Message.MINUTES_TO_EDIT_MESSAGE)))
            throw new ApiException(ErrorCode.CAN_NOT_EDIT_MESSAGE);
        if (content.isEmpty())
            throw new ApiException(ErrorCode.TEXT_MESSAGE_CONTENT_REQUIRED);
        if (content.length() > Message.MAX_CONTENT_LENGTH)
            throw new ApiException(ErrorCode.INVALID_MESSAGE_CONTENT_LENGTH);
        if (content.equals(message.getContent()))
            throw new ApiException(ErrorCode.TEXT_MESSAGE_CONTENT_UNCHANGED);
    }

    /**
     * Dùng cho direct chat: broadcast lên /chat/{chatId} + gửi riêng /message/{targetId}
     */
    private void sendMessageNotification(UUID chatId, UUID targetId, MessageResponse response) {
        messagingTemplate.convertAndSend(WebSocketChannelPrefix.CHAT_CHANNEL_PREFIX + "/" + chatId, response);
        messagingTemplate.convertAndSend(WebSocketChannelPrefix.MESSAGE_CHANNEL_PREFIX + "/" + targetId, response);
    }

    /**
     * Dùng cho group chat:
     * - Broadcast 1 lần duy nhất lên /chat/{chatId} → tránh duplicate N lần theo số thành viên
     * - Loop gửi /message/{userId} per-member (trừ sender) để cập nhật sidebar chat list
     */
    private void sendGroupMessageNotification(UUID chatId, UUID senderId,
                                              Collection<User> members,
                                              MessageResponse response) {
        // ✅ Chỉ 1 lần — tất cả subscriber /chat/{chatId} đều nhận đúng 1 bản
        messagingTemplate.convertAndSend(WebSocketChannelPrefix.CHAT_CHANNEL_PREFIX + "/" + chatId, response);

        // Per-user notification cho sidebar (chỉ các member không phải sender)
        if (members != null) {
            members.forEach(member -> {
                if (!member.getId().equals(senderId)) {
                    messagingTemplate.convertAndSend(
                            WebSocketChannelPrefix.MESSAGE_CHANNEL_PREFIX + "/" + member.getId(), response);
                }
            });
        }
    }
}