package com.socialapp.domain.relationship.repository;

import com.socialapp.domain.relationship.entity.BlockRelationship;
import com.socialapp.domain.relationship.entity.FriendRelationship;
import com.socialapp.domain.relationship.entity.FriendRequest;

import java.util.List;
import java.util.Optional;

public interface FriendRepository {

    boolean existsFriendship(String userIdA, String userIdB);

    List<FriendRelationship> findFriendsByUserId(String userId);

    FriendRelationship save(FriendRelationship relationship);

    void delete(String userIdA, String userIdB);
}