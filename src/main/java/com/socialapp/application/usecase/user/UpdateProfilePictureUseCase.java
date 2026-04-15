package com.socialapp.application.usecase.user;

import com.socialapp.application.dto.response.UserResponse;
import com.socialapp.application.mapper.UserMapper;
import com.socialapp.application.port.FileStoragePort;
import com.socialapp.domain.model.aggregate.User;
import com.socialapp.domain.model.valueobject.FileMeta;
import com.socialapp.domain.model.valueobject.UserId;
import com.socialapp.domain.repository.UserRepository;
import com.socialapp.domain.service.UserProfileDomainService;
import org.springframework.web.multipart.MultipartFile;


public class UpdateProfilePictureUseCase {

    private final UserProfileDomainService userProfileDomainService;
    private final UserRepository           userRepository;
    private final FileStoragePort          fileStoragePort;
    private final UserMapper               userMapper;

    public UpdateProfilePictureUseCase(UserProfileDomainService userProfileDomainService,
                                       UserRepository userRepository,
                                       FileStoragePort fileStoragePort,
                                       UserMapper userMapper) {
        this.userProfileDomainService = userProfileDomainService;
        this.userRepository           = userRepository;
        this.fileStoragePort          = fileStoragePort;
        this.userMapper               = userMapper;
    }

    public UserResponse execute(String userId, MultipartFile file) {
        FileMeta fileMeta = fileStoragePort.store(file);
        userProfileDomainService.setProfilePicture(new UserId(userId), fileMeta);

        User updated = userRepository.findById(new UserId(userId)).orElseThrow();
        return userMapper.toResponse(updated);
    }
}