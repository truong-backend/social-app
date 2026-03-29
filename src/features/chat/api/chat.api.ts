import { axiosInstance } from '@services/axios.instance'
import { unwrapData } from '@utils/api-response'
import type {
  Chat,
  Message,
  SendMessageRequest,
  UpdateMessageRequest,
  DeleteMessageRequest,
} from '../types/chat.types'

export const getChatListApi = async (): Promise<Chat[]> => {
  const response = await axiosInstance.get('/api/messages/chats')
  return unwrapData(response) ?? []
}

export const getChatMessagesApi = async (
  chatId: string,
  skip = 0,
  limit = 20,
): Promise<Message[]> => {
  const response = await axiosInstance.get(`/api/messages/chats/${chatId}`, {
    params: { skip, limit },
  })
  return unwrapData(response) ?? []
}

export const sendMessageApi = async (
  targetUserId: string,
  payload: SendMessageRequest,
  files?: File[],
): Promise<Message> => {
  const formData = new FormData()
  formData.append('data', new Blob([JSON.stringify(payload)], { type: 'application/json' }))
  files?.forEach((file) => formData.append('files', file))

  const response = await axiosInstance.post(
    `/api/messages/chats/${targetUserId}`,
    formData,
    { headers: { 'Content-Type': 'multipart/form-data' } },
  )
  return unwrapData(response)
}

export const updateMessageApi = async (
  messageId: string,
  payload: UpdateMessageRequest,
): Promise<Message> => {
  const response = await axiosInstance.put(`/api/messages/${messageId}`, payload)
  return unwrapData(response)
}

export const deleteMessageApi = async (
  messageId: string,
  payload: DeleteMessageRequest,
): Promise<void> => {
  await axiosInstance.delete(`/api/messages/${messageId}`, { data: payload })
}

export const searchChatsApi = async (query: string): Promise<Chat[]> => {
  const response = await axiosInstance.get('/api/messages/chats/search', {
    params: { q: query },
  })
  return unwrapData(response) ?? []
}