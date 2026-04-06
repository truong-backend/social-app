package com.socialapp.application.user.usecase;

import com.socialapp.application.shared.exception.ResourceNotFoundException;
import com.socialapp.application.shared.port.FileStorage;
import com.socialapp.application.user.dto.response.UserResponseDtos.UserProfileResponse;
import com.socialapp.domain.relationship.repository.BlockRepository;
import com.socialapp.domain.relationship.repository.FriendRepository;
import com.socialapp.domain.relationship.repository.FriendRequestRepository;
import com.socialapp.domain.user.entity.User;
import com.socialapp.domain.user.repository.UserRepository;
import com.socialapp.domain.user.service.UserDomainService;
import org.springframework.transaction.annotation.Transactional;

public class GetProfileUseCase {

    private final UserRepository          userRepository;
    private final BlockRepository         blockRepository;
    private final FriendRepository        friendRepository;
    private final FriendRequestRepository friendRequestRepository;
    private final UserDomainService       userDomainService;
    private final FileStorage             fileStorage;

    public GetProfileUseCase(UserRepository userRepository,
                             BlockRepository blockRepository,
                             FriendRepository friendRepository,
                             FriendRequestRepository friendRequestRepository,
                             UserDomainService userDomainService,
                             FileStorage fileStorage) {
        this.userRepository           = userRepository;
        this.blockRepository          = blockRepository;
        this.friendRepository         = friendRepository;
        this.friendRequestRepository  = friendRequestRepository;
        this.userDomainService        = userDomainService;
        this.fileStorage              = fileStorage;
    }

    @Transactional(readOnly = true)
    public UserProfileResponse execute(String requesterId, String targetId) {

        User target = userRepository.findById(targetId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Validate: target có chặn requester không?
        boolean isBlockedByTarget = blockRepository.exists(targetId, requesterId);
        userDomainService.validateCanViewProfile(requesterId, targetId, isBlockedByTarget);

        boolean isFriend           = friendRepository.existsFriendship(requesterId, targetId);
        boolean isBlockedByMe      = blockRepository.exists(requesterId, targetId);
        boolean hasSentRequest     = friendRequestRepository.exists(requesterId, targetId);
        boolean hasReceivedRequest = friendRequestRepository.exists(targetId, requesterId);

        // ✅ FIX: Chuyển object path → public URL để frontend render được ảnh
        String rawPath     = target.getProfilePicturePath();
        String pictureUrl  = (rawPath != null && !rawPath.isBlank())
                ? fileStorage.getPublicUrl(rawPath)
                : null;

        return new UserProfileResponse(
                target.getId(),
                target.getUsername().getValue(),
                target.getFullName().getFamilyName(),
                target.getFullName().getGivenName(),
                target.getBio(),
                pictureUrl,                              // ← đã là URL đầy đủ
                target.getBirthdate(),
                target.getCounts().getFriendCount(),
                isFriend,
                isBlockedByMe,
                hasSentRequest,
                hasReceivedRequest
        );
    }
}
