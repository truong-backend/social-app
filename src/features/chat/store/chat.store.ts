import { create } from 'zustand'
import { immer } from 'zustand/middleware/immer'
import type { Message } from '../types/chat.types'

interface ChatState {
  activeChatId: string | null
  messagesByChatId: Record<string, Message[]>

  setActiveChatId: (chatId: string | null) => void
  appendMessage: (chatId: string, message: Message) => void
  updateMessage: (chatId: string, updatedMessage: Message) => void
  removeMessage: (chatId: string, messageId: string) => void
  setMessages: (chatId: string, messages: Message[]) => void
}

export const useChatStore = create<ChatState>()(
  immer((set) => ({
    activeChatId: null,
    messagesByChatId: {},

    setActiveChatId: (chatId) =>
      set((state) => { state.activeChatId = chatId }),

    appendMessage: (chatId, message) =>
      set((state) => {
        if (!state.messagesByChatId[chatId]) {
          state.messagesByChatId[chatId] = []
        }
        const exists = state.messagesByChatId[chatId].some((m: Message) => m.id === message.id)
        if (!exists) {
          state.messagesByChatId[chatId].push(message)
        }
      }),

    updateMessage: (chatId, updatedMessage) =>
      set((state) => {
        const messages = state.messagesByChatId[chatId]
        if (!messages) return
        const index = messages.findIndex((m: Message) => m.id === updatedMessage.id)
        if (index !== -1) messages[index] = updatedMessage
      }),

    removeMessage: (chatId, messageId) =>
      set((state) => {
        if (!state.messagesByChatId[chatId]) return
        state.messagesByChatId[chatId] = state.messagesByChatId[chatId].filter(
          (m: Message) => m.id !== messageId,
        )
      }),

    setMessages: (chatId, messages) =>
      set((state) => { state.messagesByChatId[chatId] = messages }),
  })),
)