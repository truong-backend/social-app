import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import toast from 'react-hot-toast'
import {
  sendFriendRequestApi, acceptFriendRequestApi, deleteRequestApi,
  unfriendApi, blockUserApi, unblockUserApi,
  getFriendsApi, getReceivedRequestsApi, getSentRequestsApi, getBlockedUsersApi,
} from '../api/relationship.api'
import { RELATIONSHIP_QUERY_KEYS } from '../constants/relationship.constants'
import { CHAT_QUERY_KEYS } from '@features/chat/constants/chat.constants'
import { extractErrorMessage } from '@utils/api-response'

const useInvalidateRelationships = () => {
  const qc = useQueryClient()
  return () => {
    qc.invalidateQueries({ queryKey: RELATIONSHIP_QUERY_KEYS.friends })
    qc.invalidateQueries({ queryKey: RELATIONSHIP_QUERY_KEYS.received })
    qc.invalidateQueries({ queryKey: RELATIONSHIP_QUERY_KEYS.sent })
    qc.invalidateQueries({ queryKey: RELATIONSHIP_QUERY_KEYS.blocked })
    // Invalidate profile queries để isFriend/hasSentRequest được cập nhật
    qc.invalidateQueries({ queryKey: ['user', 'profile'] })
    // Invalidate chat list để sidebar cập nhật khi kết bạn mới
    qc.invalidateQueries({ queryKey: CHAT_QUERY_KEYS.list() })
  }
}

// ── Queries ───────────────────────────────────────────────────

export const useFriends          = () => useQuery({ queryKey: RELATIONSHIP_QUERY_KEYS.friends,  queryFn: getFriendsApi })
export const useReceivedRequests = () => useQuery({ queryKey: RELATIONSHIP_QUERY_KEYS.received, queryFn: getReceivedRequestsApi })
export const useSentRequests     = () => useQuery({ queryKey: RELATIONSHIP_QUERY_KEYS.sent,      queryFn: getSentRequestsApi })
export const useBlockedUsers     = () => useQuery({ queryKey: RELATIONSHIP_QUERY_KEYS.blocked,   queryFn: getBlockedUsersApi })

// ── Mutations ─────────────────────────────────────────────────

export const useSendFriendRequest = () => {
  const invalidate = useInvalidateRelationships()
  return useMutation({
    mutationFn: sendFriendRequestApi,
    onSuccess:  () => { toast.success('Đã gửi lời mời kết bạn'); invalidate() },
    onError:    (e) => toast.error(extractErrorMessage(e)),
  })
}

export const useAcceptFriendRequest = () => {
  const invalidate = useInvalidateRelationships()
  return useMutation({
    mutationFn: acceptFriendRequestApi,
    onSuccess:  () => { toast.success('Đã chấp nhận lời mời'); invalidate() },
    onError:    (e) => toast.error(extractErrorMessage(e)),
  })
}

export const useDeleteRequest = () => {
  const invalidate = useInvalidateRelationships()
  return useMutation({
    mutationFn: deleteRequestApi,
    onSuccess:  () => { toast.success('Đã xóa lời mời'); invalidate() },
    onError:    (e) => toast.error(extractErrorMessage(e)),
  })
}

export const useUnfriend = () => {
  const invalidate = useInvalidateRelationships()
  return useMutation({
    mutationFn: unfriendApi,
    onSuccess:  () => { toast.success('Đã hủy kết bạn'); invalidate() },
    onError:    (e) => toast.error(extractErrorMessage(e)),
  })
}

export const useBlockUser = () => {
  const invalidate = useInvalidateRelationships()
  return useMutation({
    mutationFn: blockUserApi,
    onSuccess:  () => { toast.success('Đã chặn người dùng'); invalidate() },
    onError:    (e) => toast.error(extractErrorMessage(e)),
  })
}

export const useUnblockUser = () => {
  const invalidate = useInvalidateRelationships()
  return useMutation({
    mutationFn: unblockUserApi,
    onSuccess:  () => { toast.success('Đã bỏ chặn người dùng'); invalidate() },
    onError:    (e) => toast.error(extractErrorMessage(e)),
  })
}