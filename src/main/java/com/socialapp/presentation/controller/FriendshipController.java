package com.socialapp.presentation.controller;

import com.socialapp.application.dto.response.ApiResponse;
import com.socialapp.application.usecase.friendship.AcceptFriendRequestUseCase;
import com.socialapp.application.usecase.friendship.BlockUserUseCase;
import com.socialapp.application.usecase.friendship.CancelFriendRequestUseCase;
import com.socialapp.application.usecase.friendship.RejectFriendRequestUseCase;
import com.socialapp.application.usecase.friendship.SendFriendRequestUseCase;
import com.socialapp.application.usecase.friendship.UnblockUserUseCase;
import com.socialapp.application.usecase.friendship.UnfriendUseCase;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import com.socialapp.application.dto.request.PageRequest;
import com.socialapp.application.dto.response.PageResponse;
import com.socialapp.application.dto.response.UserResponse;
import com.socialapp.application.usecase.friendship.GetFriendStatusUseCase;
import com.socialapp.application.usecase.friendship.ListFriendsUseCase;
import com.socialapp.application.usecase.friendship.ListReceivedRequestsUseCase;
import com.socialapp.application.usecase.friendship.ListSentRequestsUseCase;
import java.util.Map;

/**
 * REST Controller — Friendship & Block
 *
 * POST   /api/friendships/request/{receiverId}          — Gửi lời mời kết bạn
 * DELETE /api/friendships/request/{receiverId}          — Hủy lời mời đã gửi
 * POST   /api/friendships/request/{requesterId}/accept  — Chấp nhận lời mời kết bạn
 * POST   /api/friendships/request/{requesterId}/reject  — Từ chối lời mời kết bạn
 * DELETE /api/friendships/{friendId}                    — Hủy kết bạn
 * POST   /api/friendships/block/{targetId}              — Chặn người dùng
 * DELETE /api/friendships/block/{targetId}              — Bỏ chặn người dùng
 *
 * Domain rules (enforced in FriendshipDomainService):
 *   - Tối đa 100 bạn bè, 100 block, 100 lời mời gửi/nhận
 *   - Không thể gửi lời mời cho người đã chặn / đã là bạn bè
 */
@RestController
@RequestMapping("/api/friendships")
public class FriendshipController {

    private final SendFriendRequestUseCase   sendFriendRequestUseCase;
    private final CancelFriendRequestUseCase cancelFriendRequestUseCase;
    private final AcceptFriendRequestUseCase acceptFriendRequestUseCase;
    private final RejectFriendRequestUseCase rejectFriendRequestUseCase;
    private final UnfriendUseCase            unfriendUseCase;
    private final BlockUserUseCase           blockUserUseCase;
    private final UnblockUserUseCase         unblockUserUseCase;
    private final ListFriendsUseCase         listFriendsUseCase;
    private final ListSentRequestsUseCase    listSentRequestsUseCase;
    private final ListReceivedRequestsUseCase listReceivedRequestsUseCase;
    private final GetFriendStatusUseCase     getFriendStatusUseCase;

    public FriendshipController(
            SendFriendRequestUseCase sendFriendRequestUseCase,
            CancelFriendRequestUseCase cancelFriendRequestUseCase,
            AcceptFriendRequestUseCase acceptFriendRequestUseCase,
            RejectFriendRequestUseCase rejectFriendRequestUseCase,
            UnfriendUseCase unfriendUseCase,
            BlockUserUseCase blockUserUseCase,
            UnblockUserUseCase unblockUserUseCase,
            ListFriendsUseCase listFriendsUseCase,
            ListSentRequestsUseCase listSentRequestsUseCase,
            ListReceivedRequestsUseCase listReceivedRequestsUseCase,
            GetFriendStatusUseCase getFriendStatusUseCase) {

        this.sendFriendRequestUseCase   = sendFriendRequestUseCase;
        this.cancelFriendRequestUseCase = cancelFriendRequestUseCase;
        this.acceptFriendRequestUseCase = acceptFriendRequestUseCase;
        this.rejectFriendRequestUseCase = rejectFriendRequestUseCase;
        this.unfriendUseCase            = unfriendUseCase;
        this.blockUserUseCase           = blockUserUseCase;
        this.unblockUserUseCase         = unblockUserUseCase;
        this.listFriendsUseCase         = listFriendsUseCase;
        this.listSentRequestsUseCase    = listSentRequestsUseCase;
        this.listReceivedRequestsUseCase = listReceivedRequestsUseCase;
        this.getFriendStatusUseCase     = getFriendStatusUseCase;
    }

    // ── POST /api/friendships/request/{receiverId} ───────────
    @PostMapping("/request/{receiverId}")
    public ResponseEntity<ApiResponse<Void>> sendFriendRequest(
            @AuthenticationPrincipal String senderId,
            @PathVariable String receiverId) {

        sendFriendRequestUseCase.execute(senderId, receiverId);
        return ResponseEntity.ok(ApiResponse.ok());
    }

    // ── DELETE /api/friendships/request/{receiverId} ─────────
    @DeleteMapping("/request/{receiverId}")
    public ResponseEntity<ApiResponse<Void>> cancelFriendRequest(
            @AuthenticationPrincipal String senderId,
            @PathVariable String receiverId) {

        cancelFriendRequestUseCase.execute(senderId, receiverId);
        return ResponseEntity.ok(ApiResponse.ok());
    }

    // ── POST /api/friendships/request/{requesterId}/accept ───
    @PostMapping("/request/{requesterId}/accept")
    public ResponseEntity<ApiResponse<Void>> acceptFriendRequest(
            @AuthenticationPrincipal String acceptorId,
            @PathVariable String requesterId) {

        acceptFriendRequestUseCase.execute(requesterId, acceptorId);
        return ResponseEntity.ok(ApiResponse.ok());
    }

    // ── POST /api/friendships/request/{requesterId}/reject ───
    @PostMapping("/request/{requesterId}/reject")
    public ResponseEntity<ApiResponse<Void>> rejectFriendRequest(
            @AuthenticationPrincipal String rejectorId,
            @PathVariable String requesterId) {

        rejectFriendRequestUseCase.execute(requesterId, rejectorId);
        return ResponseEntity.ok(ApiResponse.ok());
    }

    // ── DELETE /api/friendships/{friendId} ───────────────────
    @DeleteMapping("/{friendId}")
    public ResponseEntity<ApiResponse<Void>> unfriend(
            @AuthenticationPrincipal String userId,
            @PathVariable String friendId) {

        unfriendUseCase.execute(userId, friendId);
        return ResponseEntity.ok(ApiResponse.ok());
    }

    // ── POST /api/friendships/block/{targetId} ───────────────
    @PostMapping("/block/{targetId}")
    public ResponseEntity<ApiResponse<Void>> blockUser(
            @AuthenticationPrincipal String blockerId,
            @PathVariable String targetId) {

        blockUserUseCase.execute(blockerId, targetId);
        return ResponseEntity.ok(ApiResponse.ok());
    }

    // ── DELETE /api/friendships/block/{targetId} ─────────────
    @DeleteMapping("/block/{targetId}")
    public ResponseEntity<ApiResponse<Void>> unblockUser(
            @AuthenticationPrincipal String blockerId,
            @PathVariable String targetId) {

        unblockUserUseCase.execute(blockerId, targetId);
        return ResponseEntity.ok(ApiResponse.ok());
    }

    // ── GET /api/friendships ─────────────────────────────────
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<UserResponse>>> listFriends(
            @AuthenticationPrincipal String userId,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size) {

        PageResponse<UserResponse> data =
                listFriendsUseCase.execute(userId, new PageRequest(page, size));

        return ResponseEntity.ok(ApiResponse.ok(data));
    }

    // ── GET /api/friendships/requests/sent ───────────────────
    @GetMapping("/requests/sent")
    public ResponseEntity<ApiResponse<PageResponse<UserResponse>>> listSentRequests(
            @AuthenticationPrincipal String userId,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size) {

        PageResponse<UserResponse> data =
                listSentRequestsUseCase.execute(userId, new PageRequest(page, size));

        return ResponseEntity.ok(ApiResponse.ok(data));
    }

    // ── GET /api/friendships/requests/received ───────────────
    @GetMapping("/requests/received")
    public ResponseEntity<ApiResponse<PageResponse<UserResponse>>> listReceivedRequests(
            @AuthenticationPrincipal String userId,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size) {

        PageResponse<UserResponse> data =
                listReceivedRequestsUseCase.execute(userId, new PageRequest(page, size));

        return ResponseEntity.ok(ApiResponse.ok(data));
    }

    // ── GET /api/friendships/status/{targetId} ───────────────
    @GetMapping("/status/{targetId}")
    public ResponseEntity<ApiResponse<Map<String, String>>> getFriendStatus(
            @AuthenticationPrincipal String userId,
            @PathVariable String targetId) {

        String status = getFriendStatusUseCase.execute(userId, targetId);
        return ResponseEntity.ok(ApiResponse.ok(Map.of("status", status)));
    }
}