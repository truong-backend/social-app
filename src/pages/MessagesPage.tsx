import { useParams } from 'react-router-dom'
import { ChatSidebar } from '@features/chat/components/ChatSidebar'
import { ChatWindow } from '@features/chat/components/ChatWindow'
import { useChatStore } from '@features/chat/store/chat.store'
import { useChatList } from '@features/chat/hooks/useChatList'
import { useSessionStore } from '@stores/session.store'
import { useEffect } from 'react'

export const MessagesPage = () => {
  const { chatId: chatIdFromParams } = useParams<{ chatId?: string }>()
  const { activeChatId, setActiveChatId } = useChatStore()
  const { data: chats } = useChatList()
  const userId = useSessionStore((state) => state.userId) ?? ''

  // Sync URL param → store
  useEffect(() => {
    if (chatIdFromParams && chatIdFromParams !== activeChatId) {
      setActiveChatId(chatIdFromParams)
    }
  }, [chatIdFromParams, activeChatId, setActiveChatId])

  const resolvedChatId = activeChatId ?? chatIdFromParams ?? null

  // FIX: Derive targetUserId từ chat data thay vì hardcode ''
  const activeChat = chats?.find((c) => c.id === resolvedChatId)
  const targetUserId = activeChat?.memberIds.find((id) => id !== userId) ?? ''

  return (
    <div className="messages-page">
      <ChatSidebar />

      <div className="messages-page__main">
        {resolvedChatId && targetUserId ? (
          <ChatWindow
            chatId={resolvedChatId}
            targetUserId={targetUserId}
            targetUsername={targetUserId}
          />
        ) : resolvedChatId ? (
          <div className="messages-page__empty">
            <p>Đang tải thông tin chat...</p>
          </div>
        ) : (
          <div className="messages-page__empty">
            <p>Chọn một đoạn chat để bắt đầu nhắn tin</p>
          </div>
        )}
      </div>
    </div>
  )
}