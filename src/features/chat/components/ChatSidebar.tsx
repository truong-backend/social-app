import { useState } from 'react'
import { useChatList } from '../hooks/useChatList'
import { useChatStore } from '../store/chat.store'
import { useSessionStore } from '@stores/session.store'
import type { Chat } from '../types/chat.types'

export const ChatSidebar = () => {
  const [searchQuery, setSearchQuery] = useState('')
  const { data: chats, isLoading } = useChatList()
  const setActiveChatId = useChatStore((state) => state.setActiveChatId)
  const activeChatId = useChatStore((state) => state.activeChatId)
  const userId = useSessionStore((state) => state.userId) ?? ''

  const filteredChats = (chats ?? [])
    .map((chat: Chat) => ({
      chat,
      otherMemberId: chat.memberIds.find((id: string) => id !== userId),
    }))
    .filter((item): item is { chat: Chat; otherMemberId: string } =>
      !!item.otherMemberId,
    )

  return (
    <aside className="chat-sidebar">
      <div className="chat-sidebar__search">
        <input
          type="text"
          placeholder="Tìm kiếm đoạn chat..."
          value={searchQuery}
          onChange={(e) => setSearchQuery(e.target.value)}
          className="chat-sidebar__search-input"
        />
      </div>

      <div className="chat-sidebar__list">
        {isLoading ? (
          <div className="chat-sidebar__loading">Đang tải...</div>
        ) : filteredChats.length === 0 ? (
          <div className="chat-sidebar__empty">Chưa có đoạn chat nào</div>
        ) : (
          filteredChats.map(({ chat, otherMemberId }) => (
            <button
              key={chat.id}
              className={`chat-sidebar__item ${
                activeChatId === chat.id ? 'chat-sidebar__item--active' : ''
              }`}
              onClick={() => setActiveChatId(chat.id)}
            >
              <div className="chat-sidebar__item-avatar">
                <div className="chat-sidebar__item-avatar-placeholder">
                  {otherMemberId.slice(0, 2).toUpperCase()}
                </div>
              </div>
              <div className="chat-sidebar__item-info">
                <span className="chat-sidebar__item-name">{otherMemberId}</span>
              </div>
            </button>
          ))
        )}
      </div>
    </aside>
  )
}