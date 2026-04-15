package com.socialapp.application.mapper;

import com.socialapp.application.dto.response.UserResponse;
import com.socialapp.domain.model.aggregate.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public UserResponse toResponse(User user) {
        String avatarUrl = (user.getProfilePicture() != null)
                ? user.getProfilePicture().getMeta().getPath()
                : null;

        return new UserResponse(
                user.getId().getValue(),
                user.getUsername().getValue(),
                user.getFamilyName(),
                user.getGivenName(),
                user.getBio(),
                user.getBirthdate().getValue(),
                user.getFriendCount(),
                user.getRequestSentCount(),
                user.getRequestReceivedCount(),
                user.getBlockCount(),
                avatarUrl,
                user.getNextChangeNameDate(),
                user.getNextChangeBirthdateDate(),
                user.getNextChangeUsernameDate()
        );
    }
}