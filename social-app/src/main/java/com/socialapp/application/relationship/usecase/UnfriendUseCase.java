package com.socialapp.application.relationship.usecase;

import com.socialapp.application.relationship.dto.response.MessageResponse;
import com.socialapp.domain.relationship.repository.FriendRepository;
import com.socialapp.domain.relationship.service.RelationshipDomainService;
import com.socialapp.domain.user.entity.User;
import com.socialapp.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UnfriendUseCase {

    private final UserRepository userRepository;
    private final FriendRepository friendRepository;
    private final RelationshipDomainService domainService;

    @Transactional
    public MessageResponse execute(String requesterId, String targetId) {

        domainService.validateUnfriend(
                friendRepository.existsFriendship(requesterId, targetId));

        friendRepository.delete(requesterId, targetId);

        User requester = userRepository.findById(requesterId).orElseThrow();
        User target = userRepository.findById(targetId).orElseThrow();
        requester.onFriendRemoved();
        target.onFriendRemoved();
        userRepository.save(requester);
        userRepository.save(target);

        return new MessageResponse("Unfriended successfully");
    }
}
