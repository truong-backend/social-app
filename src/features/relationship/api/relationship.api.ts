import { axiosInstance } from '@services/axios.instance'
import { unwrapData } from '@utils/api-response'

// ── Friend ────────────────────────────────────────────────────

export const sendFriendRequestApi   = (targetId: string)  => axiosInstance.post(`/api/relationships/friends/${targetId}`)
export const acceptFriendRequestApi = (senderId: string)  => axiosInstance.put(`/api/relationships/friends/${senderId}/accept`)
export const deleteRequestApi       = (targetId: string)  => axiosInstance.delete(`/api/relationships/friends/${targetId}/request`)
export const unfriendApi            = (targetId: string)  => axiosInstance.delete(`/api/relationships/friends/${targetId}`)

export const getFriendsApi = async (): Promise<string[]> => {
  const res = await axiosInstance.get('/api/relationships/friends')
  return unwrapData<string[]>(res) ?? []
}

export const getReceivedRequestsApi = async (): Promise<string[]> => {
  const res = await axiosInstance.get('/api/relationships/requests/received')
  return unwrapData<string[]>(res) ?? []
}

export const getSentRequestsApi = async (): Promise<string[]> => {
  const res = await axiosInstance.get('/api/relationships/requests/sent')
  return unwrapData<string[]>(res) ?? []
}

// ── Block ─────────────────────────────────────────────────────

export const blockUserApi   = (targetId: string) => axiosInstance.post(`/api/relationships/blocks/${targetId}`)
export const unblockUserApi = (targetId: string) => axiosInstance.delete(`/api/relationships/blocks/${targetId}`)

export const getBlockedUsersApi = async (): Promise<string[]> => {
  const res = await axiosInstance.get('/api/relationships/blocks')
  return unwrapData<string[]>(res) ?? []
}