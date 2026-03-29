// Queries
export { useFriends }           from './hooks/useRelationship'
export { useReceivedRequests }  from './hooks/useRelationship'
export { useSentRequests }      from './hooks/useRelationship'
export { useBlockedUsers }      from './hooks/useRelationship'

// Mutations — friend
export { useSendFriendRequest }   from './hooks/useRelationship'
export { useAcceptFriendRequest } from './hooks/useRelationship'
export { useDeleteRequest }       from './hooks/useRelationship'
export { useUnfriend }            from './hooks/useRelationship'

// Mutations — block
export { useBlockUser }   from './hooks/useRelationship'
export { useUnblockUser } from './hooks/useRelationship'

// Constants
export { RELATIONSHIP_QUERY_KEYS } from './constants/relationship.constants'

// API (raw — use hooks instead in components)
export {
  sendFriendRequestApi,
  acceptFriendRequestApi,
  deleteRequestApi,
  unfriendApi,
  blockUserApi,
  unblockUserApi,
  getFriendsApi,
  getReceivedRequestsApi,
  getSentRequestsApi,
  getBlockedUsersApi,
} from './api/relationship.api'