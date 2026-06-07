package com.stu.socialnetworkapi.repository.neo4j;

import com.stu.socialnetworkapi.dto.projection.ChatProjection;
import com.stu.socialnetworkapi.dto.projection.GroupMemberProjection;
import com.stu.socialnetworkapi.dto.projection.PinnedMessageProjection;
import com.stu.socialnetworkapi.entity.Chat;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ChatRepository extends Neo4jRepository<Chat, UUID> {

    @Query("""
            MATCH (u:User {id: $userId})-[:IS_MEMBER_OF]->(chat:Chat)<-[:IS_MEMBER_OF]-(target:User {id: $targetId})
            WHERE chat.isGroup IS NULL OR chat.isGroup = false
            RETURN chat.id
            """)
    Optional<UUID> getDirectChatIdByMemberIds(UUID userId, UUID targetId);

    @Query("""
            MATCH (u:User {id: $userId})-[:IS_MEMBER_OF]->(chat:Chat)
            RETURN chat.id
            """)
    List<UUID> getChatIdsByUserId(UUID userId);

    @Query("""
            MATCH (currentUser:User {id: $userId})-[:IS_MEMBER_OF]->(chat:Chat)<-[:IS_MEMBER_OF]-(target:User)
            WHERE target.id <> $userId AND (chat.isGroup IS NULL OR chat.isGroup = false)
            RETURN target.id
            """)
    List<UUID> getTargetIds(UUID userId);

    // ── DIRECT CHATS ──────────────────────────────────────────────────────────

    @Query("""
            MATCH (currentUser:User {id: $userId})-[:IS_MEMBER_OF]->(chat:Chat)
            WHERE chat.isGroup IS NULL OR chat.isGroup = false

            // Find the other member (direct chat only has 2)
            MATCH (chat)<-[:IS_MEMBER_OF]-(target:User)
            WHERE target.id <> $userId

            OPTIONAL MATCH (chat)-[:HAS_MESSAGE]->(message:Message)
            WITH currentUser, chat, target, message
            ORDER BY message.sentAt DESC
            WITH currentUser, chat, target, COLLECT(message)[0] AS latestMessage

            OPTIONAL MATCH (latestMessage)<-[:SENT]-(sender:User)
            OPTIONAL MATCH (latestMessage)-[:ATTACH_FILE]->(latestMessageFile:File)
            OPTIONAL MATCH (sender)-[:HAS_PROFILE_PICTURE]->(senderProfilePic:File)
            OPTIONAL MATCH (target)-[:HAS_PROFILE_PICTURE]->(targetProfilePic:File)

            OPTIONAL MATCH (chat)-[:HAS_MESSAGE]->(unreadMsg:Message)
            WHERE NOT EXISTS((currentUser)-[:SENT]->(unreadMsg)) AND unreadMsg.isRead = false

            OPTIONAL MATCH (currentUser)-[friendRel:FRIEND]->(target)
            OPTIONAL MATCH (currentUser)-[blockOut:BLOCK]->(target)
            OPTIONAL MATCH (currentUser)<-[blockIn:BLOCK]-(target)

            RETURN
                chat.id AS chatId,
                target.givenName + ' ' + target.familyName AS name,
                latestMessage.id AS latestMessageId,
                latestMessage.content AS latestMessageContent,
                latestMessageFile.id AS latestMessageFileId,
                COALESCE(latestMessage.sentAt, chat.createdAt) AS latestMessageSentAt,
                latestMessage.deleteAt IS NOT NULL AS latestMessageDeleted,
                latestMessage.type AS latestMessageType,
                latestMessage.callId AS latestMessageCallId,
                latestMessage.callAt AS latestMessageCallAt,
                latestMessage.endAt AS latestMessageEndAt,
                latestMessage.isAnswered AS latestMessageAnswered,
                latestMessage.isVideoCall AS latestMessageIsVideoCall,
                sender.id AS latestMessageSenderId,
                sender.username AS latestMessageSenderUsername,
                sender.givenName AS latestMessageSenderGivenName,
                sender.familyName AS latestMessageSenderFamilyName,
                senderProfilePic.id AS latestMessageSenderProfilePictureId,
                target.id AS targetId,
                target.username AS targetUsername,
                target.givenName AS targetGivenName,
                target.familyName AS targetFamilyName,
                targetProfilePic.id AS targetProfilePictureId,
                COUNT(unreadMsg) AS notReadMessageCount,
                CASE WHEN friendRel IS NOT NULL THEN true ELSE false END AS isFriend,
                CASE
                    WHEN blockOut IS NOT NULL THEN 'BLOCKED'
                    WHEN blockIn IS NOT NULL THEN 'HAS_BEEN_BLOCKED'
                    ELSE 'NORMAL'
                END AS blockStatus,
                false AS isGroup,
                null AS groupAvatarFileId,
                2 AS memberCount,
                'MEMBER' AS myRole
            ORDER BY latestMessageSentAt DESC
            """)
    List<ChatProjection> getDirectChatList(UUID userId);

    // ── GROUP CHATS ───────────────────────────────────────────────────────────

    @Query("""
            MATCH (currentUser:User {id: $userId})-[myMembership:IS_MEMBER_OF]->(chat:Chat)
            WHERE chat.isGroup = true

            OPTIONAL MATCH (chat)-[:HAS_MESSAGE]->(message:Message)
            WITH currentUser, chat, myMembership, message
            ORDER BY message.sentAt DESC
            WITH currentUser, chat, myMembership, COLLECT(message)[0] AS latestMessage

            OPTIONAL MATCH (latestMessage)<-[:SENT]-(sender:User)
            OPTIONAL MATCH (latestMessage)-[:ATTACH_FILE]->(latestMessageFile:File)
            OPTIONAL MATCH (sender)-[:HAS_PROFILE_PICTURE]->(senderProfilePic:File)

            OPTIONAL MATCH (chat)-[:HAS_MESSAGE]->(unreadMsg:Message)
            WHERE NOT EXISTS((currentUser)-[:SENT]->(unreadMsg)) AND unreadMsg.isRead = false

            MATCH (chat)<-[:IS_MEMBER_OF]-(allMembers:User)

            RETURN
                chat.id AS chatId,
                chat.groupName AS name,
                latestMessage.id AS latestMessageId,
                latestMessage.content AS latestMessageContent,
                latestMessageFile.id AS latestMessageFileId,
                COALESCE(latestMessage.sentAt, chat.createdAt) AS latestMessageSentAt,
                latestMessage.deleteAt IS NOT NULL AS latestMessageDeleted,
                latestMessage.type AS latestMessageType,
                latestMessage.callId AS latestMessageCallId,
                latestMessage.callAt AS latestMessageCallAt,
                latestMessage.endAt AS latestMessageEndAt,
                latestMessage.isAnswered AS latestMessageAnswered,
                latestMessage.isVideoCall AS latestMessageIsVideoCall,
                sender.id AS latestMessageSenderId,
                sender.username AS latestMessageSenderUsername,
                sender.givenName AS latestMessageSenderGivenName,
                sender.familyName AS latestMessageSenderFamilyName,
                senderProfilePic.id AS latestMessageSenderProfilePictureId,
                null AS targetId,
                null AS targetUsername,
                null AS targetGivenName,
                null AS targetFamilyName,
                null AS targetProfilePictureId,
                COUNT(DISTINCT unreadMsg) AS notReadMessageCount,
                false AS isFriend,
                'NORMAL' AS blockStatus,
                true AS isGroup,
                chat.groupAvatarFileId AS groupAvatarFileId,
                COUNT(DISTINCT allMembers) AS memberCount,
                myMembership.role AS myRole
            ORDER BY latestMessageSentAt DESC
            """)
    List<ChatProjection> getGroupChatList(UUID userId);

    // ── SEARCH ────────────────────────────────────────────────────────────────
    @Query("""
        MATCH (currentUser:User {id: $userId})-[myMembership:IS_MEMBER_OF]->(chat:Chat)

        OPTIONAL MATCH (chat)<-[:IS_MEMBER_OF]-(target:User)
        WHERE target.id <> $userId AND (chat.isGroup IS NULL OR chat.isGroup = false)

        OPTIONAL MATCH (chat)-[:HAS_MESSAGE]->(message:Message)
        WITH currentUser, chat, myMembership, target, message
        ORDER BY message.sentAt DESC
        WITH currentUser, chat, myMembership, target, COLLECT(message)[0] AS latestMessage

        OPTIONAL MATCH (chat)-[:HAS_MESSAGE]->(anyMessage:Message)
        WHERE toLower(anyMessage.content) CONTAINS toLower($query)
        WITH currentUser, chat, myMembership, target, latestMessage, COUNT(anyMessage) AS matchedMsgCount

        WHERE
            (chat.isGroup = true AND toLower(chat.groupName) CONTAINS toLower($query))
            OR
            (
                (chat.isGroup IS NULL OR chat.isGroup = false)
                AND (
                    toLower(target.givenName + ' ' + target.familyName) CONTAINS toLower($query)
                    OR toLower(target.username) CONTAINS toLower($query)
                )
            )
            OR matchedMsgCount > 0

        OPTIONAL MATCH (latestMessage)<-[:SENT]-(sender:User)
        OPTIONAL MATCH (latestMessage)-[:ATTACH_FILE]->(latestMessageFile:File)
        OPTIONAL MATCH (sender)-[:HAS_PROFILE_PICTURE]->(senderProfilePic:File)
        OPTIONAL MATCH (target)-[:HAS_PROFILE_PICTURE]->(targetProfilePic:File)
        OPTIONAL MATCH (chat)<-[:IS_MEMBER_OF]-(allMembers:User)
        OPTIONAL MATCH (currentUser)-[friendRel:FRIEND]->(target)
        OPTIONAL MATCH (currentUser)-[blockOut:BLOCK]->(target)
        OPTIONAL MATCH (currentUser)<-[blockIn:BLOCK]-(target)

        RETURN
            chat.id AS chatId,
            CASE WHEN chat.isGroup = true THEN chat.groupName ELSE target.givenName + ' ' + target.familyName END AS name,
            latestMessage.id AS latestMessageId,
            latestMessage.content AS latestMessageContent,
            latestMessageFile.id AS latestMessageFileId,
            COALESCE(latestMessage.sentAt, chat.createdAt) AS latestMessageSentAt,
            latestMessage.deleteAt IS NOT NULL AS latestMessageDeleted,
            latestMessage.type AS latestMessageType,
            latestMessage.callId AS latestMessageCallId,
            latestMessage.callAt AS latestMessageCallAt,
            latestMessage.endAt AS latestMessageEndAt,
            latestMessage.isAnswered AS latestMessageAnswered,
            latestMessage.isVideoCall AS latestMessageIsVideoCall,
            sender.id AS latestMessageSenderId,
            sender.username AS latestMessageSenderUsername,
            sender.givenName AS latestMessageSenderGivenName,
            sender.familyName AS latestMessageSenderFamilyName,
            senderProfilePic.id AS latestMessageSenderProfilePictureId,
            target.id AS targetId,
            target.username AS targetUsername,
            target.givenName AS targetGivenName,
            target.familyName AS targetFamilyName,
            targetProfilePic.id AS targetProfilePictureId,
            0 AS notReadMessageCount,
            CASE WHEN friendRel IS NOT NULL THEN true ELSE false END AS isFriend,
            CASE
                WHEN blockOut IS NOT NULL THEN 'BLOCKED'
                WHEN blockIn IS NOT NULL THEN 'HAS_BEEN_BLOCKED'
                ELSE 'NORMAL'
            END AS blockStatus,
            COALESCE(chat.isGroup, false) AS isGroup,
            chat.groupAvatarFileId AS groupAvatarFileId,
            COUNT(DISTINCT allMembers) AS memberCount,
            myMembership.role AS myRole
        ORDER BY latestMessageSentAt DESC
        """)
    List<ChatProjection> searchChats(UUID userId, String query);

    // ── GROUP MANAGEMENT ──────────────────────────────────────────────────────

    @Query("""
            MATCH (u:User {id: $userId})-[m:IS_MEMBER_OF]->(chat:Chat {id: $chatId})
            RETURN m.role AS role
            """)
    String getMemberRole(UUID userId, UUID chatId);

    @Query("""
            MATCH (u:User {id: $userId})-[m:IS_MEMBER_OF]->(chat:Chat {id: $chatId})
            SET m.role = $role
            """)
    void setMemberRole(UUID userId, UUID chatId, String role);

    @Query("""
            MATCH (u:User {id: $userId})-[m:IS_MEMBER_OF]->(chat:Chat {id: $chatId})
            DELETE m
            """)
    void removeMember(UUID userId, UUID chatId);

    @Query("""
            MATCH (u:User {username: $username}), (chat:Chat {id: $chatId})
            MERGE (u)-[m:IS_MEMBER_OF]->(chat)
            ON CREATE SET m.role = 'MEMBER', m.joinedAt = datetime()
            """)
    void addMember(String username, UUID chatId);

    @Query("""
            MATCH (chat:Chat {id: $chatId})<-[m:IS_MEMBER_OF]-(u:User)
            OPTIONAL MATCH (u)-[:HAS_PROFILE_PICTURE]->(pic:File)
            RETURN u.id AS userId, u.username AS username,
                   u.givenName AS givenName, u.familyName AS familyName,
                   pic.id AS profilePictureId, m.role AS role, m.joinedAt AS joinedAt
            """)
    List<GroupMemberProjection> getGroupMembers(UUID chatId);

    @Query("""
            MATCH (chat:Chat {id: $chatId})
            SET chat.groupName = COALESCE($name, chat.groupName),
                chat.groupAvatarFileId = COALESCE($avatarFileId, chat.groupAvatarFileId)
            """)
    void updateGroupInfo(UUID chatId, String name, String avatarFileId);

    @Query("""
            MATCH (chat:Chat {id: $chatId})
            OPTIONAL MATCH (chat)<-[:IS_MEMBER_OF]-(members:User)
            DETACH DELETE chat
            """)
    void dissolveGroup(UUID chatId);

    @Query("""
            MATCH (chat:Chat {id: $chatId})
            MATCH (msg:Message)-[:HAS_MESSAGE]-(chat)
            WHERE NOT EXISTS((chat)-[:PINNED_MESSAGE]->(msg))
            AND msg.id = $messageId
            MERGE (chat)-[:PINNED_MESSAGE]->(msg)
            """)
    void pinMessage(UUID chatId, UUID messageId);

    @Query("""
            MATCH (chat:Chat {id: $chatId})-[r:PINNED_MESSAGE]->(msg:Message {id: $messageId})
            DELETE r
            """)
    void unpinMessage(UUID chatId, UUID messageId);

    @Query("""
            MATCH (chat:Chat {id: $chatId})-[:PINNED_MESSAGE]->(msg:Message)
            OPTIONAL MATCH (msg)<-[:SENT]-(sender:User)
            RETURN msg.id AS id, msg.content AS content, msg.sentAt AS sentAt,
                   sender.username AS senderUsername
            """)
    List<PinnedMessageProjection> getPinnedMessages(UUID chatId);
}