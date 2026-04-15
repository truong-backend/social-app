package com.socialapp.application.usecase.user;

import com.socialapp.application.dto.response.UserResponse;
import com.socialapp.application.mapper.UserMapper;
import com.socialapp.domain.model.aggregate.User;
import com.socialapp.domain.model.valueobject.UserId;
import com.socialapp.domain.repository.UserRepository;

public class GetUserProfileUseCase {

    private final UserRepository userRepository;
    private final UserMapper     userMapper;

    public GetUserProfileUseCase(UserRepository userRepository,
                                 UserMapper userMapper) {
        this.userRepository = userRepository;
        this.userMapper     = userMapper;
    }

    public UserResponse execute(String userId) {
        User user = userRepository.findById(new UserId(userId))
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
        return userMapper.toResponse(user);
    }
}