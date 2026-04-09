import { useParams, useSearchParams } from 'react-router-dom'
import { ChatSidebar } from '@features/chat/components/ChatSidebar'
import { ChatWindow } from '@features/chat/components/ChatWindow'
import { useChatStore } from '@features/chat/store/chat.store'
import { useChatList } from '@features/chat/hooks/useChatList'
import { useSessionStore } from '@stores/session.store'
import { useQuery } from '@tanstack/react-query'
import { getProfileApi } from '@features/user/api/user.api'
import { useEffect } from 'react'

export const MessagesPage = () => {
  const { chatId: chatIdFromParams } = useParams<{ chatId?: string }>()
  const [searchParams] = useSearchParams()
  const withUserId = searchParams.get('with') // từ /messages?with=userId (điều hướng từ profile)

  const { activeChatId, setActiveChatId } = useChatStore()
  const { data: chats } = useChatList()
  const userId = useSessionStore((state) => state.userId) ?? ''

  // Ưu tiên: chatId từ URL params → activeChatId trong store
  useEffect(() => {
    if (chatIdFromParams && chatIdFromParams !== activeChatId) {
      setActiveChatId(chatIdFromParams)
    }
  }, [chatIdFromParams, activeChatId, setActiveChatId])

  // Nếu có ?with=userId, tìm chat đã tồn tại với người đó; nếu chưa có thì để ChatWindow tự tạo
  useEffect(() => {
    if (!withUserId || !chats) return
    const existingChat = chats.find((c) => c.memberIds.includes(withUserId) && c.memberIds.includes(userId))
    if (existingChat) {
      setActiveChatId(existingChat.id)
    }
    // Nếu chưa có chat, activeChatId = null → ChatWindow sẽ mở chế độ "new chat" với withUserId
  }, [withUserId, chats, userId, setActiveChatId])

  const resolvedChatId = activeChatId ?? chatIdFromParams ?? null
  const activeChat     = chats?.find((c) => c.id === resolvedChatId)

  // targetUserId: từ chat đang active, hoặc từ query param ?with=
  const targetUserId = activeChat?.memberIds.find((id) => id !== userId) ?? withUserId ?? ''

  const { data: targetProfile } = useQuery({
    queryKey: ['user', 'profile', targetUserId],
    queryFn: () => getProfileApi(targetUserId),
    staleTime: 1000 * 60 * 5,
    enabled: !!targetUserId,
  })

  const targetUsername = targetProfile
    ? `${targetProfile.familyName ?? ''} ${targetProfile.givenName ?? ''}`.trim() ||
      targetProfile.username ||
      targetUserId
    : targetUserId

  // Có targetUserId (dù chưa có chatId) → hiện ChatWindow để nhắn tin ngay
  const shouldShowChat = !!targetUserId

  return (
    <div className="flex h-[calc(100vh-64px)] overflow-hidden bg-surface-container-low">
      {/* Sidebar */}
      <ChatSidebar />

      {/* Main panel */}
      <div className="flex-1 flex flex-col">
        {shouldShowChat ? (
          <ChatWindow
            chatId={resolvedChatId ?? null}
            targetUserId={targetUserId}
            targetUsername={targetUsername}
            targetProfilePic={targetProfile?.profilePictureUrl ?? undefined}
          />
        ) : (
          <div className="flex-1 flex flex-col items-center justify-center gap-4 text-on-surface-variant">
            <div className="w-20 h-20 rounded-full bg-surface-container-high flex items-center justify-center">
              <span className="material-symbols-outlined text-4xl text-primary">chat_bubble</span>
            </div>
            <div className="text-center">
              <p
                className="font-bold text-on-surface"
                style={{ fontFamily: "'Plus Jakarta Sans', sans-serif" }}
              >
                Your Messages
              </p>
              <p className="text-sm mt-1">Select a chat to start messaging</p>
            </div>
          </div>
        )}
      </div>
    </div>
  )
}