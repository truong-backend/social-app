import { useMutation, useQueryClient } from '@tanstack/react-query'
import toast from 'react-hot-toast'
import { sendMessageApi } from '../api/chat.api'
import { CHAT_QUERY_KEYS } from '../constants/chat.constants'
import { useChatStore } from '../store/chat.store'
import { extractErrorMessage } from '@utils/api-response'
import type { SendMessageRequest } from '../types/chat.types'

export const useSendMessage = (targetUserId: string, chatId: string | null) => {
  const queryClient = useQueryClient()
  const appendMessage = useChatStore((state) => state.appendMessage)

  return useMutation({
    mutationFn: ({
      payload,
      files,
    }: {
      payload: SendMessageRequest
      files?: File[]
    }) => sendMessageApi(targetUserId, payload, files),

    onSuccess: (newMessage) => {
      const resolvedChatId = chatId ?? newMessage.chatId
      appendMessage(resolvedChatId, newMessage)
      queryClient.invalidateQueries({ queryKey: CHAT_QUERY_KEYS.messages(resolvedChatId) })
      queryClient.invalidateQueries({ queryKey: CHAT_QUERY_KEYS.list() })
    },

    onError: (error) => {
      toast.error(extractErrorMessage(error))
    },
  })
}