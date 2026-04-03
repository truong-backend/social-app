package com.socialapp.domain.user.service;

import com.socialapp.domain.user.entity.User;
import com.socialapp.domain.user.valueobject.FullName;
import com.socialapp.domain.user.valueobject.Username;
import com.socialapp.domain.user.exception.UserDomainException;

/**
 * Domain Service: UserDomainService
 *
 * Chứa các business rules và logic domain của User.
 * Không phụ thuộc vào Spring, Repository hay Infrastructure.
 */
public class UserDomainService {

    /**
     * Áp dụng thay đổi tên (họ + tên)
     */
    public void applyChangeName(User user, FullName newFullName) {
        if (newFullName == null) {
            throw new IllegalArgumentException("Full name cannot be null");
        }

        user.changeName(newFullName);
    }

    /**
     * Áp dụng thay đổi username
     */
    public void applyChangeUsername(User user, Username newUsername, boolean alreadyExists) {
        if (newUsername == null) {
            throw new IllegalArgumentException("Username cannot be null");
        }

        if (alreadyExists) {
            throw new UserDomainException("Username already exists");
        }

        if (newUsername.getValue().length() < 3) {
            throw new UserDomainException("Username must be at least 3 characters long");
        }

        user.changeUsername(newUsername);
    }

    /**
     * Kiểm tra quyền xem profile của người khác
     */
    public void validateCanViewProfile(String requesterId, String targetId, boolean isBlockedByTarget) {
        if (requesterId.equals(targetId)) {
            return; // Cho phép xem profile của chính mình
        }

        if (isBlockedByTarget) {
            throw new UserDomainException("You have been blocked by this user and cannot view their profile");
        }
    }
}