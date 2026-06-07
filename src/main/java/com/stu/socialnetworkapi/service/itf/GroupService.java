package com.stu.socialnetworkapi.service.itf;

import com.stu.socialnetworkapi.dto.projection.PinnedMessageProjection;
import com.stu.socialnetworkapi.dto.request.CreateGroupRequest;
import com.stu.socialnetworkapi.dto.request.GroupMemberRequest;
import com.stu.socialnetworkapi.dto.request.UpdateGroupRequest;
import com.stu.socialnetworkapi.dto.response.ChatResponse;
import com.stu.socialnetworkapi.dto.response.GroupMemberResponse;

import java.util.List;
import java.util.UUID;

public interface GroupService {
    ChatResponse createGroup(CreateGroupRequest request);
    ChatResponse updateGroup(UpdateGroupRequest request);
    void addMembers(GroupMemberRequest request);
    void removeMember(GroupMemberRequest request);
    void leaveGroup(UUID chatId);
    void dissolveGroup(UUID chatId);
    void promoteToAdmin(GroupMemberRequest request);
    void demoteToMember(GroupMemberRequest request);
    void transferOwnership(GroupMemberRequest request);
    List<GroupMemberResponse> getMembers(UUID chatId);
    void pinMessage(UUID chatId, UUID messageId);
    void unpinMessage(UUID chatId, UUID messageId);
    List<PinnedMessageProjection> getPinnedMessages(UUID chatId);
}