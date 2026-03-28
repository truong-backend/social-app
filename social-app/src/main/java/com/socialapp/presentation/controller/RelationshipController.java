package com.socialapp.presentation.controller;

import com.socialapp.application.relationship.usecase.*;
import com.socialapp.domain.account.repository.AccountRepository;
import com.socialapp.domain.relationship.entity.FriendRelationship;
import com.socialapp.domain.relationship.entity.FriendRequest;
import com.socialapp.domain.relationship.entity.BlockRelationship;
import com.socialapp.domain.relationship.repository.*;
import com.socialapp.presentation.util.ApiResponse;
import com.socialapp.presentation.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/relationships")
@RequiredArgsConstructor
public class RelationshipController {

    private final SendFriendRequestUseCase    sendRequestUseCase;
    private final AcceptFriendRequestUseCase  acceptRequestUseCase;
    private final DeleteFriendRequestUseCase  deleteRequestUseCase;
    private final UnfriendUseCase             unfriendUseCase;
    private final BlockUserUseCase            blockUserUseCase;
    private final UnblockUserUseCase          unblockUserUseCase;
    private final FriendRepository            friendRepository;
    private final FriendRequestRepository     friendRequestRepository;
    private final BlockRepository             blockRepository;
    private final AccountRepository           accountRepository;

    private String resolveUserId() {
        return accountRepository.findById(SecurityUtil.currentAccountId())
                .orElseThrow().getUserId();
    }

    /** POST /api/relationships/friends/{targetId} — gửi lời mời */
    @PostMapping("/friends/{targetId}")
    public ApiResponse<Void> sendRequest(@PathVariable String targetId) {
        var res = sendRequestUseCase.execute(resolveUserId(), targetId);
        return ApiResponse.ok(res.message());
    }

    /** PUT /api/relationships/friends/{senderId}/accept — chấp nhận */
    @PutMapping("/friends/{senderId}/accept")
    public ApiResponse<Void> acceptRequest(@PathVariable String senderId) {
        var res = acceptRequestUseCase.execute(resolveUserId(), senderId);
        return ApiResponse.ok(res.message());
    }

    /** DELETE /api/relationships/friends/{targetId}/request — hủy/từ chối */
    @DeleteMapping("/friends/{targetId}/request")
    public ApiResponse<Void> deleteRequest(@PathVariable String targetId) {
        var res = deleteRequestUseCase.execute(resolveUserId(), targetId);
        return ApiResponse.ok(res.message());
    }

    /** DELETE /api/relationships/friends/{targetId} — hủy kết bạn */
    @DeleteMapping("/friends/{targetId}")
    public ApiResponse<Void> unfriend(@PathVariable String targetId) {
        var res = unfriendUseCase.execute(resolveUserId(), targetId);
        return ApiResponse.ok(res.message());
    }

    /** GET /api/relationships/friends — danh sách bạn bè */
    @GetMapping("/friends")
    public ApiResponse<List<String>> getFriends() {
        String userId = resolveUserId();
        List<String> friendIds = friendRepository.findFriendsByUserId(userId)
                .stream()
                .map(r -> r.getOtherUserId(userId))
                .toList();
        return ApiResponse.ok(friendIds);
    }

    /** GET /api/relationships/requests/sent — lời mời đã gửi */
    @GetMapping("/requests/sent")
    public ApiResponse<List<String>> getSentRequests() {
        String userId = resolveUserId();
        List<String> receiverIds = friendRequestRepository.findSentByUserId(userId)
                .stream().map(FriendRequest::getReceiverId).toList();
        return ApiResponse.ok(receiverIds);
    }

    /** GET /api/relationships/requests/received — lời mời đã nhận */
    @GetMapping("/requests/received")
    public ApiResponse<List<String>> getReceivedRequests() {
        String userId = resolveUserId();
        List<String> senderIds = friendRequestRepository.findReceivedByUserId(userId)
                .stream().map(FriendRequest::getSenderId).toList();
        return ApiResponse.ok(senderIds);
    }

    /** POST /api/relationships/blocks/{targetId} — chặn */
    @PostMapping("/blocks/{targetId}")
    public ApiResponse<Void> block(@PathVariable String targetId) {
        var res = blockUserUseCase.execute(resolveUserId(), targetId);
        return ApiResponse.ok(res.message());
    }

    /** DELETE /api/relationships/blocks/{targetId} — bỏ chặn */
    @DeleteMapping("/blocks/{targetId}")
    public ApiResponse<Void> unblock(@PathVariable String targetId) {
        var res = unblockUserUseCase.execute(resolveUserId(), targetId);
        return ApiResponse.ok(res.message());
    }

    /** GET /api/relationships/blocks — danh sách đã chặn */
    @GetMapping("/blocks")
    public ApiResponse<List<String>> getBlocked() {
        String userId = resolveUserId();
        List<String> blockedIds = blockRepository.findBlockedByUserId(userId)
                .stream().map(BlockRelationship::getBlockedId).toList();
        return ApiResponse.ok(blockedIds);
    }
}
