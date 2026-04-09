import { useMutation, useQueryClient } from '@tanstack/react-query'
import toast from 'react-hot-toast'
import { sendMessageApi } from '../api/chat.api'
import { CHAT_QUERY_KEYS } from '../constants/chat.constants'
import { useChatStore } from '../store/chat.store'
import { extractErrorMessage } from '@utils/api-response'
import type { Message, SendMessageRequest } from '../types/chat.types'

export const useSendMessage = (targetUserId: string, chatId: string | null) => {
  const queryClient = useQueryClient()
  const appendMessage = useChatStore((state) => state.appendMessage)
  const setActiveChatId = useChatStore((state) => state.setActiveChatId)

  return useMutation<Message, Error, { payload: SendMessageRequest; files?: File[] }>({
    mutationFn: ({ payload, files }) => sendMessageApi(targetUserId, payload, files),

    onSuccess: (newMessage) => {
      const resolvedChatId = chatId ?? newMessage.chatId
      appendMessage(resolvedChatId, newMessage)

      // Nếu là cuộc trò chuyện mới (chưa có chatId), set active chat
      if (!chatId && newMessage.chatId) {
        setActiveChatId(newMessage.chatId)
      }

      // Invalidate để sidebar cập nhật danh sách chat ngay
      queryClient.invalidateQueries({ queryKey: CHAT_QUERY_KEYS.list() })
      queryClient.invalidateQueries({ queryKey: CHAT_QUERY_KEYS.messages(resolvedChatId) })
    },

    onError: (error) => {
      toast.error(extractErrorMessage(error))
    },
  })
}