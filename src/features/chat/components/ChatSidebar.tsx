import { useState } from 'react'
import { useChatList } from '../hooks/useChatList'
import { useChatStore } from '../store/chat.store'
import { useSessionStore } from '@stores/session.store'
import { useQuery } from '@tanstack/react-query'
import { getProfileApi } from '@features/user/api/user.api'
import { searchChatsApi } from '../api/chat.api'
import { useDebounce } from '@hooks/useDebounce'
import { formatRelativeTime } from '@utils/date.formatter'
import type { Chat } from '../types/chat.types'

// Component hiển thị từng chat item với tên thật
const ChatItem = ({
  chat,
  otherMemberId,
  isActive,
  onClick,
}: {
  chat: Chat
  otherMemberId: string
  isActive: boolean
  onClick: () => void
}) => {
  const { data: profile } = useQuery({
    queryKey: ['user', 'profile', otherMemberId],
    queryFn: () => getProfileApi(otherMemberId),
    staleTime: 1000 * 60 * 5,
    enabled: !!otherMemberId,
  })

  const displayName = profile
    ? `${profile.familyName ?? ''} ${profile.givenName ?? ''}`.trim() || profile.username || otherMemberId
    : otherMemberId.slice(0, 8) + '...'

  const avatar = profile?.profilePictureUrl

  return (
    <button
      className={`w-full flex items-center gap-4 p-4 rounded-xl transition-colors text-left ${
        isActive ? 'bg-surface-container-lowest shadow-sm' : 'hover:bg-white/40'
      }`}
      onClick={onClick}
    >
      {/* Avatar */}
      <div className="relative flex-shrink-0">
        {avatar ? (
          <img
            src={avatar}
            alt={displayName}
            className="w-12 h-12 rounded-full object-cover"
          />
        ) : (
          <div className="w-12 h-12 rounded-full bg-gradient-to-br from-primary to-primary-container text-on-primary flex items-center justify-center font-bold text-sm">
            {displayName.charAt(0).toUpperCase()}
          </div>
        )}
        {isActive && (
          <div className="absolute bottom-0 right-0 w-3.5 h-3.5 bg-tertiary-fixed-dim rounded-full border-2 border-white shadow-[0_0_8px_rgba(96,236,121,0.5)]" />
        )}
      </div>

      <div className="flex-1 min-w-0">
        <div className="flex justify-between items-baseline">
          <h4 className="font-semibold text-on-surface truncate">{displayName}</h4>
          <span className="text-[10px] text-slate-400 font-medium flex-shrink-0 ml-2">
            {formatRelativeTime(chat.createdAt)}
          </span>
        </div>
        <p className="text-xs text-on-surface-variant truncate mt-0.5">
          {profile?.username ? `@${profile.username}` : 'Nhấn để xem tin nhắn'}
        </p>
      </div>
    </button>
  )
}

export const ChatSidebar = () => {
  const [searchQuery, setSearchQuery] = useState('')
  const debouncedQuery  = useDebounce(searchQuery, 300)
  const { data: allChats, isLoading: allLoading } = useChatList()
  const { data: searchedChats, isLoading: searchLoading } = useQuery({
    queryKey: ['chats', 'search', debouncedQuery],
    queryFn: () => searchChatsApi(debouncedQuery),
    enabled: debouncedQuery.trim().length >= 1,
    staleTime: 1000 * 30,
  })

  const setActiveChatId = useChatStore((state) => state.setActiveChatId)
  const activeChatId    = useChatStore((state) => state.activeChatId)
  const userId          = useSessionStore((state) => state.userId) ?? ''

  const isSearching = debouncedQuery.trim().length >= 1
  const chats       = isSearching ? (searchedChats ?? []) : (allChats ?? [])
  const isLoading   = isSearching ? searchLoading : allLoading

  const chatItems = chats
    .map((chat: Chat) => ({
      chat,
      otherMemberId: chat.memberIds.find((id: string) => id !== userId) ?? '',
    }))
    .filter((item) => !!item.otherMemberId)

  return (
    <aside className="flex flex-col w-full md:w-80 lg:w-96 h-full bg-surface-container-low border-r border-transparent">
      {/* Header */}
      <div className="p-6">
        <h2
          className="text-2xl font-extrabold tracking-tight mb-4"
          style={{ fontFamily: "'Plus Jakarta Sans', sans-serif" }}
        >
          Chats
        </h2>
        {/* Search */}
        <div className="relative mb-3">
          <span className="material-symbols-outlined absolute left-3 top-1/2 -translate-y-1/2 text-slate-400 text-lg">
            search
          </span>
          <input
            type="text"
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
            placeholder="Tìm kiếm cuộc trò chuyện..."
            className="w-full pl-9 pr-3 py-2 bg-slate-100/60 rounded-full text-sm focus:ring-2 focus:ring-primary/20 outline-none placeholder:text-slate-400"
          />
          {searchQuery && (
            <button
              onClick={() => setSearchQuery('')}
              className="absolute right-3 top-1/2 -translate-y-1/2 text-slate-400 hover:text-slate-600"
            >
              <span className="material-symbols-outlined text-base">close</span>
            </button>
          )}
        </div>
        {/* Filter tabs */}
        <div className="flex gap-2">
          <button className="bg-primary-container/20 text-primary-dim px-4 py-1.5 rounded-full text-xs font-bold">
            All
          </button>
          <button className="text-slate-500 px-4 py-1.5 rounded-full text-xs font-bold hover:bg-slate-200/50 transition-colors">
            Unread
          </button>
          <button className="text-slate-500 px-4 py-1.5 rounded-full text-xs font-bold hover:bg-slate-200/50 transition-colors">
            Groups
          </button>
        </div>
      </div>

      {/* Chat list */}
      <div className="flex-1 overflow-y-auto px-2 pb-6 space-y-1" style={{ scrollbarWidth: 'thin' }}>
        {isLoading ? (
          <div className="flex items-center justify-center py-12 text-sm text-on-surface-variant">
            <span className="inline-block w-5 h-5 rounded-full border-2 border-primary/30 border-t-primary animate-spin mr-2" />
            Đang tải...
          </div>
        ) : chatItems.length === 0 ? (
          <div className="flex flex-col items-center justify-center py-16 gap-2 text-on-surface-variant">
            <span className="material-symbols-outlined text-4xl opacity-40">chat_bubble</span>
            <p className="text-sm">
              {isSearching ? 'Không tìm thấy cuộc trò chuyện nào' : 'Chưa có đoạn chat nào'}
            </p>
          </div>
        ) : (
          chatItems.map(({ chat, otherMemberId }) => (
            <ChatItem
              key={chat.id}
              chat={chat}
              otherMemberId={otherMemberId}
              isActive={activeChatId === chat.id}
              onClick={() => setActiveChatId(chat.id)}
            />
          ))
        )}
      </div>
    </aside>
  )
}
