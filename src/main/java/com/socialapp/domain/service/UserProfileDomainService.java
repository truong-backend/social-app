package com.socialapp.domain.service;

import com.socialapp.application.dto.response.ErrorCode;
import com.socialapp.domain.model.aggregate.User;
import com.socialapp.domain.model.entity.FileEntity;
import com.socialapp.domain.model.valueobject.Birthdate;
import com.socialapp.domain.model.valueobject.FileMeta;
import com.socialapp.domain.model.valueobject.UserId;
import com.socialapp.domain.model.valueobject.Username;
import com.socialapp.domain.repository.UserRepository;
import com.socialapp.presentation.advice.DomainException;

/**
 * Domain Service: UserProfileDomainService
 * ─────────────────────────────────────────────────────────────
 * Xử lý cập nhật thông tin cá nhân người dùng.
 *
 * Trách nhiệm:
 *   - Đổi tên (60-day cooldown)
 *   - Đổi ngày sinh (60-day cooldown)
 *   - Đổi username (unique check + 60-day cooldown)
 *   - Cập nhật bio
 *   - Đặt ảnh đại diện
 */
public class UserProfileDomainService {

    private final UserRepository userRepository;

    public UserProfileDomainService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public void changeName(UserId userId, String familyName, String givenName) {
        User user = requireUser(userId);
        user.changeName(familyName, givenName);
        userRepository.save(user);
    }

    public void changeBirthdate(UserId userId, Birthdate birthdate) {
        User user = requireUser(userId);
        user.changeBirthdate(birthdate);
        userRepository.save(user);
    }

    public void changeUsername(UserId userId, String newUsernameStr) {
        Username newUsername = new Username(newUsernameStr);

        if (userRepository.existsByUsername(newUsername))
            throw new DomainException(ErrorCode.USERNAME_TAKEN,
                    "Username '" + newUsernameStr + "' is already taken");

        User user = requireUser(userId);
        user.changeUsername(newUsername);
        userRepository.save(user);
    }

    public void updateBio(UserId userId, String bio) {
        User user = requireUser(userId);
        user.updateBio(bio);
        userRepository.save(user);
    }

    public void setProfilePicture(UserId userId, FileMeta fileMeta) {
        User user = requireUser(userId);
        user.setProfilePicture(new FileEntity(fileMeta));
        userRepository.save(user);
    }

    // ── Helper ───────────────────────────────────────────────

    private User requireUser(UserId userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new DomainException(
                        ErrorCode.USER_NOT_FOUND, "User not found: " + userId));
    }
}