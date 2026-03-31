import { useParams } from 'react-router-dom'
import { ChatSidebar } from '@features/chat/components/ChatSidebar'
import { ChatWindow } from '@features/chat/components/ChatWindow'
import { useChatStore } from '@features/chat/store/chat.store'
import { useEffect } from 'react'


export const MessagesPage = () => {
  const { chatId: chatIdFromParams } = useParams<{ chatId?: string }>()
  const { activeChatId, setActiveChatId } = useChatStore()

  // Sync URL param → store
  useEffect(() => {
    if (chatIdFromParams && chatIdFromParams !== activeChatId) {
      setActiveChatId(chatIdFromParams)
    }
  }, [chatIdFromParams, activeChatId, setActiveChatId])

  const resolvedChatId = activeChatId ?? chatIdFromParams ?? null

  return (
    <div className="messages-page">

      
      <ChatSidebar />

      <div className="messages-page__main">
        {resolvedChatId ? (
          <ChatWindow
            chatId={resolvedChatId}
            targetUserId=""
            targetUsername="Đoạn chat"
          />
        ) : (
          <div className="messages-page__empty">
            <p>Chọn một đoạn chat để bắt đầu nhắn tin</p>
          </div>
        )}
      </div>
    </div>
  )
}