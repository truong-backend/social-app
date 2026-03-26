package com.socialapp.domain.relationship.repository;

import com.socialapp.domain.relationship.entity.FriendRequest;

import java.util.List;
import java.util.Optional;

public interface FriendRequestRepository {

    boolean exists(String senderId, String receiverId);

    Optional<FriendRequest> find(String senderId, String receiverId);

    List<FriendRequest> findSentByUserId(String senderId);

    List<FriendRequest> findReceivedByUserId(String receiverId);

    FriendRequest save(FriendRequest request);

    void delete(String senderId, String receiverId);
}