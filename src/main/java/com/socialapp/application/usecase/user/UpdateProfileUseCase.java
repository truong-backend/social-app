package com.socialapp.application.usecase.user;

import com.socialapp.application.dto.request.UpdateProfileRequest;
import com.socialapp.application.dto.response.UserResponse;
import com.socialapp.application.mapper.UserMapper;
import com.socialapp.domain.model.aggregate.User;
import com.socialapp.domain.model.valueobject.Birthdate;
import com.socialapp.domain.model.valueobject.UserId;
import com.socialapp.domain.repository.UserRepository;
import com.socialapp.domain.service.UserProfileDomainService;

public class UpdateProfileUseCase {

    private final UserProfileDomainService userProfileDomainService;
    private final UserRepository           userRepository;
    private final UserMapper               userMapper;

    public UpdateProfileUseCase(UserProfileDomainService userProfileDomainService,
                                UserRepository userRepository,
                                UserMapper userMapper) {
        this.userProfileDomainService = userProfileDomainService;
        this.userRepository           = userRepository;
        this.userMapper               = userMapper;
    }

    public UserResponse execute(String userId, UpdateProfileRequest req) {
        UserId uid = new UserId(userId);

        // Mỗi field chỉ update khi client gửi lên (không null)
        if (req.familyName() != null && req.givenName() != null)
            userProfileDomainService.changeName(uid, req.familyName(), req.givenName());

        if (req.birthdate() != null)
            userProfileDomainService.changeBirthdate(uid, new Birthdate(req.birthdate()));

        if (req.username() != null)
            userProfileDomainService.changeUsername(uid, req.username());

        if (req.bio() != null)
            userProfileDomainService.updateBio(uid, req.bio());

        User updated = userRepository.findById(uid)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
        return userMapper.toResponse(updated);
    }
}