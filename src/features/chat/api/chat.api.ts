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

    setActiveChatId: (chatId: string | null) =>
      set((state) => {
        state.activeChatId = chatId
      }),

    appendMessage: (chatId: string, message: Message) =>
      set((state) => {
        if (!state.messagesByChatId[chatId]) {
          state.messagesByChatId[chatId] = []
        }
        // Tránh duplicate nếu optimistic update đã thêm
        const exists = state.messagesByChatId[chatId].some((m: Message) => m.id === message.id)
        if (!exists) {
          state.messagesByChatId[chatId].push(message)
        }
      }),

    updateMessage: (chatId: string, updatedMessage: Message) =>
      set((state) => {
        const messages = state.messagesByChatId[chatId]
        if (!messages) return
        const index = messages.findIndex((m: Message) => m.id === updatedMessage.id)
        if (index !== -1) {
          messages[index] = updatedMessage
        }
      }),

    removeMessage: (chatId: string, messageId: string) =>
      set((state) => {
        const messages = state.messagesByChatId[chatId]
        if (!messages) return
        state.messagesByChatId[chatId] = messages.filter((m: Message) => m.id !== messageId)
      }),

    setMessages: (chatId: string, messages: Message[]) =>
      set((state) => {
        state.messagesByChatId[chatId] = messages
      }),
  })),
)