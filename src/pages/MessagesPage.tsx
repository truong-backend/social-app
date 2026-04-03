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

  const activeChat   = chats?.find((c) => c.id === resolvedChatId)
  const targetUserId = activeChat?.memberIds.find((id) => id !== userId) ?? ''

  return (
    <div className="flex h-[calc(100vh-0px)] overflow-hidden bg-background">
      {/* Sidebar */}
      <ChatSidebar />

      {/* Main panel */}
      <div className="flex-1 flex flex-col bg-surface-container-low">
        {resolvedChatId && targetUserId ? (
          <ChatWindow
            chatId={resolvedChatId}
            targetUserId={targetUserId}
            targetUsername={targetUserId}
          />
        ) : resolvedChatId ? (
          <div className="flex-1 flex flex-col items-center justify-center gap-3 text-on-surface-variant">
            <span className="material-symbols-outlined text-5xl animate-spin" style={{ animationDuration: '2s' }}>
              progress_activity
            </span>
            <p className="text-sm font-medium">Đang tải thông tin chat...</p>
          </div>
        ) : (
          <div className="flex-1 flex flex-col items-center justify-center gap-4 text-on-surface-variant">
            <div className="w-20 h-20 rounded-full bg-surface-container-high flex items-center justify-center">
              <span className="material-symbols-outlined text-4xl text-primary">chat_bubble</span>
            </div>
            <div className="text-center">
              <p className="font-bold text-on-surface">Tin nhắn của bạn</p>
              <p className="text-sm mt-1">Chọn một đoạn chat để bắt đầu nhắn tin</p>
            </div>
          </div>
        )}
      </div>
    </div>
  )
}