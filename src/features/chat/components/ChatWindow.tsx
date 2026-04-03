import { useState, useEffect, useRef } from 'react'
import { useShallow } from 'zustand/react/shallow'
import { useChatMessages } from '../hooks/useChatMessages'
import { useSendMessage } from '../hooks/useSendMessage'
import { useChatWebSocket } from '../hooks/useChatWebSocket'
import { useChatStore } from '../store/chat.store'
import { MessageBubble } from './MessageBubble'
import { useSessionStore } from '@stores/session.store'
import { CallButton } from '@features/call/components/CallButton'

interface ChatWindowProps {
  chatId:         string
  targetUserId:   string
  targetUsername: string
}

export const ChatWindow = ({ chatId, targetUserId, targetUsername }: ChatWindowProps) => {
  const [inputContent, setInputContent] = useState('')
  const [selectedFiles, setSelectedFiles] = useState<File[]>([])
  const bottomRef  = useRef<HTMLDivElement>(null)
  const fileInputRef = useRef<HTMLInputElement>(null)

  const userId            = useSessionStore((state) => state.userId) ?? ''
  // FIX: useShallow để Zustand so sánh shallow thay vì tạo array mới [] mỗi render
  const messagesFromStore = useChatStore(
    useShallow((state) => state.messagesByChatId[chatId] ?? [])
  )

  useChatWebSocket(chatId)

  const { data, fetchNextPage, hasNextPage, isLoading } = useChatMessages(chatId)
  const sendMessage = useSendMessage(targetUserId, chatId)

  useEffect(() => {
    bottomRef.current?.scrollIntoView({ behavior: 'smooth' })
  }, [messagesFromStore.length])

  const handleSend = (event: React.FormEvent) => {
    event.preventDefault()
    if (!inputContent.trim() && selectedFiles.length === 0) return
    sendMessage.mutate(
      { payload: { content: inputContent }, files: selectedFiles },
      { onSuccess: () => { setInputContent(''); setSelectedFiles([]) } },
    )
  }

  const queryMessages = data?.pages.flat() ?? []
  const allMessages   = [...queryMessages, ...messagesFromStore].filter(
    (msg, index, self) => self.findIndex((m) => m.id === msg.id) === index,
  )

  return (
    <div className="chat-window">
      <header className="chat-window__header">
        <h2 className="chat-window__title">{targetUsername}</h2>
        {targetUserId && (
          <CallButton targetUserId={targetUserId} targetName={targetUsername} />
        )}
      </header>

      <div className="chat-window__messages">
        {hasNextPage && (
          <button className="chat-window__load-more" onClick={() => fetchNextPage()}>
            Tải tin nhắn cũ hơn
          </button>
        )}
        {isLoading ? (
          <div className="chat-window__loading">Đang tải...</div>
        ) : (
          allMessages.map((message) => (
            <MessageBubble
              key={message.id}
              message={message}
              isOwnMessage={message.senderId === userId}
            />
          ))
        )}
        <div ref={bottomRef} />
      </div>

      <form className="chat-window__form" onSubmit={handleSend}>
        {selectedFiles.length > 0 && (
          <div className="chat-window__file-preview">
            {selectedFiles.map((file, index) => (
              <span key={index} className="chat-window__file-tag">
                {file.name}
                <button
                  type="button"
                  onClick={() => setSelectedFiles((prev) => prev.filter((_, i) => i !== index))}
                >✕</button>
              </span>
            ))}
          </div>
        )}
        <div className="chat-window__input-row">
          <button type="button" className="chat-window__attach-btn"
            onClick={() => fileInputRef.current?.click()}>📎</button>
          <input ref={fileInputRef} type="file" multiple className="chat-window__file-input"
            onChange={(e) => setSelectedFiles((prev) => [...prev, ...Array.from(e.target.files ?? [])])} />
          <input
            className="chat-window__text-input"
            type="text"
            placeholder="Nhập tin nhắn..."
            value={inputContent}
            onChange={(e) => setInputContent(e.target.value)}
            onKeyDown={(e) => {
              if (e.key === 'Enter' && !e.shiftKey) {
                e.preventDefault()
                handleSend(e as unknown as React.FormEvent)
              }
            }}
          />
          <button
            type="submit"
            className="chat-window__send-btn"
            disabled={sendMessage.isPending || (!inputContent.trim() && selectedFiles.length === 0)}
          >Gửi</button>
        </div>
      </form>
    </div>
  )
}