import { useEffect } from 'react'
import { websocketService } from '@services/websocket.service'
import { useChatStore } from '../store/chat.store'
import { CHAT_WEBSOCKET_EVENTS } from '../constants/chat.constants'
import type { Message } from '../types/chat.types'

export const useChatWebSocket = (chatId: string) => {
  const { appendMessage, updateMessage, removeMessage } = useChatStore()

  useEffect(() => {
    if (!chatId) return

    const newMessageTopic = `/topic/chat.${chatId}.${CHAT_WEBSOCKET_EVENTS.NEW_MESSAGE}`
    const updatedTopic = `/topic/chat.${chatId}.${CHAT_WEBSOCKET_EVENTS.MESSAGE_UPDATED}`
    const deletedTopic = `/topic/chat.${chatId}.${CHAT_WEBSOCKET_EVENTS.MESSAGE_DELETED}`

    websocketService.subscribe(newMessageTopic, (frame) => {
      const message: Message = JSON.parse(frame.body)
      appendMessage(chatId, message)
    })

    websocketService.subscribe(updatedTopic, (frame) => {
      const updatedMessage: Message = JSON.parse(frame.body)
      updateMessage(chatId, updatedMessage)
    })

    websocketService.subscribe(deletedTopic, (frame) => {
      const deletedMessageId: string = JSON.parse(frame.body)
      removeMessage(chatId, deletedMessageId)
    })

    return () => {
      websocketService.unsubscribe(newMessageTopic)
      websocketService.unsubscribe(updatedTopic)
      websocketService.unsubscribe(deletedTopic)
    }
  }, [chatId, appendMessage, updateMessage, removeMessage])
}