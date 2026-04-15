package com.socialapp.domain.repository;

import com.socialapp.domain.model.aggregate.User;
import com.socialapp.domain.model.valueobject.UserId;
import com.socialapp.domain.model.valueobject.Username;

import java.util.List;
import java.util.Optional;

/**
 * Repository: User
 */
public interface UserRepository {

    Optional<User> findById(UserId id);

    Optional<User> findByUsername(Username username);

    /** Tìm kiếm người dùng theo tên hoặc username (search feature) */
    List<User> searchByKeyword(String keyword, int limit, int offset);

    void save(User user);

    void delete(UserId id);

    boolean existsByUsername(Username username);

    long countAll();

    // Thêm vào cuối interface UserRepository (trước dấu })

    List<User> listFriends(UserId userId, int limit, int offset);

    List<User> listSentRequests(UserId userId, int limit, int offset);

    List<User> listReceivedRequests(UserId userId, int limit, int offset);

    boolean hasSentRequest(UserId userId, UserId targetId);

    boolean hasReceivedRequest(UserId userId, UserId senderId);

    boolean areFriends(UserId userAId, UserId userBId);

    boolean isBlocked(UserId blockerId, UserId targetId);

    // Thêm vào cuối interface UserRepository

    /** Tạo relationship (sender)-[:REQUEST]->(receiver) trong graph */
    void createRequestRelationship(UserId senderId, UserId receiverId);

    /** Xóa relationship [:REQUEST] giữa hai node */
    void deleteRequestRelationship(UserId senderId, UserId receiverId);

    /** Tạo relationship (userA)-[:FRIEND]-(userB) — undirected, dùng MERGE 2 chiều */
    void createFriendRelationship(UserId userAId, UserId userBId);

    /** Xóa relationship [:FRIEND] giữa hai node */
    void deleteFriendRelationship(UserId userAId, UserId userBId);

    /** Tạo relationship (blocker)-[:BLOCK]->(target) */
    void createBlockRelationship(UserId blockerId, UserId targetId);

    /** Xóa relationship [:BLOCK] */
    void deleteBlockRelationship(UserId blockerId, UserId targetId);
}