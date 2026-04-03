import { useState } from 'react'
import { useChatList } from '../hooks/useChatList'
import { useChatStore } from '../store/chat.store'
import { useSessionStore } from '@stores/session.store'
import type { Chat } from '../types/chat.types'

export const ChatSidebar = () => {
  const [searchQuery, setSearchQuery] = useState('')
  const { data: chats, isLoading } = useChatList()
  const setActiveChatId = useChatStore((state) => state.setActiveChatId)
  const activeChatId    = useChatStore((state) => state.activeChatId)
  const userId          = useSessionStore((state) => state.userId) ?? ''

  const filteredChats = (chats ?? [])
    .map((chat: Chat) => ({
      chat,
      otherMemberId: chat.memberIds.find((id: string) => id !== userId),
    }))
    .filter((item): item is { chat: Chat; otherMemberId: string } =>
      !!item.otherMemberId &&
      item.otherMemberId.toLowerCase().includes(searchQuery.toLowerCase()),
    )

  return (
    <aside className="flex flex-col w-full md:w-[400px] h-full bg-surface border-r border-outline-variant/20">
      {/* Header + search */}
      <div className="px-6 py-8 flex flex-col gap-6">
        <div className="flex items-center justify-between">
          <h1 className="text-2xl font-extrabold tracking-tight font-headline text-on-surface">
            Messages
          </h1>
          <button className="w-10 h-10 flex items-center justify-center rounded-full bg-surface-container-low text-primary hover:bg-surface-container-high transition-colors">
            <span className="material-symbols-outlined">edit_square</span>
          </button>
        </div>

        <div className="relative">
          <span className="material-symbols-outlined absolute left-4 top-1/2 -translate-y-1/2 text-on-surface-variant">
            search
          </span>
          <input
            type="text"
            placeholder="Tìm kiếm đoạn chat..."
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
            className="w-full pl-12 pr-4 py-3 bg-surface-container-low border-none rounded-xl text-sm placeholder:text-on-surface-variant/60 focus:ring-2 focus:ring-primary/20 outline-none transition-all"
          />
        </div>
      </div>

      {/* Chat list */}
      <div className="flex-1 overflow-y-auto px-2 space-y-1">
        {isLoading ? (
          <div className="flex items-center justify-center py-12 text-sm text-on-surface-variant">
            <span className="inline-block w-5 h-5 rounded-full border-2 border-primary/30 border-t-primary animate-spin mr-2" />
            Đang tải...
          </div>
        ) : filteredChats.length === 0 ? (
          <div className="flex flex-col items-center justify-center py-16 gap-2 text-on-surface-variant">
            <span className="material-symbols-outlined text-4xl opacity-40">chat_bubble</span>
            <p className="text-sm">Chưa có đoạn chat nào</p>
          </div>
        ) : (
          filteredChats.map(({ chat, otherMemberId }) => (
            <button
              key={chat.id}
              className={`group w-full flex items-center gap-4 p-4 rounded-xl transition-colors text-left ${
                activeChatId === chat.id
                  ? 'bg-surface-container-highest'
                  : 'hover:bg-surface-container-low'
              }`}
              onClick={() => setActiveChatId(chat.id)}
            >
              {/* Avatar placeholder */}
              <div className="relative flex-shrink-0">
                <div className="w-14 h-14 rounded-full bg-gradient-to-br from-primary to-primary-container text-on-primary flex items-center justify-center text-lg font-bold">
                  {otherMemberId.slice(0, 2).toUpperCase()}
                </div>
                {activeChatId === chat.id && (
                  <span className="absolute bottom-0 right-0 w-3.5 h-3.5 bg-secondary rounded-full border-2 border-surface animate-pulse shadow-[0_0_8px_rgba(176,13,106,0.5)]" />
                )}
              </div>

              <div className="flex-1 min-w-0">
                <p className="font-bold text-on-surface truncate">{otherMemberId}</p>
                <p className="text-sm text-on-surface-variant truncate mt-0.5">
                  Nhấn để xem tin nhắn
                </p>
              </div>
            </button>
          ))
        )}
      </div>
    </aside>
  )
}