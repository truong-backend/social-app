package com.stu.socialnetworkapi.controller;

import com.stu.socialnetworkapi.dto.projection.PinnedMessageProjection;
import com.stu.socialnetworkapi.dto.request.CreateGroupRequest;
import com.stu.socialnetworkapi.dto.request.GroupMemberRequest;
import com.stu.socialnetworkapi.dto.request.UpdateGroupRequest;
import com.stu.socialnetworkapi.dto.response.ApiResponse;
import com.stu.socialnetworkapi.dto.response.ChatResponse;
import com.stu.socialnetworkapi.dto.response.GroupMemberResponse;
import com.stu.socialnetworkapi.service.itf.GroupService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/v1/group")
@RequiredArgsConstructor
public class GroupController {

    private final GroupService groupService;

    @PostMapping
    public ApiResponse<ChatResponse> createGroup(@Valid @RequestBody CreateGroupRequest request) {
        return ApiResponse.success(groupService.createGroup(request));
    }

    @PutMapping
    public ApiResponse<ChatResponse> updateGroup(@Valid UpdateGroupRequest request) {
        return ApiResponse.success(groupService.updateGroup(request));
    }

    @PostMapping("/members/add")
    public ApiResponse<Void> addMembers(@Valid @RequestBody GroupMemberRequest request) {
        groupService.addMembers(request);
        return ApiResponse.success();
    }

    @DeleteMapping("/members/remove")
    public ApiResponse<Void> removeMember(@Valid @RequestBody GroupMemberRequest request) {
        groupService.removeMember(request);
        return ApiResponse.success();
    }

    @PostMapping("/{chatId}/leave")
    public ApiResponse<Void> leaveGroup(@PathVariable UUID chatId) {
        groupService.leaveGroup(chatId);
        return ApiResponse.success();
    }

    @DeleteMapping("/{chatId}/dissolve")
    public ApiResponse<Void> dissolveGroup(@PathVariable UUID chatId) {
        groupService.dissolveGroup(chatId);
        return ApiResponse.success();
    }

    @PostMapping("/members/promote")
    public ApiResponse<Void> promoteToAdmin(@Valid @RequestBody GroupMemberRequest request) {
        groupService.promoteToAdmin(request);
        return ApiResponse.success();
    }

    @PostMapping("/members/demote")
    public ApiResponse<Void> demoteToMember(@Valid @RequestBody GroupMemberRequest request) {
        groupService.demoteToMember(request);
        return ApiResponse.success();
    }

    @PostMapping("/members/transfer-ownership")
    public ApiResponse<Void> transferOwnership(@Valid @RequestBody GroupMemberRequest request) {
        groupService.transferOwnership(request);
        return ApiResponse.success();
    }

    @GetMapping("/{chatId}/members")
    public ApiResponse<List<GroupMemberResponse>> getMembers(@PathVariable UUID chatId) {
        return ApiResponse.success(groupService.getMembers(chatId));
    }

    @PostMapping("/{chatId}/pin/{messageId}")
    public ApiResponse<Void> pinMessage(@PathVariable UUID chatId, @PathVariable UUID messageId) {
        groupService.pinMessage(chatId, messageId);
        return ApiResponse.success();
    }

    @DeleteMapping("/{chatId}/pin/{messageId}")
    public ApiResponse<Void> unpinMessage(@PathVariable UUID chatId, @PathVariable UUID messageId) {
        groupService.unpinMessage(chatId, messageId);
        return ApiResponse.success();
    }

    @GetMapping("/{chatId}/pins")
    public ApiResponse<List<PinnedMessageProjection>> getPinnedMessages(@PathVariable UUID chatId) {
        return ApiResponse.success(groupService.getPinnedMessages(chatId));
    }
}