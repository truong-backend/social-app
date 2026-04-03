package com.socialapp.domain.notification.service;

import com.socialapp.domain.notification.entity.Notification;
import com.socialapp.domain.notification.valueobject.NotificationAction;
import com.socialapp.domain.notification.valueobject.NotificationTarget;
import com.socialapp.domain.notification.valueobject.NotificationTargetType;

/**
 * Domain Service: NotificationDomainService
 *
 * Factory logic tạo từng loại Notification cụ thể.
 * Tách ra service để Application layer không cần biết
 * chi tiết cách tạo từng loại.
 */
public class NotificationDomainService {

    public Notification createFriendRequestNotification(
            String receiverId, String senderId, String requestRelId) {
        return Notification.create(
                receiverId, senderId,
                NotificationAction.SENT_ADD_FRIEND_REQUEST,
                NotificationTarget.of(NotificationTargetType.REQUEST, requestRelId)
        );
    }

    public Notification createBeFriendNotification(
            String receiverId, String senderId, String friendRelId) {
        return Notification.create(
                receiverId, senderId,
                NotificationAction.BE_FRIEND,
                NotificationTarget.of(NotificationTargetType.FRIEND, friendRelId)
        );
    }

    public Notification createLikedPostNotification(
            String postOwnerId, String likerId, String postId) {
        return Notification.create(
                postOwnerId, likerId,
                NotificationAction.LIKED_POST,
                NotificationTarget.of(NotificationTargetType.POST, postId)
        );
    }

    public Notification createCommentedPostNotification(
            String postOwnerId, String commenterId, String postId) {
        return Notification.create(
                postOwnerId, commenterId,
                NotificationAction.COMMENTED_POST,
                NotificationTarget.of(NotificationTargetType.POST, postId)
        );
    }

    public Notification createLikedCommentNotification(
            String commentOwnerId, String likerId, String commentId) {
        return Notification.create(
                commentOwnerId, likerId,
                NotificationAction.LIKED_COMMENT,
                NotificationTarget.of(NotificationTargetType.COMMENT, commentId)
        );
    }

    public Notification createRepliedCommentNotification(
            String commentOwnerId, String replierId, String commentId) {
        return Notification.create(
                commentOwnerId, replierId,
                NotificationAction.REPLIED_COMMENT,
                NotificationTarget.of(NotificationTargetType.COMMENT, commentId)
        );
    }
}