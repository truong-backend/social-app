package com.socialapp.application.call.usecase;

import com.socialapp.application.call.dto.CallDtos;
import com.socialapp.application.shared.exception.ForbiddenException;
import com.socialapp.application.shared.exception.ResourceNotFoundException;
import com.socialapp.application.shared.port.RealtimePublisher;
import com.socialapp.domain.message.entity.Call;
import com.socialapp.domain.message.entity.Chat;
import com.socialapp.domain.message.repository.CallRepository;
import com.socialapp.domain.message.repository.ChatRepository;
import com.socialapp.infrastructure.call.InCallStore;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

public class InitiateCallUseCase {

    private final ChatRepository    chatRepository;
    private final CallRepository    callRepository;
    private final RealtimePublisher realtimePublisher;
    private final InCallStore       inCallStore;

    public InitiateCallUseCase(ChatRepository chatRepository,
                               CallRepository callRepository,
                               RealtimePublisher realtimePublisher,
                               InCallStore inCallStore) {
        this.chatRepository    = chatRepository;
        this.callRepository    = callRepository;
        this.realtimePublisher = realtimePublisher;
        this.inCallStore       = inCallStore;
    }

    @Transactional
    public CallDtos.CallResponse execute(String callerId, String targetUserId,
                                         boolean isVideoCall) {
        // Lấy hoặc tạo chat giữa 2 người
        Chat chat = chatRepository.findDirectChat(callerId, targetUserId)
                .orElseGet(() -> {
                    Chat newChat = Chat.createDirect(callerId, targetUserId);
                    return chatRepository.save(newChat);
                });

        if (!chat.hasMember(callerId)) {
            throw new ForbiddenException("Not a member of this chat");
        }

        String roomId = UUID.randomUUID().toString();
        Call call = Call.initiate(callerId, chat.getId(), roomId, isVideoCall);
        callRepository.save(call);
        inCallStore.put(chat.getId(), call.getCallId());

        // Push realtime tới người nhận
        String targetId = chat.getOtherMember(callerId);
        realtimePublisher.publish(
                "/topic/call/" + targetId,
                new CallDtos.CallResponse(
                        call.getCallId(), roomId, chat.getId(),
                        callerId, isVideoCall, call.getCallAt()
                )
        );

        return new CallDtos.CallResponse(
                call.getCallId(), roomId, chat.getId(),
                callerId, isVideoCall, call.getCallAt()
        );
    }
}